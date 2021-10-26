package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceJpaRepository extends JpaRepository<Service, Integer> {

    @Query("SELECT s FROM Service s WHERE s.id = :id")
    Service findById(int id);

    @Query("SELECT s FROM Service s WHERE s.structure.id = :idStructure")
    List<Service> findByStructure(int idStructure);
}
