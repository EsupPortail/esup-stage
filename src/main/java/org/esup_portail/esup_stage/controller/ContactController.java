package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ContactFormDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    CiviliteJpaRepository civiliteJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Contact> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Contact> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contactRepository.count(filters));
        paginatedResponse.setData(contactRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public Contact getById(@PathVariable("id") int id) {
        Contact contact = contactJpaRepository.findById(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }
        return contact;
    }

    @GetMapping("/getByService/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public List<Contact> getByService(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        List<Contact> contacts = contactJpaRepository.findByService(id);

        if (contacts == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        /*
        * Si idCentreGestion != -1, on est dans le cadre d'une convention
        *
        * Dans ce cas pour les utilisateurs gestionnaires, on ne renvoit que les contacts qui sont rattachés au centre de la convention ou
          qui ont un centre de gestion avec code confidentialité égale à 0 ou au centre de gestion de type établissement
        * */
        if ((UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) && idCentreGestion != -1) {
            CentreGestion centreGestion = centreGestionJpaRepository.findById(idCentreGestion.intValue());
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
            }
            List<Contact> filteredContacts = new ArrayList<Contact>();
            for (Contact contact : contacts) {
                if (contact.getCentreGestion().getCodeConfidentialite().getCode().equals("0") || contact.getCentreGestion().getId() == centreGestion.getId() ||
                        contact.getCentreGestion().getNiveauCentre().getLibelle().equals("ETABLISSEMENT")) {
                    filteredContacts.add(contact);
                }
            }
            return filteredContacts;
        }

        return contacts;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.CREATION})
    public Contact create(@Valid @RequestBody ContactFormDto contactFormDto) {
        Contact contact = new Contact();
        setContactData(contact, contactFormDto);

        Service service = serviceJpaRepository.findById(contactFormDto.getIdService());
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouvé");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        CentreGestion centreGestion;
        //Ajoute le centreGestion de l'utilisateur qui a créé le contact pour les utilisateurs gestionnaires.
        if (UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
            if (contactFormDto.getIdCentreGestion() != null) {
                centreGestion = centreGestionJpaRepository.findById(contactFormDto.getIdCentreGestion().intValue());
            } else {
                centreGestion = centreGestionJpaRepository.getCentreEtablissement();
            }
        } else {
            centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        }
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
        }

        contact.setCentreGestion(centreGestion);
        contact.setService(service);

        return contactJpaRepository.saveAndFlush(contact);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.MODIFICATION})
    public Contact update(@PathVariable("id") int id, @Valid @RequestBody ContactFormDto contactFormDto) {
        Contact contact = contactJpaRepository.findById(id);
        setContactData(contact, contactFormDto);
        contact = contactJpaRepository.saveAndFlush(contact);
        return contact;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Contact contact = contactJpaRepository.findById(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }
        contactJpaRepository.delete(contact);
        contactJpaRepository.flush();
        return true;
    }

    private void setContactData(Contact contact, ContactFormDto contactFormDto) {
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }
        Civilite civilite = civiliteJpaRepository.findById(contactFormDto.getIdCivilite());

        contact.setNom(contactFormDto.getNom());
        contact.setPrenom(contactFormDto.getPrenom());
        contact.setCivilite(civilite);
        contact.setFonction(contactFormDto.getFonction());
        contact.setTel(contactFormDto.getTel());
        contact.setFax(contactFormDto.getFax());
        contact.setMail(contactFormDto.getMail());
    }
}
