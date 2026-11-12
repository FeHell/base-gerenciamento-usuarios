package br.com.gerenciamentoplataforma.usuario.repository;

import br.com.gerenciamentoplataforma.usuario.entity.Usuario;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Query("select u from Usuario u where lower(u.login) = lower(:login)")
    Optional<Usuario> buscarPorLogin(@Param("login") String login);

    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select u from Usuario u where lower(u.login) = lower(:login)")
    Optional<Usuario> buscarLoginParaAutenticacao(@Param("login") String login);

    // Compatível com os bloqueios de FK usados pela auditoria durante o encerramento de sessões.
    @Query(value = "select u.* from usuario u where u.id = :id for no key update", nativeQuery = true)
    Optional<Usuario> bloquearPorId(@Param("id") UUID id);
}
