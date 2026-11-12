package br.com.gerenciamentoplataforma.usuario.repository;

import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPermissaoPlataforma;
import br.com.gerenciamentoplataforma.usuario.entity.UsuarioPermissaoPlataformaId;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioPermissaoPlataformaRepository
        extends JpaRepository<UsuarioPermissaoPlataforma, UsuarioPermissaoPlataformaId> {

    String PERMISSOES_ATRIBUIDAS = """
            from UsuarioPermissaoPlataforma concessao
            join Permissao permissao on permissao.id = concessao.id.idPermissao
            where permissao.nomePlataforma is not null
            """;

    List<UsuarioPermissaoPlataforma> findByIdIdUsuario(UUID idUsuario);

    @Query("select concessao.id.idUsuario as idUsuario, permissao.codigo as codigo " + PERMISSOES_ATRIBUIDAS + """
            and concessao.id.idUsuario in :usuarios
            order by permissao.codigo
            """)
    List<Atribuicao> listarAtribuicoes(List<UUID> usuarios);

    @Query("select permissao.codigo " + PERMISSOES_ATRIBUIDAS + """
            and concessao.id.idUsuario = :idUsuario
            and permissao.status = br.com.gerenciamentoplataforma.common.StatusCadastro.ATIVO
            order by permissao.codigo
            """)
    List<String> listarCodigosAtivos(UUID idUsuario);

    interface Atribuicao {
        UUID getIdUsuario();
        String getCodigo();
    }
}
