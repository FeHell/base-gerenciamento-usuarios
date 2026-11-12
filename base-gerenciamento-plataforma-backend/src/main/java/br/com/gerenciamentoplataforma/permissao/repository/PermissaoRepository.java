package br.com.gerenciamentoplataforma.permissao.repository;

import br.com.gerenciamentoplataforma.permissao.entity.Permissao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {

    Optional<Permissao> findByCodigo(String codigo);
}
