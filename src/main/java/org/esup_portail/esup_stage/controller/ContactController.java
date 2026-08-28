package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.constants.DroitOpposition;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ContactDetailDto;
import org.esup_portail.esup_stage.dto.ContactDto;
import org.esup_portail.esup_stage.dto.ContactFormDto;
import org.esup_portail.esup_stage.dto.DroitOppositionFormDto;
import org.esup_portail.esup_stage.dto.DroitOppositionResultDto;
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
import org.esup_portail.esup_stage.service.ConfidentialiteAccessService;
import org.esup_portail.esup_stage.service.DroitOppositionContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    ConfidentialiteAccessService confidentialiteAccessService;

    @Autowired
    DroitOppositionContactService droitOppositionContactService;

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

            List<Integer> centreIds = confidentialiteAccessService.getVisibleCentreIds(centresDemandeur);
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
            List<Integer> centreIds = confidentialiteAccessService.getVisibleCentreIds(centresDemandeur);
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

    /**
     * Envoi manuel du mail de droit d'opposition à un contact précis.
     * La relance d'un contact déjà sollicité est autorisée : l'écran confirme au préalable.
     */
    @PostMapping("/{id}/solliciter-droit-opposition")
    @Secure(fonctions = {AppFonctionEnum.SERVICE_CONTACT_ACC}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true, evaluator = ContactPermissionEvaluator.class)
    public ContactDetailDto solliciterDroitOpposition(@PathVariable("id") int id) {
        droitOppositionContactService.solliciterContact(id);
        return buildContactDetailDto(contactJpaRepository.findById(id), shouldHideSensitiveContactFields(ServiceContext.getUtilisateur()));
    }

    /**
     * Enregistrement en masse des refus d'être contacté remontés sur la boîte mail générique.
     */
    @PostMapping("/refus-etre-contacte")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public DroitOppositionResultDto enregistrerRefusEtreContacte(@RequestBody DroitOppositionFormDto droitOppositionFormDto) {
        return droitOppositionContactService.enregistrerRefus(droitOppositionFormDto.getMails());
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
        setRefusEtreContacte(contact, contactFormDto.getRefusEtreContacte());
    }

    /**
     * Applique le droit d'opposition saisi manuellement.
     * Le refus n'est horodaté qu'à la transition vers "true" pour ne pas écraser la trace d'origine,
     * et sa levée est réservée aux profils gestionnaires : un étudiant peut déclarer un refus,
     * pas l'annuler.
     */
    private void setRefusEtreContacte(Contact contact, Boolean refusEtreContacte) {
        boolean refusActuel = Boolean.TRUE.equals(contact.getRefusEtreContacte());
        boolean refusDemande = Boolean.TRUE.equals(refusEtreContacte);

        if (refusActuel == refusDemande) {
            return;
        }

        if (refusActuel && shouldHideSensitiveContactFields(ServiceContext.getUtilisateur())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Le refus d'être contacté ne peut être levé que par un gestionnaire");
        }

        contact.setRefusEtreContacte(refusDemande);
        if (refusDemande) {
            contact.setDateRefusEtreContacte(new Date());
            contact.setOrigineRefusEtreContacte(DroitOpposition.ORIGINE_REFUS_MANUEL);
        } else {
            contact.setDateRefusEtreContacte(null);
            contact.setOrigineRefusEtreContacte(null);
        }
    }

    private List<Contact> getVisibleContactsByService(int idService, Integer idCentreGestion, Utilisateur utilisateur) {
        List<Contact> contacts = contactJpaRepository.findByService(idService);

        if (contacts == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
        }

        CentreGestion centreGestionConvention = null;
        if (idCentreGestion != null && idCentreGestion != -1) {
            centreGestionConvention = centreGestionJpaRepository.findById(idCentreGestion.intValue());
            if (centreGestionConvention == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
            }
        }
        final CentreGestion centreConvention = centreGestionConvention;

        // L'administrateur a accès à l'ensemble des contacts
        if (UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            return contacts;
        }

        // Profils non gestionnaires (étudiant / enseignant en contexte convention) : la
        // confidentialité est appliquée côté backend. Seuls les contacts utilisables pour le
        // centre concerné (même centre, non confidentiel, ou établissement) sont renvoyés — les
        // contacts confidentiels d'un autre centre ne fuitent pas.
        if (!isGestionnaire(utilisateur)) {
            return contacts.stream()
                    .filter(contact -> canUseContactForConvention(contact, centreConvention, List.of()))
                    .toList();
        }

        List<CentreGestion> centresDemandeur = getCurrentGestionnaireCentres(utilisateur);
        if (centresDemandeur.isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Impossible de determiner le centre de gestion du gestionnaire");
        }

        List<Contact> filteredContacts = contacts.stream()
                .filter(contact -> canViewContact(centresDemandeur, contact))
                .toList();

        if (centreConvention == null) {
            return filteredContacts;
        }

        return filteredContacts.stream()
                .filter(contact -> canUseContactForConvention(contact, centreConvention, centresDemandeur))
                .toList();
    }

    private boolean isGestionnaire(Utilisateur utilisateur) {
        return confidentialiteAccessService.isGestionnaire(utilisateur);
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
        return confidentialiteAccessService.getCentresDemandeur(utilisateur);
    }

    private boolean canViewContact(List<CentreGestion> centresDemandeur, Contact contact) {
        return confidentialiteAccessService.canViewContact(centresDemandeur, contact);
    }

    private boolean canUseContactForConvention(Contact contact, CentreGestion centreGestionConvention, List<CentreGestion> centresDemandeur) {
        return confidentialiteAccessService.canUseContactForConvention(contact, centreGestionConvention, centresDemandeur);
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
        // Le droit d'opposition reste visible de tous les profils
        contactDetailDto.setRefusEtreContacte(contact.getRefusEtreContacte());
        contactDetailDto.setDateRefusEtreContacte(contact.getDateRefusEtreContacte());
        contactDetailDto.setDateEnvoiMailOpposition(contact.getDateEnvoiMailOpposition());
        if (!hideSensitiveFields) {
            // Centre gestionnaire (RGPD) et coordonnées réservés aux profils autorisés
            contactDetailDto.setCentreGestionnaire(ContactDetailDto.CentreGestionDto.from(contact.getCentreGestion()));
            contactDetailDto.setMail(contact.getMail());
            contactDetailDto.setTel(contact.getTel());
            contactDetailDto.setTelephone(contact.getTel());
            contactDetailDto.setFax(contact.getFax());
        }
        return contactDetailDto;
    }
}
