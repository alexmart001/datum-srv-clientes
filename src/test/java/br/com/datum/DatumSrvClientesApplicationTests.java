package br.com.datum;

import br.com.datum.config.SecurityConfig;
import br.com.datum.controllers.ClientController;
import br.com.datum.services.ClienteServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test mínimo: confirma que o contexto MVC (controller + segurança)
 * sobe corretamente, sem depender de banco de dados, RabbitMQ ou do
 * Authorization Server reais - ClienteServices e JwtDecoder são
 * mockados. A cobertura de comportamento fica em ClientControllerTest.
 */
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class DatumSrvClientesApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteServices clienteServices;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

}
