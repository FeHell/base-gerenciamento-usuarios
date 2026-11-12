package br.com.gerenciamentoplataforma.aplicacao.repository;

import br.com.gerenciamentoplataforma.aplicacao.entity.Aplicacao;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AplicacaoRepository extends JpaRepository<Aplicacao, UUID> {

    Optional<Aplicacao> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select a from Aplicacao a where a.slug = :slug")
    Optional<Aplicacao> buscarSlugParaLogin(@Param("slug") String slug);

    // Compatível com os bloqueios de FK usados pela auditoria durante o encerramento de sessões.
    @Query(value = "select a.* from aplicacao a where a.slug = :slug for no key update", nativeQuery = true)
    Optional<Aplicacao> bloquearPorSlug(@Param("slug") String slug);

    @Query(value = "select a.* from aplicacao a where a.id = :id for no key update", nativeQuery = true)
    Optional<Aplicacao> bloquearPorId(@Param("id") UUID id);
}
