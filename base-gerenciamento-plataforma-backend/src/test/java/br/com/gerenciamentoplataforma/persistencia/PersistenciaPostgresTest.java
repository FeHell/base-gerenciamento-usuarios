package br.com.gerenciamentoplataforma.persistencia;

import br.com.gerenciamentoplataforma.aplicacao.entity.Aplicacao;
import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissao;
import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissaoId;
import br.com.gerenciamentoplataforma.aplicacao.entity.TipoAplicacao;
import br.com.gerenciamentoplataforma.aplicacao.repository.AplicacaoRepository;
import br.com.gerenciamentoplataforma.auditoria.entity.Auditoria;
import br.com.gerenciamentoplataforma.common.StatusCadastro;
import br.com.gerenciamentoplataforma.perfil.entity.Perfil;
import br.com.gerenciamentoplataforma.perfil.entity.PerfilPermissao;
import br.com.gerenciamentoplataforma.perfil.entity.PerfilPermissaoId;
import br.com.gerenciamentoplataforma.perfil.repository.PerfilPermissaoRepository;
import br.com.gerenciamentoplataforma.perfil.repository.PerfilRepository;
import br.com.gerenciamentoplataforma.permissao.entity.Permissao;
import br.com.gerenciamentoplataforma.sessao.entity.Sessao;
import br.com.gerenciamentoplataforma.sessao.entity.StatusSessao;
import br.com.gerenciamentoplataforma.sessao.entity.TokenAtualizacao;
import br.com.gerenciamentoplataforma.sessao.repository.TokenAtualizacaoRepository;
import br.com.gerenciamentoplataforma.usuario.entity.StatusUsuario;
import br.com.gerenciamentoplataforma.usuario.entity.Usuario;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioAplicacao;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioAplicacaoId;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPerfil;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPerfilId;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioPerfilRepository;
import br.com.gerenciamentoplataforma.usuario.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(showSql = false, properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=public"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "TESTE_PERSISTENCIA_POSTGRES", matches = "true")
class PersistenciaPostgresTest {

    @DynamicPropertySource
    static void configurarConexao(DynamicPropertyRegistry propriedades) {
        propriedades.add("spring.datasource.url",
                () -> "jdbc:postgresql://127.0.0.1:5432/gerenciamento_usuarios");
        propriedades.add("spring.datasource.username", () -> System.getenv("DATABASE_USERNAME"));
        propriedades.add("spring.datasource.password", () -> System.getenv().getOrDefault("DATABASE_PASSWORD", ""));
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TestEntityManager entidades;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private AplicacaoRepository aplicacoes;

    @Autowired
    private PerfilRepository perfis;

    @Autowired
    private PerfilPermissaoRepository permissoesDosPerfis;

    @Autowired
    private UsuarioPerfilRepository perfisDosUsuarios;

    @Autowired
    private TokenAtualizacaoRepository tokens;

    @BeforeEach
    void confirmarBanco() {
        assertThat(jdbc.queryForObject("select current_database()", String.class))
                .isEqualTo("gerenciamento_usuarios");
    }

    @Test
    void persisteVinculosSessaoCadeiaDeTokensEAuditoria() {
        Usuario usuario = criarUsuario();
        Aplicacao aplicacao = criarAplicacao();
        Permissao permissao = entidades.persist(new Permissao(
                "TESTE_" + UUID.randomUUID(), null, StatusCadastro.ATIVO));
        Perfil perfil = criarPerfil(aplicacao);
        var usuarioAplicacaoId = new UsuarioAplicacaoId(usuario.getId(), aplicacao.getId());
        var aplicacaoPermissaoId = new AplicacaoPermissaoId(aplicacao.getId(), permissao.getId());
        var perfilPermissaoId = new PerfilPermissaoId(aplicacao.getId(), perfil.getId(), permissao.getId());
        var usuarioPerfilId = new UsuarioPerfilId(usuario.getId(), aplicacao.getId(), perfil.getId());

        entidades.persist(new UsuarioAplicacao(usuarioAplicacaoId));
        entidades.persist(new AplicacaoPermissao(aplicacaoPermissaoId));
        entidades.persist(new PerfilPermissao(perfilPermissaoId));
        entidades.persist(new UsuarioPerfil(usuarioPerfilId));
        Sessao sessao = criarSessao(usuario, aplicacao);
        TokenAtualizacao inicial = criarToken(sessao, null);
        TokenAtualizacao sucessor = criarToken(sessao, inicial.getId());

        var valor = JsonNodeFactory.instance.objectNode().put("status", "ATIVO");
        Auditoria evento = entidades.persistAndFlush(new Auditoria(
                null, null, "TESTE_PERSISTENCIA", "usuario", null, valor, "::1"));
        entidades.clear();

        assertThat(aplicacoes.findBySlug(aplicacao.getSlug())).isPresent();
        assertThat(perfis.findByIdAndIdAplicacao(perfil.getId(), aplicacao.getId())).isPresent();
        assertThat(perfisDosUsuarios.findById(usuarioPerfilId).orElseThrow()
                .getUsuarioAplicacao().getUsuario().getLogin()).isEqualTo(usuario.getLogin());
        assertThat(permissoesDosPerfis.findById(perfilPermissaoId).orElseThrow()
                .getAplicacaoPermissao().getPermissao().getCodigo()).isEqualTo(permissao.getCodigo());
        assertThat(permissoesDosPerfis.findById(perfilPermissaoId).orElseThrow()
                .getPerfil().getNome()).isEqualTo(perfil.getNome());
        TokenAtualizacao token = tokens.findByHashToken(sucessor.getHashToken()).orElseThrow();
        assertThat(token.getSessao().getUsuarioAplicacao().getId()).isEqualTo(usuarioAplicacaoId);
        assertThat(token.getAnterior().getHashToken()).isEqualTo(inicial.getHashToken());
        assertThat(token.getDataCriacao()).isNotNull();
        assertThat(token.isRevogado()).isFalse();

        Auditoria registrada = entidades.find(Auditoria.class, evento.getId());
        assertThat(registrada.getValorNovo()).isEqualTo(valor);
        assertThat(registrada.getDataRegistro()).isNotNull();
        assertThat(registrada.getIdAplicacao()).isNull();
    }

    @Test
    void consultaIdentidadeSemDistinguirCaixaERejeitaLoginDuplicado() {
        Usuario usuario = criarUsuario();
        String loginMaiusculo = usuario.getLogin().toUpperCase(Locale.ROOT);
        String emailMaiusculo = usuario.getEmail().toUpperCase(Locale.ROOT);
        assertThat(usuarios.buscarPorLogin(loginMaiusculo).orElseThrow().getId()).isEqualTo(usuario.getId());
        assertThat(usuarios.buscarPorEmail(emailMaiusculo).orElseThrow().getId()).isEqualTo(usuario.getId());

        verificarViolacao(() -> usuarios.saveAndFlush(new Usuario(
                loginMaiusculo, "hash-teste", UUID.randomUUID() + "@example.test", StatusUsuario.ATIVO)),
                "23505", "idx_usuario_login_lower");
    }

    @Test
    void rejeitaPerfilDeOutraAplicacaoMesmoComVinculoExistente() {
        Usuario usuario = criarUsuario();
        Aplicacao aplicacao = criarAplicacao();
        Perfil perfilDeOutraAplicacao = criarPerfil(criarAplicacao());
        entidades.persist(new UsuarioAplicacao(new UsuarioAplicacaoId(usuario.getId(), aplicacao.getId())));
        entidades.flush();

        var vinculo = new UsuarioPerfilId(usuario.getId(), aplicacao.getId(), perfilDeOutraAplicacao.getId());

        verificarViolacao(() -> perfisDosUsuarios.saveAndFlush(new UsuarioPerfil(vinculo)),
                "23503", "fk_usuario_perfil_perfil");
    }

    @Test
    void rejeitaPermissaoNaoHabilitadaNaAplicacaoDoPerfil() {
        Aplicacao aplicacao = criarAplicacao();
        Aplicacao outraAplicacao = criarAplicacao();
        Perfil perfil = criarPerfil(aplicacao);
        Permissao permissao = entidades.persist(new Permissao(
                "TESTE_" + UUID.randomUUID(), null, StatusCadastro.ATIVO));
        entidades.persist(new AplicacaoPermissao(new AplicacaoPermissaoId(outraAplicacao.getId(), permissao.getId())));
        entidades.flush();

        var vinculo = new PerfilPermissaoId(aplicacao.getId(), perfil.getId(), permissao.getId());

        verificarViolacao(() -> permissoesDosPerfis.saveAndFlush(new PerfilPermissao(vinculo)),
                "23503", "fk_perfil_permissao_aplicacao_permissao");
    }

    @Test
    void rejeitaPredecessorDeOutraFamilia() {
        Usuario usuario = criarUsuario();
        Aplicacao aplicacao = criarAplicacao();
        entidades.persist(new UsuarioAplicacao(new UsuarioAplicacaoId(usuario.getId(), aplicacao.getId())));
        Sessao sessao = criarSessao(usuario, aplicacao);
        Sessao outraSessao = criarSessao(usuario, aplicacao);
        TokenAtualizacao predecessor = criarToken(outraSessao, null);
        entidades.flush();

        var token = new TokenAtualizacao(
                sessao.getId(), sessao.getIdFamilia(), UUID.randomUUID().toString(),
                predecessor.getId(), sessao.getExpiracaoAbsoluta());

        verificarViolacao(() -> tokens.saveAndFlush(token), "23503", "fk_token_anterior");
    }

    private Usuario criarUsuario() {
        String identidade = UUID.randomUUID().toString();
        return entidades.persist(new Usuario(identidade, "hash-teste", identidade + "@example.test",
                StatusUsuario.ATIVO));
    }

    private Aplicacao criarAplicacao() {
        return entidades.persist(new Aplicacao("Aplicação de teste", UUID.randomUUID().toString(),
                TipoAplicacao.PUBLICO, null, StatusCadastro.ATIVO));
    }

    private Perfil criarPerfil(Aplicacao aplicacao) {
        return entidades.persist(new Perfil(aplicacao.getId(), "Perfil de teste", null, StatusCadastro.ATIVO));
    }

    private Sessao criarSessao(Usuario usuario, Aplicacao aplicacao) {
        Instant agora = Instant.now();
        return entidades.persist(new Sessao(usuario.getId(), aplicacao.getId(), UUID.randomUUID(),
                StatusSessao.ATIVO, agora.plusSeconds(3600), agora.plusSeconds(1800), agora));
    }

    private TokenAtualizacao criarToken(Sessao sessao, UUID idAnterior) {
        return entidades.persist(new TokenAtualizacao(
                sessao.getId(), sessao.getIdFamilia(), UUID.randomUUID().toString(),
                idAnterior, sessao.getExpiracaoAbsoluta()));
    }

    private void verificarViolacao(Runnable operacao, String codigo, String restricao) {
        assertThatThrownBy(operacao::run)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(SQLException.class)
                .rootCause()
                .satisfies(causa -> {
                    assertThat(((SQLException) causa).getSQLState()).isEqualTo(codigo);
                    assertThat(causa).hasMessageContaining(restricao);
                });
    }
}
