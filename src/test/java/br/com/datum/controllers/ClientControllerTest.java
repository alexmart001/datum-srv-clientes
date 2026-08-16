package br.com.datum.controllers;

import br.com.datum.config.SecurityConfig;
import br.com.datum.data.dto.ClienteDTO;
import br.com.datum.data.dto.CustomerScoreDTO;
import br.com.datum.exception.ExternalServiceException;
import br.com.datum.exception.ResourceNotFoundException;
import br.com.datum.services.ClienteServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de todos os endpoints de ClientController, usando MockMvc + o
 * ClienteServices mockado - sem tocar em banco de dados ou em qualquer
 * serviço externo. O JwtDecoder também é mockado para garantir que o
 * contexto de teste nunca tenta resolver o issuer-uri real via rede.
 */
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");

    private static final String CPF_VALIDO = "11604567805";
    private static final String CPF_INVALIDO = "00000000000";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteServices clienteServices;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private ClienteDTO cliente(Long id) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(id);
        dto.setNome("Alexandre Martins");
        dto.setCpf(CPF_VALIDO);
        dto.setEmail("alexandre@teste.com");
        dto.setStatus(true);
        return dto;
    }

    private String clienteJson(String nome, String cpf, String email, String status) {
        return String.format(
                "{\"nome\":\"%s\",\"cpf\":\"%s\",\"email\":\"%s\",\"status\":\"%s\"}",
                nome, cpf, email, status);
    }

    // ---------------------------------------------------------------
    // GET /customers
    // ---------------------------------------------------------------

    @Test
    void findAll_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void findAll_comTokenUser_retorna200EListaClientes() throws Exception {
        when(clienteServices.findAll()).thenReturn(List.of(cliente(1L)));

        mockMvc.perform(get("/customers").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(clienteServices).findAll();
    }

    @Test
    void findAll_comTokenAdmin_retorna200() throws Exception {
        when(clienteServices.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/customers").with(jwt().authorities(ROLE_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_comStatus_delegaParaSearchIgnorandoFindAll() throws Exception {
        when(clienteServices.search(null, "ACTIVE")).thenReturn(List.of(cliente(1L)));

        mockMvc.perform(get("/customers").param("status", "ACTIVE").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk());

        verify(clienteServices).search(null, "ACTIVE");
        verify(clienteServices, never()).findAll();
    }

    @Test
    void findAll_comStatusInvalido_retorna400() throws Exception {
        when(clienteServices.search(null, "FOO")).thenThrow(new IllegalArgumentException("Invalid status value: 'FOO'."));

        mockMvc.perform(get("/customers").param("status", "FOO").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // GET /customers/search
    // ---------------------------------------------------------------

    @Test
    void search_porNome_retorna200() throws Exception {
        when(clienteServices.search("Alexandre", null)).thenReturn(List.of(cliente(1L)));

        mockMvc.perform(get("/customers/search").param("name", "Alexandre").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Alexandre Martins"));

        verify(clienteServices).search("Alexandre", null);
    }

    @Test
    void search_comStatusNaQuery_statusEIgnorado() throws Exception {
        when(clienteServices.search("Alexandre", null)).thenReturn(List.of());

        mockMvc.perform(get("/customers/search")
                        .param("name", "Alexandre")
                        .param("status", "ACTIVE")
                        .with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk());

        verify(clienteServices).search("Alexandre", null);
        verify(clienteServices, never()).search(anyString(), eq("ACTIVE"));
    }

    @Test
    void search_semParametros_buscaTudo() throws Exception {
        when(clienteServices.search(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/customers/search").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk());

        verify(clienteServices).search(null, null);
    }

    // ---------------------------------------------------------------
    // GET /customers/{id}
    // ---------------------------------------------------------------

    @Test
    void findById_existente_retorna200() throws Exception {
        when(clienteServices.findById(1L)).thenReturn(cliente(1L));

        mockMvc.perform(get("/customers/1").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findById_inexistente_retorna404() throws Exception {
        when(clienteServices.findById(999L)).thenThrow(new ResourceNotFoundException("Cliente not found!"));

        mockMvc.perform(get("/customers/999").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente not found!"));
    }

    // ---------------------------------------------------------------
    // GET /customers/{id}/score
    // ---------------------------------------------------------------

    @Test
    void getScore_existente_retorna200() throws Exception {
        when(clienteServices.getScore(1L)).thenReturn(new CustomerScoreDTO(1L, CPF_VALIDO, 750, "LOW_RISK"));

        mockMvc.perform(get("/customers/1/score").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.score").value(750))
                .andExpect(jsonPath("$.classification").value("LOW_RISK"));
    }

    @Test
    void getScore_clienteInexistente_retorna404() throws Exception {
        when(clienteServices.getScore(999L)).thenThrow(new ResourceNotFoundException("Cliente not found!"));

        mockMvc.perform(get("/customers/999/score").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getScore_servicoExternoIndisponivel_retorna502() throws Exception {
        when(clienteServices.getScore(1L))
                .thenThrow(new ExternalServiceException("Serviço de score indisponível no momento", new RuntimeException()));

        mockMvc.perform(get("/customers/1/score").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void getScore_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/customers/1/score"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(clienteServices);
    }

    // ---------------------------------------------------------------
    // POST /customers
    // ---------------------------------------------------------------

    @Test
    void create_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_comTokenUser_retorna403() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_comTokenAdmin_retorna200ComClienteCriado() throws Exception {
        when(clienteServices.create(any(ClienteDTO.class))).thenReturn(cliente(1L));

        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(clienteServices).create(any(ClienteDTO.class));
    }

    @Test
    void create_cpfInvalido_retorna400SemChamarService() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_INVALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("CPF")));

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_nomeEmBranco_retorna400() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_emailEmBranco_retorna400() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "", "ACTIVE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_corpoAusente_retorna400NaoQuebra() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void create_statusInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "FOO")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteServices);
    }

    // ---------------------------------------------------------------
    // PUT /customers/{id}
    // ---------------------------------------------------------------

    @Test
    void update_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void update_comTokenUser_retorna403() throws Exception {
        mockMvc.perform(put("/customers/1")
                        .with(jwt().authorities(ROLE_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void update_comTokenAdmin_retorna200() throws Exception {
        when(clienteServices.update(eq(1L), any(ClienteDTO.class))).thenReturn(cliente(1L));

        mockMvc.perform(put("/customers/1")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(clienteServices).update(eq(1L), any(ClienteDTO.class));
    }

    @Test
    void update_clienteInexistente_retorna404() throws Exception {
        when(clienteServices.update(eq(999L), any(ClienteDTO.class)))
                .thenThrow(new ResourceNotFoundException("Cliente not found!"));

        mockMvc.perform(put("/customers/999")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_VALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_cpfInvalido_retorna400() throws Exception {
        mockMvc.perform(put("/customers/1")
                        .with(jwt().authorities(ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("Alexandre Martins", CPF_INVALIDO, "a@a.com", "ACTIVE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteServices);
    }

    // ---------------------------------------------------------------
    // DELETE /customers/{id}
    // ---------------------------------------------------------------

    @Test
    void delete_semToken_retorna401() throws Exception {
        mockMvc.perform(delete("/customers/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void delete_comTokenUser_retorna403() throws Exception {
        mockMvc.perform(delete("/customers/1").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(clienteServices);
    }

    @Test
    void delete_comTokenAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/customers/1").with(jwt().authorities(ROLE_ADMIN)))
                .andExpect(status().isNoContent());

        verify(clienteServices).delete(1L);
    }

    @Test
    void delete_clienteInexistente_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente not found!")).when(clienteServices).delete(999L);

        mockMvc.perform(delete("/customers/999").with(jwt().authorities(ROLE_ADMIN)))
                .andExpect(status().isNotFound());
    }
}
