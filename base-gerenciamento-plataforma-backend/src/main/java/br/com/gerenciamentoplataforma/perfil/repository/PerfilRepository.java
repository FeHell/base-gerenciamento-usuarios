package br.com.gerenciamentoplataforma.perfil.repository;

import br.com.gerenciamentoplataforma.perfil.entity.Perfil;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByIdAndIdAplicacao(UUID id, UUID idAplicacao);

    Page<Perfil> findByIdAplicacao(UUID idAplicacao, Pageable pagina);
}
