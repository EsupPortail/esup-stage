package org.esup_portail.esup_stage.controller;

import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ContactDetailDto;
import org.esup_portail.esup_stage.dto.ContactDto;
import org.esup_portail.esup_stage.dto.ContactFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.security.permission.ContactPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    CiviliteJpaRepository civiliteJpaRepository;

    @GetMapping("/getByService/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public List<ContactDto> getByService(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
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
            List<ContactDto> filteredContacts = new ArrayList<>();
            for (Contact contact : contacts) {
                if (contact.getCentreGestion().getCodeConfidentialite().getCode().equals("0") || contact.getCentreGestion().getId() == centreGestion.getId() ||
                        contact.getCentreGestion().getNiveauCentre().getLibelle().equals("ETABLISSEMENT")) {
                    filteredContacts.add(buildContactDto(contact));
                }
            }
            return filteredContacts;
        }

        return contacts.stream().map(this::buildContactDto).toList();
    }

    @GetMapping("/getByService/{id}/detail")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits ={ DroitEnum.MODIFICATION},forbiddenEtu = true)
    public List<ContactDetailDto> getByServiceWithDetail(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
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
            List<ContactDetailDto> filteredContacts = new ArrayList<>();
            for (Contact contact : contacts) {
                if (contact.getCentreGestion().getCodeConfidentialite().getCode().equals("0") || contact.getCentreGestion().getId() == centreGestion.getId() ||
                        contact.getCentreGestion().getNiveauCentre().getLibelle().equals("ETABLISSEMENT")) {
                    filteredContacts.add(buildContactDetailDto(contact));
                }
            }
            return filteredContacts;
        }

        return contacts.stream().map(this::buildContactDetailDto).toList();
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
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.MODIFICATION},evaluator = ContactPermissionEvaluator.class)
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

    private ContactDto buildContactDto(Contact contact) {
        ContactDto contactDto = new ContactDto();
        contactDto.setId(contact.getId());
        contactDto.setNom(contact.getNom());
        contactDto.setPrenom(contact.getPrenom());
        contactDto.setIdCentreGestion(contact.getCentreGestion().getId());
        return contactDto;
    }

    private ContactDetailDto buildContactDetailDto(Contact contact) {
        ContactDetailDto contactDetailDto = new ContactDetailDto();
        contactDetailDto.setId(contact.getId());
        contactDetailDto.setNom(contact.getNom());
        contactDetailDto.setPrenom(contact.getPrenom());
        contactDetailDto.setMail(contact.getMail());
        contactDetailDto.setTel(contact.getTel());
        contactDetailDto.setFonction(contact.getFonction());
        contactDetailDto.setFax(contact.getFax());
        contactDetailDto.setCivilite(contact.getCivilite());
        contactDetailDto.setIdCentreGestion(contact.getCentreGestion().getId());
        return contactDetailDto;
    }
}
