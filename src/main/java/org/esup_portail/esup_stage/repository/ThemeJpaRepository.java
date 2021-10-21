package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeJpaRepository extends JpaRepository<Theme, Integer> {

    Theme findById(int id);
}
