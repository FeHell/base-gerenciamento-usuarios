package br.com.gerenciamentoplataforma.administracao.bootstrap;

import br.com.gerenciamentoplataforma.administracao.service.AcessoAdministrativo;
import br.com.gerenciamentoplataforma.administracao.service.SenhasAdministrativas;
import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissao;
import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissaoId;
import br.com.gerenciamentoplataforma.aplicacao.repository.AplicacaoPermissaoRepository;
import br.com.gerenciamentoplataforma.aplicacao.repository.AplicacaoRepository;
import br.com.gerenciamentoplataforma.auditoria.entity.Auditoria;
import br.com.gerenciamentoplataforma.auditoria.repository.AuditoriaRepository;
import br.com.gerenciamentoplataforma.common.StatusCadastro;
import br.com.gerenciamentoplataforma.perfil.entity.Perfil;
import br.com.gerenciamentoplataforma.perfil.entity.PerfilPermissao;
import br.com.gerenciamentoplataforma.perfil.entity.PerfilPermissaoId;
import br.com.gerenciamentoplataforma.perfil.repository.PerfilPermissaoRepository;
import br.com.gerenciamentoplataforma.perfil.repository.PerfilRepository;
import br.com.gerenciamentoplataforma.permissao.entity.Permissao;
import br.com.gerenciamentoplataforma.permissao.repository.PermissaoRepository;
import br.com.gerenciamentoplataforma.usuario.entity.StatusUsuario;
import br.com.gerenciamentoplataforma.usuario.entity.Usuario;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioAplicacao;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioAplicacaoId;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPerfil;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPerfilId;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioAplicacaoRepository;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioPerfilRepository;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.validation.Validator;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gerenciamentoplataforma.administracao.dto.ContratosAdministrativos.NovoUsuario;

@Service
@Profile("bootstrap-admin")
public class InicializacaoAdministrativa {

    public static final String SLUG = "plataforma-admin";

    private final JdbcTemplate jdbc;
    private final AplicacaoRepository aplicacoes;
    private final UsuarioRepository usuarios;
    private final PermissaoRepository permissoes;
    private final AplicacaoPermissaoRepository habilitacoes;
    private final PerfilRepository perfis;
    private final PerfilPermissaoRepository permissoesPerfil;
    private final UsuarioAplicacaoRepository vinculos;
    private final UsuarioPerfilRepository atribuicoes;
    private final AuditoriaRepository auditoria;
    private final SenhasAdministrativas senhas;
    private final Validator validador;

    public InicializacaoAdministrativa(JdbcTemplate jdbc, AplicacaoRepository aplicacoes, UsuarioRepository usuarios,
                                       PermissaoRepository permissoes, AplicacaoPermissaoRepository habilitacoes,
                                       PerfilRepository perfis, PerfilPermissaoRepository permissoesPerfil,
                                       UsuarioAplicacaoRepository vinculos, UsuarioPerfilRepository atribuicoes,
                                       AuditoriaRepository auditoria, SenhasAdministrativas senhas, Validator validador) {
        this.jdbc = jdbc;
        this.aplicacoes = aplicacoes;
        this.usuarios = usuarios;
        this.permissoes = permissoes;
        this.habilitacoes = habilitacoes;
        this.perfis = perfis;
        this.permissoesPerfil = permissoesPerfil;
        this.vinculos = vinculos;
        this.atribuicoes = atribuicoes;
        this.auditoria = auditoria;
        this.senhas = senhas;
        this.validador = validador;
    }

    @Transactional
    public void criarPrimeiroAdministrador(UUID idAplicacao, NovoUsuario dados) {
        if (!validador.validate(dados).isEmpty()) {
            throw new IllegalArgumentException("Informe login, e-mail e senha válidos para o primeiro administrador.");
        }
        if (aplicacoes.existsById(idAplicacao) || aplicacoes.findBySlug(SLUG).isPresent()
                || usuarios.buscarPorLogin(dados.login().strip()).isPresent()
                || usuarios.buscarPorEmail(dados.email().strip()).isPresent()
                || permissoes.findByCodigo(AcessoAdministrativo.PLATAFORMA_ADMINISTRAR).isPresent()) {
            throw new IllegalStateException("A aplicação, a conta ou a permissão administrativa já existe.");
        }

        String hash = senhas.gerarHash(dados.senha());
        jdbc.update("""
                insert into aplicacao (id, nome, slug, tipo, status)
                values (?, 'Administração da plataforma', ?, 'PUBLICO', 'ATIVO')
                """, idAplicacao, SLUG);
        var usuario = usuarios.saveAndFlush(new Usuario(dados.login().strip(), hash, dados.email().strip(),
                StatusUsuario.ATIVO));
        var permissao = permissoes.saveAndFlush(new Permissao(AcessoAdministrativo.PLATAFORMA_ADMINISTRAR,
                "Administrar os cadastros globais e as aplicações da plataforma.", StatusCadastro.ATIVO));
        if (permissoes.findByCodigo(AcessoAdministrativo.APLICACAO_ADMINISTRAR).isEmpty()) {
            permissoes.save(new Permissao(AcessoAdministrativo.APLICACAO_ADMINISTRAR,
                    "Administrar perfis, vínculos e sessões da aplicação.", StatusCadastro.ATIVO));
        }
        var perfil = perfis.saveAndFlush(new Perfil(idAplicacao, "Administrador da plataforma",
                null, StatusCadastro.ATIVO));
        habilitacoes.saveAndFlush(new AplicacaoPermissao(new AplicacaoPermissaoId(idAplicacao, permissao.getId())));
        permissoesPerfil.save(new PerfilPermissao(new PerfilPermissaoId(idAplicacao, perfil.getId(), permissao.getId())));
        vinculos.saveAndFlush(new UsuarioAplicacao(new UsuarioAplicacaoId(usuario.getId(), idAplicacao)));
        atribuicoes.save(new UsuarioPerfil(new UsuarioPerfilId(usuario.getId(), idAplicacao, perfil.getId())));
        var evento = JsonNodeFactory.instance.objectNode().put("idRecurso", usuario.getId().toString());
        auditoria.save(new Auditoria(null, idAplicacao, "CRIAR_PRIMEIRO_ADMINISTRADOR",
                "usuario", null, evento, null));
    }
}
