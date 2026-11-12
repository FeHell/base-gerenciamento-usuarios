package br.com.gerenciamentoplataforma.aplicacao.repository;

import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissao;
import br.com.gerenciamentoplataforma.aplicacao.entity.AplicacaoPermissaoId;
import br.com.gerenciamentoplataforma.permissao.entity.Permissao;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AplicacaoPermissaoRepository extends JpaRepository<AplicacaoPermissao, AplicacaoPermissaoId> {

    @Query("select ap.permissao from AplicacaoPermissao ap where ap.id.idAplicacao = :id order by ap.permissao.codigo")
    List<Permissao> listarPermissoes(@Param("id") UUID idAplicacao);
}
