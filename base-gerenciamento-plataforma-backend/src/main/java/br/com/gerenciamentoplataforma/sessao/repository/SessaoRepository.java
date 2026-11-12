package br.com.gerenciamentoplataforma.sessao.repository;

import br.com.gerenciamentoplataforma.sessao.entity.Sessao;
import br.com.gerenciamentoplataforma.sessao.entity.StatusSessao;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sessao s where s.id = :id")
    Optional<Sessao> bloquearPorId(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Sessao> findByIdAndIdAplicacao(UUID id, UUID idAplicacao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Sessao> findByIdUsuarioAndStatusOrderById(UUID idUsuario, StatusSessao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Sessao> findByIdAplicacaoAndStatusOrderById(UUID idAplicacao, StatusSessao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Sessao> findByIdUsuarioAndIdAplicacaoAndStatusOrderById(UUID idUsuario, UUID idAplicacao,
                                                               StatusSessao status);

    Page<Sessao> findByIdAplicacao(UUID idAplicacao, Pageable pagina);
}
