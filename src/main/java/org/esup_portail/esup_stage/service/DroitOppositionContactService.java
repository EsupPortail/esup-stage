package org.esup_portail.esup_stage.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.constants.DroitOpposition;
import org.esup_portail.esup_stage.constants.ValidationPatterns;
import org.esup_portail.esup_stage.dto.DroitOppositionResultDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.TemplateMail;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Droit d'opposition des contacts en entreprise (signataires et tuteurs professionnels).
 * <p>
 * Deux voies alimentent le refus d'être contacté :
 * <ul>
 *     <li>l'envoi automatique d'un mail invitant le contact à signaler son refus sur la boîte
 *     générique de l'établissement, déclenché par la tâche planifiée
 *     {@code EnvoiMailDroitOppositionContact} ;</li>
 *     <li>la saisie en masse des adresses ayant répondu sur cette boîte générique.</li>
 * </ul>
 * La saisie unitaire, elle, passe directement par les écrans contact.
 */
@Service
public class DroitOppositionContactService {

    private static final Logger logger = LogManager.getLogger(DroitOppositionContactService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(ValidationPatterns.EMAIL);

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContactJpaRepository contactJpaRepository;

    @Autowired
    MailerService mailerService;

    @Autowired
    AppConfigService appConfigService;

    /**
     * Envoie le mail de recueil du droit d'opposition aux contacts des conventions validées qui
     * n'ont encore jamais été sollicités.
     * <p>
     * Un contact n'est sollicité qu'une seule fois : {@code dateEnvoiMailOpposition} est horodatée
     * après chaque envoi réussi. Un contact à la fois tuteur professionnel et signataire ne reçoit
     * qu'un seul mail, celui du tuteur professionnel.
     */
    @Transactional
    public DroitOppositionResultDto envoyerMailsDroitOpposition() {
        String mailGenerique = getMailOppositionContact();

        DroitOppositionResultDto result = new DroitOppositionResultDto();
        Set<Integer> contactsTraites = new HashSet<>();

        traiterLot(conventionJpaRepository.findConventionsTuteurProDroitOpposition(),
                Convention::getContact, TemplateMail.CODE_DROIT_OPPOSITION_TUTEUR_PRO,
                mailGenerique, contactsTraites, result);

        traiterLot(conventionJpaRepository.findConventionsSignataireDroitOpposition(),
                Convention::getSignataire, TemplateMail.CODE_DROIT_OPPOSITION_SIGNATAIRE,
                mailGenerique, contactsTraites, result);

        logger.info("Droit d'opposition : {} mail(s) envoyé(s), {} en erreur", result.getEnvoyes(), result.getErreurs());
        return result;
    }

    /**
     * Envoi manuel du mail de droit d'opposition à un contact précis, depuis sa fiche.
     * <p>
     * Contrairement à l'envoi automatique, la relance est ici autorisée : c'est un geste délibéré
     * d'un gestionnaire (mail perdu, adresse corrigée). {@code dateEnvoiMailOpposition} est
     * réhorodatée, ce qui sort définitivement le contact du périmètre de la tâche planifiée.
     *
     * @return la date de la sollicitation précédente, ou {@code null} s'il s'agit de la première
     */
    @Transactional
    public Date solliciterContact(int idContact) {
        String mailGenerique = getMailOppositionContact();

        Contact contact = contactJpaRepository.findById(idContact);
        if (contact == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contact non trouve");
        }
        if (contact.getMail() == null || contact.getMail().trim().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ce contact n'a pas d'adresse mail renseignée");
        }
        if (Boolean.TRUE.equals(contact.getRefusEtreContacte())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ce contact a déjà signalé qu'il ne souhaitait pas être contacté");
        }

        // Le tuteur professionnel l'emporte, comme dans l'envoi automatique
        String templateMailCode = TemplateMail.CODE_DROIT_OPPOSITION_TUTEUR_PRO;
        Convention convention = premiere(conventionJpaRepository.findConventionsValideesParTuteurPro(idContact));
        if (convention == null) {
            templateMailCode = TemplateMail.CODE_DROIT_OPPOSITION_SIGNATAIRE;
            convention = premiere(conventionJpaRepository.findConventionsValideesParSignataire(idContact));
        }
        if (convention == null) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Ce contact n'est tuteur professionnel ni signataire d'aucune convention validée");
        }

        Date dateEnvoiPrecedent = contact.getDateEnvoiMailOpposition();
        mailerService.sendDroitOpposition(contact.getMail(), convention, templateMailCode,
                DroitOpposition.construireLienMailto(mailGenerique, contact.getPrenom(), contact.getNom(), contact.getMail()));

        contact.setDateEnvoiMailOpposition(new Date());
        contactJpaRepository.saveAndFlush(contact);
        logger.info("Droit d'opposition : envoi manuel au contact {}", idContact);
        return dateEnvoiPrecedent;
    }

    private Convention premiere(List<Convention> conventions) {
        return conventions.isEmpty() ? null : conventions.get(0);
    }

    /**
     * Enregistre en masse les refus remontés sur la boîte mail générique.
     * Toutes les fiches contact portant l'adresse sont impactées.
     */
    @Transactional
    public DroitOppositionResultDto enregistrerRefus(List<String> mails) {
        DroitOppositionResultDto result = new DroitOppositionResultDto();
        if (mails == null || mails.isEmpty()) {
            return result;
        }

        // LinkedHashSet : dédoublonnage en conservant l'ordre de saisie pour la lisibilité du compte-rendu
        Set<String> adresses = new LinkedHashSet<>();
        for (String mail : mails) {
            if (mail == null) {
                continue;
            }
            String normalise = mail.trim().toLowerCase();
            if (!normalise.isEmpty()) {
                adresses.add(normalise);
            }
        }

        for (String adresse : adresses) {
            if (!EMAIL_PATTERN.matcher(adresse).matches()) {
                result.getInvalides().add(adresse);
                continue;
            }

            List<Contact> contacts = contactJpaRepository.findByMailIgnoreCase(adresse);
            if (contacts.isEmpty()) {
                result.getInconnues().add(adresse);
                continue;
            }

            for (Contact contact : contacts) {
                if (!Boolean.TRUE.equals(contact.getRefusEtreContacte())) {
                    contact.setRefusEtreContacte(true);
                    contact.setDateRefusEtreContacte(new Date());
                    contact.setOrigineRefusEtreContacte(DroitOpposition.ORIGINE_REFUS_MASSE);
                }
            }
            contactJpaRepository.saveAll(contacts);
            result.ajouterTraitee(adresse, contacts.size());
        }

        contactJpaRepository.flush();
        return result;
    }

    private void traiterLot(List<Convention> conventions, ContactExtracteur extracteur, String templateMailCode,
                            String mailGenerique, Set<Integer> contactsTraites, DroitOppositionResultDto result) {
        List<Contact> contactsAHorodater = new ArrayList<>();

        for (Convention convention : conventions) {
            Contact contact = extracteur.extraire(convention);
            // Les conventions sont triées par id décroissant : le premier passage retient la
            // convention la plus récente du contact, les suivantes sont ignorées.
            if (contact == null || !contactsTraites.add(contact.getId())) {
                continue;
            }

            try {
                mailerService.sendDroitOpposition(contact.getMail(), convention, templateMailCode,
                        DroitOpposition.construireLienMailto(mailGenerique, contact.getPrenom(), contact.getNom(), contact.getMail()));
                contact.setDateEnvoiMailOpposition(new Date());
                contactsAHorodater.add(contact);
                result.setEnvoyes(result.getEnvoyes() + 1);
            } catch (Exception e) {
                // Un échec unitaire ne doit pas interrompre le lot
                logger.error("Droit d'opposition : échec de l'envoi au contact {}", contact.getId(), e);
                result.setErreurs(result.getErreurs() + 1);
            }
        }

        if (!contactsAHorodater.isEmpty()) {
            contactJpaRepository.saveAll(contactsAHorodater);
            contactJpaRepository.flush();
        }
    }

    private String getMailOppositionContact() {
        String mailGenerique = appConfigService.getConfigGenerale().getMailOppositionContact();
        if (mailGenerique == null || mailGenerique.trim().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "L'adresse de la boîte mail générique de recueil des refus n'est pas renseignée dans les paramètres généraux");
        }
        return mailGenerique.trim();
    }

    @FunctionalInterface
    private interface ContactExtracteur {
        Contact extraire(Convention convention);
    }
}
