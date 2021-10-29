package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ContactRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@ApiController
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Contact> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Contact> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contactRepository.count(filters));
        paginatedResponse.setData(contactRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public Contact getById(@PathVariable("id") int id) {
        Contact contact = contactJpaRepository.findById(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvée");
        }
        return contact;
    }

    @GetMapping("/getByService/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public List<Contact> getByService(@PathVariable("id") int id) {
        List<Contact> contact = contactJpaRepository.findByService(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvée");
        }
        return contact;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.CREATION})
    public Contact create(@Valid @RequestBody Contact _contact) {
        return contactJpaRepository.saveAndFlush(_contact);
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.MODIFICATION})
    public Contact update(@PathVariable("id") int id, @Valid @RequestBody Contact _contact) {
        Contact contact = contactJpaRepository.findById(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvée");
        }
        contact.setNom(_contact.getNom());
        contact.setPrenom(_contact.getPrenom());
        contact.setCivilite(_contact.getCivilite());
        contact.setFonction(_contact.getFonction());
        contact.setTel(_contact.getTel());
        contact.setFax(_contact.getFax());
        contact.setMail(_contact.getMail());
        contact = contactJpaRepository.saveAndFlush(contact);
        return contact;
    }

}
