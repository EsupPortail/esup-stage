package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactJpaRepository extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.id = :id")
    Contact findById(int id);

    @Query("SELECT c FROM Contact c WHERE c.service.id = :idService")
    List<Contact> findByService(int idService);
}
