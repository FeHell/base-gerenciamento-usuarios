package br.com.gerenciamentoplataforma.perfil.entity;

import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissao;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "perfil_permissao")
public class PerfilPermissao {
    @EmbeddedId
    private PerfilPermissaoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "id_aplicacao", referencedColumnName = "id_aplicacao", insertable = false, updatable = false),
        @JoinColumn(name = "id_perfil", referencedColumnName = "id_perfil", insertable = false, updatable = false)
    })
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "id_aplicacao", referencedColumnName = "id_aplicacao", insertable = false, updatable = false),
        @JoinColumn(name = "id_permissao", referencedColumnName = "id_permissao", insertable = false, updatable = false)
    })
    private AplicacaoPermissao aplicacaoPermissao;

    protected PerfilPermissao() {
    }

    public PerfilPermissao(PerfilPermissaoId id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public PerfilPermissaoId getId() {
        return id;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public AplicacaoPermissao getAplicacaoPermissao() {
        return aplicacaoPermissao;
    }
}
