package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.constants.ValidationPatterns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigSignatureDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.dto.LibelleImpressionLangueDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.AffectationId;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.repository.AffectationJpaRepository;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.repository.LangueConventionJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.FileValidationService;
import org.esup_portail.esup_stage.service.impression.LibelleImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@ApiController
@RequestMapping("/config")
public class AppConfigController {

    private static final Logger logger = LogManager.getLogger(AppConfigController.class);

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    LibelleImpressionService libelleImpressionService;

    @Autowired
    AffectationJpaRepository affectationJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    FileValidationService fileValidationService;

    @JsonView(Views.Etu.class)
    @GetMapping("/generale/etu")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ConfigGeneraleDto getConfigGeneraleEtu() {
        return appConfigService.getConfigGenerale();
    }

    @GetMapping("/generale")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ConfigGeneraleDto getConfigGenerale() {
        return appConfigService.getConfigGenerale();
    }

    @PostMapping("/generale")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public ConfigGeneraleDto updateGenerale(@RequestBody ConfigGeneraleDto configGeneraleDto) throws JacksonException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.GENERAL);
        }
        if (configGeneraleDto.getTypeCentre() == TypeCentreEnum.VIDE) {
            configGeneraleDto.setTypeCentre(null);
        }
        configGeneraleDto.setMailOppositionContact(normaliserMailOppositionContact(configGeneraleDto.getMailOppositionContact()));
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configGeneraleDto));
        appConfigJpaRepository.saveAndFlush(appConfig);

        // ajout de code université dans la table Affectation si elle n'existe pas
        List<Affectation> affectations = affectationJpaRepository.findByCodeUniversite(configGeneraleDto.getCodeUniversite());
        if (affectations.isEmpty()) {
            Affectation affectation = new Affectation();
            AffectationId affectationId = new AffectationId();
            affectationId.setCode("");
            affectationId.setCodeUniversite(configGeneraleDto.getCodeUniversite());
            affectation.setId(affectationId);
            affectation.setLibelle("");
            affectationJpaRepository.saveAndFlush(affectation);
        }

        return appConfigService.getConfigGenerale();
    }

    /**
     * La boîte générique de recueil des refus doit être une adresse mail simple : elle est injectée
     * telle quelle dans le lien {@code mailto:} des mails de droit d'opposition. Une valeur mal
     * formée produirait un lien inopérant côté contact, sans erreur visible.
     */
    private String normaliserMailOppositionContact(String mailOppositionContact) {
        if (mailOppositionContact == null || mailOppositionContact.trim().isEmpty()) {
            return null;
        }
        String normalise = mailOppositionContact.trim();
        if (!normalise.matches(ValidationPatterns.EMAIL)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "L'adresse de la boîte mail de recueil des refus n'est pas valide (attendu : adresse@domaine.fr)");
        }
        return normalise;
    }

    @GetMapping("/alerte-mail")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL,AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE},forbiddenEtu = true)
    public ConfigAlerteMailDto getConfigAlerteMail() {
        return appConfigService.getConfigAlerteMail();
    }

    @PostMapping("/alerte-mail")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public ConfigAlerteMailDto updateAlerteMail(@RequestBody ConfigAlerteMailDto configAlerteMailDto) throws JacksonException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.ALERTE);
        }
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configAlerteMailDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        return appConfigService.getConfigAlerteMail();
    }

    @GetMapping("/theme")
    @Secure
    public ConfigThemeDto getConfigTheme() {
        return appConfigService.getConfigTheme();
    }

    @PostMapping("/theme")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public ConfigThemeDto updateTheme(@RequestParam("data") String data, @RequestParam(value="logo",required = false) MultipartFile logo, @RequestParam(value="favicon",required = false) MultipartFile favicon) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ConfigThemeDto configThemeDto = mapper.readValue(data, ConfigThemeDto.class);
        ConfigThemeDto configThemeDtoOrigin = appConfigService.getConfigTheme();
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.THEME);
            appConfig.setParametres(mapper.writeValueAsString(configThemeDto));
            appConfigJpaRepository.saveAndFlush(appConfig);
        }
        configThemeDto.setDateModification(new Date());

        if (logo != null) {
            FileValidationService.ValidatedImage validatedLogo = fileValidationService.validateImage(logo);
            ConfigThemeDto.File64 logo64 = new ConfigThemeDto.File64();
            logo64.setContentType(validatedLogo.contentType());
            logo64.setBase64(Base64.getEncoder().encodeToString(validatedLogo.bytes()));
            configThemeDto.setLogo(logo64);
        } else {
            configThemeDto.setLogo(configThemeDtoOrigin.getLogo());
        }

        if (favicon != null) {
            FileValidationService.ValidatedImage validatedFavicon = fileValidationService.validateImage(favicon);
            ConfigThemeDto.File64 favicon64 = new ConfigThemeDto.File64();
            favicon64.setContentType(validatedFavicon.contentType());
            favicon64.setBase64(Base64.getEncoder().encodeToString(validatedFavicon.bytes()));
            configThemeDto.setFavicon(favicon64);
        } else {
            configThemeDto.setFavicon(configThemeDtoOrigin.getFavicon());
        }

        appConfigService.writeImageIntoFile(configThemeDto);
        appConfig.setParametres(mapper.writeValueAsString(configThemeDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        appConfigService.updateTheme();

        return appConfigService.getConfigTheme();
    }

    @DeleteMapping("/theme")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION}, forbiddenEtu = true)
    public ConfigThemeDto rollbackTheme() throws IOException, URISyntaxException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig != null) {
            appConfigJpaRepository.delete(appConfig);
            appConfigJpaRepository.flush();
        }
        appConfigService.updateTheme();
        return appConfigService.getConfigTheme();
    }

    @GetMapping("/signature")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE}, forbiddenEtu = true)
    public ConfigSignatureDto getSignature() {
        return appConfigService.getConfigSignature();
    }

    @GetMapping("/libelles-impression")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE}, forbiddenEtu = true)
    public List<LibelleImpressionLangueDto> getLibellesImpression() {
        int nbClesTotal = libelleImpressionService.getClesAutorisees().size();
        return langueConventionJpaRepository.findAll().stream()
                .filter(langue -> "O".equals(langue.getTemEnServ()))
                .map(langue -> {
                    LibelleImpressionLangueDto dto = new LibelleImpressionLangueDto();
                    dto.setCode(langue.getCode());
                    dto.setLibelle(langue.getLibelle());
                    dto.setNbClesTotal(nbClesTotal);
                    dto.setNbClesRenseignees(libelleImpressionService.compterClesTraduites(langue.getCode()));
                    Path chemin = libelleImpressionService.getCheminSurcharge(langue.getCode());
                    if (Files.isRegularFile(chemin)) {
                        dto.setSurcharge(true);
                        try {
                            dto.setDateModification(new Date(Files.getLastModifiedTime(chemin).toMillis()));
                        } catch (IOException e) {
                            logger.warn("Date de modification illisible pour {}", chemin, e);
                        }
                    }
                    return dto;
                })
                .toList();
    }

    @GetMapping(value = "/libelles-impression/{code}/fichier", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE}, forbiddenEtu = true)
    public ResponseEntity<byte[]> getFichierLibellesImpression(@PathVariable("code") String code) {
        LangueConvention langue = getLangueConvention(code);
        String nomFichier = libelleImpressionService.getNomFichier(langue.getCode());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(libelleImpressionService.getContenuFichierTravail(langue.getCode()));
    }

    @PostMapping("/libelles-impression/{code}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public List<LibelleImpressionLangueDto> updateLibellesImpression(@PathVariable("code") String code, @RequestParam("fichier") MultipartFile fichier) {
        LangueConvention langue = getLangueConvention(code);
        FileValidationService.ValidatedProperties validated = fileValidationService.validateProperties(fichier, libelleImpressionService.getClesAutorisees());
        // Le nom du fichier déposé est ignoré : le chemin est construit à partir du code de langue validé
        libelleImpressionService.ecrireSurcharge(langue.getCode(), validated.bytes());
        return getLibellesImpression();
    }

    @DeleteMapping("/libelles-impression/{code}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION}, forbiddenEtu = true)
    public List<LibelleImpressionLangueDto> deleteLibellesImpression(@PathVariable("code") String code) {
        LangueConvention langue = getLangueConvention(code);
        libelleImpressionService.supprimerSurcharge(langue.getCode());
        return getLibellesImpression();
    }

    private LangueConvention getLangueConvention(String code) {
        LangueConvention langue = code == null ? null : langueConventionJpaRepository.findByCode(code);
        if (langue == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Langue de convention " + code + " non trouvée");
        }
        return langue;
    }

    @PostMapping("/signature")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION}, forbiddenEtu = true)
    public ConfigSignatureDto updateSignature(@RequestBody ConfigSignatureDto configSignatureDto) throws JacksonException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.SIGNATURE);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.SIGNATURE);
        }
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configSignatureDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        return appConfigService.getConfigSignature();
    }

}
