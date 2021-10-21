package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleJpaRepository extends JpaRepository<Role, Integer> {

    Role findOneByCode(String code);

    Role findById(int id);
}
