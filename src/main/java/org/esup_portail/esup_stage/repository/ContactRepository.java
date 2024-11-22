package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Contact;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

@Repository
public class ContactRepository extends PaginationRepository<Contact> {
    public ContactRepository(EntityManager em) {
        super(em, Contact.class, "c");
    }

}
