package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationTuteurTokenJpaRepository extends JpaRepository<EvaluationTuteurToken, Integer> {

    EvaluationTuteurToken findById(int id);

    EvaluationTuteurToken findByToken(String token);

    @Query("""
            SELECT t
            FROM EvaluationTuteurToken t
            WHERE t.convention.id = :conventionId
              AND t.contact.id = :tuteurId
            ORDER BY t.createdAt DESC
            """)
    List<EvaluationTuteurToken> findByConventionIdAndTuteurId(@Param("conventionId") Integer conventionId,
                                                              @Param("tuteurId") Integer tuteurId);

    @Modifying
    @Query("DELETE FROM EvaluationTuteurToken t WHERE t.convention.id = :idConvention")
    int deleteByConventionId(@Param("idConvention") int idConvention);

    @Modifying
    @Query("DELETE FROM EvaluationTuteurToken t WHERE t.contact.id IN :ids")
    int deleteByContactIdIn(@Param("ids") List<Integer> ids);

    @Query("SELECT COUNT(t.id) FROM EvaluationTuteurToken t WHERE t.contact.service.structure.id = :idStructure")
    Long countByContactStructure(@Param("idStructure") int idStructure);
}
