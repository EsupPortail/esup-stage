package fr.dauphine.estage.repository;

import fr.dauphine.estage.enums.RoleEnum;
import fr.dauphine.estage.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findOneByCode(RoleEnum code);
}
