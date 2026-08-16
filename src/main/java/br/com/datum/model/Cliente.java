package br.com.datum.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_cliente")
public class Cliente implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private boolean status = true;

    @Column(name = "dt_cadastro", nullable = false)
    @CreationTimestamp
    private LocalDateTime dtCadastro;

    @Column(name = "dt_ult_atualizacao", nullable = false)
    @UpdateTimestamp
    private LocalDateTime dtUltAtualizacao;


    public Cliente() {}

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
        if (!(o instanceof Cliente cliente)) return false;
        return status == cliente.status && Objects.equals(id, cliente.id) && Objects.equals(nome, cliente.nome) && Objects.equals(cpf, cliente.cpf) && Objects.equals(email, cliente.email) && Objects.equals(dtCadastro, cliente.dtCadastro) && Objects.equals(dtUltAtualizacao, cliente.dtUltAtualizacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, cpf, email, status, dtCadastro, dtUltAtualizacao);
    }
}
