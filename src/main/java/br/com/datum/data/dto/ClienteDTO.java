package br.com.datum.data.dto;

import br.com.datum.serializer.StatusSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ClienteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String cpf;
    private String email;

    @JsonSerialize(using = StatusSerializer.class)
    private boolean status;

    private LocalDateTime dtCadastro;
    private LocalDateTime dtUltAtualizacao;

    public ClienteDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }

    public void setDtCadastro(LocalDateTime dtCadastro) {
        this.dtCadastro = dtCadastro;
    }

    public LocalDateTime getDtUltAtualizacao() {
        return dtUltAtualizacao;
    }

    public void setDtUltAtualizacao(LocalDateTime dtUltAtualizacao) {
        this.dtUltAtualizacao = dtUltAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClienteDTO that)) return false;
        return status == that.status && Objects.equals(id, that.id) && Objects.equals(nome, that.nome) && Objects.equals(cpf, that.cpf) && Objects.equals(email, that.email) && Objects.equals(dtCadastro, that.dtCadastro) && Objects.equals(dtUltAtualizacao, that.dtUltAtualizacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, cpf, email, status, dtCadastro, dtUltAtualizacao);
    }
}
