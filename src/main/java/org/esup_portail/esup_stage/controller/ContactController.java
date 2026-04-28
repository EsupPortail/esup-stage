package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ContactDto;
import org.esup_portail.esup_stage.dto.ContactFormDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Civilite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Service;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.CiviliteJpaRepository;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ContactRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
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
    public PaginatedResponse<ContactDto> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PaginatedResponse<ContactDto> paginatedResponse = new PaginatedResponse<>();
        if (!isGestionnaire(utilisateur)) {
            paginatedResponse.setTotal(contactRepository.count(filters));
            paginatedResponse.setData(toDtoList(contactRepository.findPaginated(page, perPage, predicate, sortOrder, filters), shouldHideSensitiveContactFields(utilisateur)));
            return paginatedResponse;
        }

        List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
        if (centresDemandeur.isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
        }

        List<Integer> centreIds = centresDemandeur.stream().map(CentreGestion::getId).toList();
        paginatedResponse.setTotal(contactRepository.countVisibleForCentres(centreIds, filters));
        paginatedResponse.setData(toDtoList(contactRepository.findPaginatedVisibleForCentres(centreIds, page, perPage, predicate, sortOrder, filters), false));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public ContactDto getById(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Contact contact;
        if (isGestionnaire(utilisateur)) {
            List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
            if (centresDemandeur.isEmpty()) {
                throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
            }
            List<Integer> centreIds = centresDemandeur.stream().map(CentreGestion::getId).toList();
            contact = contactJpaRepository.findVisibleByIdForCentres(id, centreIds);
        } else {
            contact = contactJpaRepository.findById(id);
        }
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }

        return ContactDto.from(contact, shouldHideSensitiveContactFields(utilisateur));
    }

    @GetMapping("/getByService/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public List<ContactDto> getByService(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (isGestionnaire(utilisateur)) {
            List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
            if (centresDemandeur.isEmpty()) {
                throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
            }
            List<Integer> centreIds = centresDemandeur.stream().map(CentreGestion::getId).toList();
            return toDtoList(contactJpaRepository.findByServiceVisibleForCentres(id, centreIds), false);
        }

        List<Contact> contacts = contactJpaRepository.findByService(id);

        if (contacts == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouvé");
        }

        return toDtoList(contacts, shouldHideSensitiveContactFields(utilisateur));
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
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.MODIFICATION}, evaluator = ContactPermissionEvaluator.class)
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

    private boolean isGestionnaire(Utilisateur utilisateur) {
        return UtilisateurHelper.isRole(utilisateur, Role.GES)
                || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES);
    }

    private List<CentreGestion> getCurrentGestionnaireCentres(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getUid() == null || utilisateur.getUid().isBlank()) {
            return new ArrayList<>();
        }
        return centreGestionJpaRepository.findAllByGestionnaireUid(utilisateur.getUid());
    }

    private List<ContactDto> toDtoList(List<Contact> contacts, boolean hideSensitiveFields) {
        return contacts.stream()
                .map(contact -> ContactDto.from(contact, hideSensitiveFields))
                .toList();
    }

    private boolean shouldHideSensitiveContactFields(Utilisateur utilisateur) {
        return UtilisateurHelper.isRole(utilisateur, Role.ETU)
                || UtilisateurHelper.isRole(utilisateur, Role.ENS);
    }

}
