package br.com.gerenciamentoplataforma.usuario.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_permissao_plataforma")
public class UsuarioPermissaoPlataforma {
    @EmbeddedId
    private UsuarioPermissaoPlataformaId id;

    protected UsuarioPermissaoPlataforma() {
    }

    public UsuarioPermissaoPlataforma(UsuarioPermissaoPlataformaId id) {
        this.id = id;
    }

    public UsuarioPermissaoPlataformaId getId() {
        return id;
    }
}
