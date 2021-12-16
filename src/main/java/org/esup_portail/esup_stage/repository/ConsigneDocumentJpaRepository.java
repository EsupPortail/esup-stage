package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ConsigneDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsigneDocumentJpaRepository extends JpaRepository<ConsigneDocument, Integer> {

    ConsigneDocument findById(int id);
}
