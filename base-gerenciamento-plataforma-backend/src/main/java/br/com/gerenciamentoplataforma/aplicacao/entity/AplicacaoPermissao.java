package br.com.gerenciamentoplataforma.aplicacao.entity;

import br.com.gerenciamentoplataforma.permissao.entity.Permissao;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "aplicacao_permissao")
public class AplicacaoPermissao {
    @EmbeddedId
    private AplicacaoPermissaoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aplicacao", referencedColumnName = "id", insertable = false, updatable = false)
    private Aplicacao aplicacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permissao", referencedColumnName = "id", insertable = false, updatable = false)
    private Permissao permissao;

    protected AplicacaoPermissao() {
    }

    public AplicacaoPermissao(AplicacaoPermissaoId id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public AplicacaoPermissaoId getId() {
        return id;
    }

    public Aplicacao getAplicacao() {
        return aplicacao;
    }

    public Permissao getPermissao() {
        return permissao;
    }
}
