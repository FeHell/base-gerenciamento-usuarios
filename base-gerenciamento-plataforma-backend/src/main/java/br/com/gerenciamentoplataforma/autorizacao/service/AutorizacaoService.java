package br.com.gerenciamentoplataforma.autorizacao.service;

import br.com.gerenciamentoplataforma.autenticacao.exception.AutenticacaoNegadaException;
import br.com.gerenciamentoplataforma.autenticacao.service.AutenticacaoService;
import br.com.gerenciamentoplataforma.autenticacao.service.IdentidadeJwt;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioPerfilRepository;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("autorizacao")
public class AutorizacaoService {

    private final AutenticacaoService autenticacao;
    private final UsuarioPerfilRepository perfis;

    public AutorizacaoService(AutenticacaoService autenticacao, UsuarioPerfilRepository perfis) {
        this.autenticacao = autenticacao;
        this.perfis = perfis;
    }

    @Transactional(readOnly = true)
    public List<String> listarPermissoes(String slug, IdentidadeJwt identidade) {
        return consultarPermissoes(slug, identidade);
    }

    @Transactional(readOnly = true)
    public boolean temPermissao(Authentication autenticacaoAtual, String slug, String codigo) {
        if (autenticacaoAtual == null || !autenticacaoAtual.isAuthenticated()
                || !(autenticacaoAtual.getPrincipal() instanceof IdentidadeJwt identidade)
                || codigo == null || codigo.isBlank()) {
            return false;
        }
        try {
            return consultarPermissoes(slug, identidade).contains(codigo);
        } catch (AutenticacaoNegadaException erro) {
            return false;
        }
    }

    private List<String> consultarPermissoes(String slug, IdentidadeJwt identidade) {
        if (slug == null || identidade == null) {
            throw new AutenticacaoNegadaException();
        }
        var usuario = autenticacao.consultarUsuario(slug, identidade);
        return perfis.buscarPermissoesEfetivas(usuario.id(), usuario.idAplicacao());
    }
}
