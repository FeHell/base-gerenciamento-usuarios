package br.com.gerenciamentoplataforma.usuario.entity;

import br.com.gerenciamentoplataforma.aplicacao.entity.Aplicacao;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "usuario_aplicacao")
public class UsuarioAplicacao {
    @EmbeddedId
    private UsuarioAplicacaoId id;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", insertable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aplicacao", referencedColumnName = "id", insertable = false, updatable = false)
    private Aplicacao aplicacao;

    protected UsuarioAplicacao() {
    }

    public UsuarioAplicacao(UsuarioAplicacaoId id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public UsuarioAplicacaoId getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Aplicacao getAplicacao() {
        return aplicacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void definirAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
