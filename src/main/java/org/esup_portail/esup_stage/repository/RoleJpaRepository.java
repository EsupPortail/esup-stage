package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleJpaRepository extends JpaRepository<Role, Integer> {

    Role findOneByCode(String code);

    Role findById(int id);

    @Query("SELECT DISTINCT r FROM Role r WHERE r.code IN ('GES', 'RESP_GES', 'ENS') ORDER BY r.libelle")
    List<Role> findAssignableCentreRoles();
}
