package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ContactDetailDto;
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
import org.esup_portail.esup_stage.service.ConfidentialiteService;
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

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ConfidentialiteService confidentialiteService;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ContactDetailDto> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PaginatedResponse<ContactDetailDto> paginatedResponse = new PaginatedResponse<>();
        List<Contact> contacts;

        if (isGestionnaire(utilisateur)) {
            List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
            if (centresDemandeur.isEmpty()) {
                throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
            }

            List<Integer> centreIds = centresDemandeur.stream().map(CentreGestion::getId).toList();
            paginatedResponse.setTotal(contactRepository.countVisibleForCentres(centreIds, filters));
            contacts = contactRepository.findPaginatedVisibleForCentres(centreIds, page, perPage, predicate, sortOrder, filters);
        } else {
            paginatedResponse.setTotal(contactRepository.count(filters));
            contacts = contactRepository.findPaginated(page, perPage, predicate, sortOrder, filters);
        }

        boolean hideSensitiveFields = shouldHideSensitiveContactFields(utilisateur);
        paginatedResponse.setData(contacts.stream().map(contact -> buildContactDetailDto(contact, hideSensitiveFields)).toList());
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.LECTURE})
    public ContactDetailDto getById(@PathVariable("id") int id) {
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
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
        }

        return buildContactDetailDto(contact, shouldHideSensitiveContactFields(utilisateur));
    }

    @GetMapping("/getByService/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC, AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<ContactDto> getByService(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        List<Contact> contacts = getVisibleContactsByService(id, idCentreGestion, utilisateur);
        return toDtoList(contacts, shouldHideSensitiveContactFields(utilisateur));
    }

    @GetMapping("/getByService/{id}/detail")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits ={ DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public List<ContactDetailDto> getByServiceWithDetail(@PathVariable("id") int id, @RequestParam(value = "idCentreGestion", required = false, defaultValue = "-1") Integer idCentreGestion) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        List<Contact> contacts = getVisibleContactsByService(id, idCentreGestion, utilisateur);
        return contacts.stream().map(contact -> buildContactDetailDto(contact, false)).toList();
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.CREATION})
    public Contact create(@Valid @RequestBody ContactFormDto contactFormDto) {
        Contact contact = new Contact();
        setContactData(contact, contactFormDto);

        Service service = serviceJpaRepository.findById(contactFormDto.getIdService());
        if (service == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Service non trouve");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        CentreGestion centreGestion = resolveContactCentre(contactFormDto, service, utilisateur);
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
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
        return contactJpaRepository.saveAndFlush(contact);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.SUPPRESSION}, evaluator = ContactPermissionEvaluator.class)
    public boolean delete(@PathVariable("id") int id) {
        Contact contact = contactJpaRepository.findById(id);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
        }
        contactJpaRepository.delete(contact);
        contactJpaRepository.flush();
        return true;
    }

    private void setContactData(Contact contact, ContactFormDto contactFormDto) {
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
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

    private List<Contact> getVisibleContactsByService(int idService, Integer idCentreGestion, Utilisateur utilisateur) {
        List<Contact> contacts = contactJpaRepository.findByService(idService);

        if (contacts == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
        }

        if (!isGestionnaire(utilisateur)) {
            return contacts;
        }

        List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
        if (centresDemandeur.isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
        }

        List<Contact> filteredContacts = contacts.stream()
                .filter(contact -> canViewContact(centresDemandeur, contact))
                .toList();

        if (idCentreGestion == null || idCentreGestion == -1) {
            return filteredContacts;
        }

        CentreGestion centreGestionConvention = centreGestionJpaRepository.findById(idCentreGestion.intValue());
        if (centreGestionConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
        }

        return filteredContacts.stream()
                .filter(contact -> canUseContactForConvention(contact, centreGestionConvention, centresDemandeur))
                .toList();
    }

    private boolean isGestionnaire(Utilisateur utilisateur) {
        return UtilisateurHelper.isRole(utilisateur, Role.GES)
                || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES);
    }

    private CentreGestion resolveGestionnaireContactCentre(ContactFormDto contactFormDto, Utilisateur utilisateur) {
        List<CentreGestion> centresGestionnaire = getCurrentGestionnaireCentres(utilisateur);
        if (centresGestionnaire.isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
        }

        if (contactFormDto.getIdCentreGestion() == null) {
            if (centresGestionnaire.size() == 1) {
                return centresGestionnaire.get(0);
            }
            throw new AppException(HttpStatus.BAD_REQUEST, "Le centre de gestion du contact doit etre renseigne");
        }

        return centresGestionnaire.stream()
                .filter(centreGestion -> centreGestion.getId() == contactFormDto.getIdCentreGestion())
                .findFirst()
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "Le centre de gestion du contact n'est pas rattache au gestionnaire"));
    }

    private CentreGestion resolveContactCentre(ContactFormDto contactFormDto, Service service, Utilisateur utilisateur) {
        if (isGestionnaire(utilisateur)) {
            return resolveGestionnaireContactCentre(contactFormDto, utilisateur);
        }

        if (contactFormDto.getIdCentreGestion() != null) {
            return centreGestionJpaRepository.findById(contactFormDto.getIdCentreGestion().intValue());
        }

        return resolveDefaultContactCentre(service);
    }

    private CentreGestion resolveDefaultContactCentre(Service service) {
        if (service.getCentreGestion() != null) {
            return service.getCentreGestion();
        }
        return centreGestionJpaRepository.getCentreEtablissement();
    }

    private List<CentreGestion> getCurrentGestionnaireCentres(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getUid() == null || utilisateur.getUid().isBlank()) {
            return new ArrayList<>();
        }
        return centreGestionJpaRepository.findAllByGestionnaireUid(utilisateur.getUid());
    }

    private boolean canViewContact(List<CentreGestion> centresDemandeur, Contact contact) {
        for (CentreGestion centreDemandeur : centresDemandeur) {
            if (confidentialiteService.canViewContact(centreDemandeur, contact)) {
                return true;
            }
        }
        return false;
    }

    private boolean canUseContactForConvention(Contact contact, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        CentreGestion centreGestionContact = contact != null ? contact.getCentreGestion() : null;
        return centreGestionContact != null
                && (centreGestionContact.getId() == centreGestionConvention.getId()
                || isAttachedToCentre(centresDemandeur, centreGestionContact)
                || confidentialiteService.isNoConfidentiality(centreGestionContact)
                || confidentialiteService.isCentreEtablissement(centreGestionContact));
    }

    private boolean isAttachedToCentre(List<CentreGestion> centresDemandeur, CentreGestion centreGestion) {
        return centresDemandeur != null
                && centreGestion != null
                && centresDemandeur.stream().anyMatch(centreDemandeur -> centreDemandeur.getId() == centreGestion.getId());
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

    private ContactDetailDto buildContactDetailDto(Contact contact, boolean hideSensitiveFields) {
        ContactDetailDto contactDetailDto = new ContactDetailDto();
        contactDetailDto.setId(contact.getId());
        contactDetailDto.setNom(contact.getNom());
        contactDetailDto.setPrenom(contact.getPrenom());
        contactDetailDto.setFonction(contact.getFonction());
        contactDetailDto.setCivilite(contact.getCivilite());
        contactDetailDto.setIdCentreGestion(contact.getCentreGestion().getId());
        contactDetailDto.setCentreGestionnaire(ContactDetailDto.CentreGestionDto.from(contact.getCentreGestion()));
        if (!hideSensitiveFields) {
            contactDetailDto.setMail(contact.getMail());
            contactDetailDto.setTel(contact.getTel());
            contactDetailDto.setTelephone(contact.getTel());
            contactDetailDto.setFax(contact.getFax());
        }
        return contactDetailDto;
    }
}
