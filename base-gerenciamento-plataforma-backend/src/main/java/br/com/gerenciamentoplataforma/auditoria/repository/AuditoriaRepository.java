package br.com.gerenciamentoplataforma.auditoria.repository;

import br.com.gerenciamentoplataforma.auditoria.entity.Auditoria;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findByIdAplicacao(UUID idAplicacao, Pageable pagina);
}
