package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleJpaRepository extends JpaRepository<Role, Integer> {

    Role findOneByCode(String code);

    Role findById(int id);
}
