package org.esup_portail.esup_stage.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.Composante;
import org.esup_portail.esup_stage.service.apogee.model.Etape;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/centre-gestion")
public class CentreGestionController {

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ConfidentialiteJpaRepository confidentialiteJpaRepository;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    FichierJpaRepository fichierJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ApogeeService apogeeService;

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<CentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        JSONObject jsonFilters = new JSONObject(filters);
        Map<String, Object> map = new HashMap<>();
        map.put("type", "boolean");
        map.put("value", true);
        jsonFilters.put("validationCreation", map);
        filters = jsonFilters.toString();
        PaginatedResponse<CentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(centreGestionRepository.count(filters));
        paginatedResponse.setData(centreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));

        if (predicate.equals("personnels")) {
            ContextDto contexteDto = ServiceContext.getServiceContext();
            Utilisateur currentUser = contexteDto.getUtilisateur();
            List<CentreGestion> list =  paginatedResponse.getData();
            Predicate<PersonnelCentreGestion> condition = value -> value.getUidPersonnel().equals(currentUser.getLogin());
            list.sort((a, b) -> Boolean.compare(a.getPersonnels().stream().anyMatch(condition), b.getPersonnels().stream().anyMatch(condition)));

            if (sortOrder.equals("asc"))
                Collections.reverse(list);
        }

        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = centreGestionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = centreGestionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @GetMapping("/creation-brouillon")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public CentreGestion getBrouillonByLogin() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        CentreGestion centreGestion = centreGestionJpaRepository.findBrouillon(utilisateur.getLogin());
        if (centreGestion == null) {
            centreGestion = new CentreGestion();
        }

        return centreGestion;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public CentreGestion getById(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        if (centreGestion == null) {
            centreGestion = new CentreGestion();
        }
        return centreGestion;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public CentreGestion create(@Valid @RequestBody CentreGestion centreGestion) {
        CentreGestion etablissement = centreGestionJpaRepository.getCentreEtablissement();

        centreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        if (etablissement != null) {
            centreGestion.setCodeConfidentialite(etablissement.getCodeConfidentialite());
        }
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @PutMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion update(@Valid @RequestBody CentreGestion centreGestion) {
        // Les ordres de validations doivent être 1, 2 ou 3, et on ne peut pas avoir le même ordre
        List<Integer> ordres = Arrays.asList(1, 2, 3);
        if (
                !ordres.contains(centreGestion.getValidationPedagogiqueOrdre())
                || !ordres.contains(centreGestion.getValidationConventionOrdre())
                || !ordres.contains(centreGestion.getVerificationAdministrativeOrdre())
                || !(!Objects.equals(centreGestion.getValidationPedagogiqueOrdre(), centreGestion.getValidationConventionOrdre()) && !Objects.equals(centreGestion.getValidationPedagogiqueOrdre(), centreGestion.getVerificationAdministrativeOrdre()) && !Objects.equals(centreGestion.getValidationConventionOrdre(), centreGestion.getVerificationAdministrativeOrdre()))
        ) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Les ordres de validations doivent être 1, 2 ou 3, et être différentes pour chaque types de validation");
        }
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion validationCreation(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        centreGestion.setValidationCreation(true);

        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        // Suppression des critères et personnels rattachés à ce centre
        critereGestionJpaRepository.deleteCriteresByCentreId(id);
        personnelCentreGestionJpaRepository.deletePersonnelsByCentreId(id);

        centreGestionJpaRepository.deleteById(id);
        centreGestionJpaRepository.flush();
    }

    @GetMapping("/{id}/composantes")
    @Secure()
    public List<Composante> getComposantes(@PathVariable("id") int id) {
        List<Composante> composantes = apogeeService.getListComposante();
        List<CritereGestion> critereGestionsComposantes = critereGestionJpaRepository.findComposantes();

        composantes = composantes.stream().filter(c -> critereGestionsComposantes.stream().noneMatch(cg -> cg.getId().getCode().equalsIgnoreCase(c.getCode()) && cg.getCentreGestion().getId() != id)).collect(Collectors.toList());
        composantes.sort(Comparator.comparing(Composante::getCode));
        return composantes;
    }

    @GetMapping("/{id}/composante")
    @Secure()
    public Composante getCentreComposante(@PathVariable("id") int id) {
        CritereGestion critereGestion = critereGestionJpaRepository.findByCentreId(id);
        Composante composante = new Composante();
        if (critereGestion != null) {
            composante.setCode(critereGestion.getId().getCode());
            composante.setLibelle(critereGestion.getLibelle());
        }
        return composante;
    }

    @PutMapping("/{id}/set-composante")
    @Secure()
    public Composante setComposante(@PathVariable("id") int id, @RequestBody Composante _composante) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        CritereGestion critereGestion = critereGestionJpaRepository.findByCentreId(id);
        CritereGestionId critereGestionId = new CritereGestionId();
        critereGestionId.setCode(_composante.getCode());
        critereGestionId.setCodeVersionEtape("");

        if (critereGestion != null) {
            if (conventionJpaRepository.countConventionRattacheUfr(critereGestion.getCentreGestion().getId(), _composante.getCode()) > 0) {
                throw new AppException(HttpStatus.FORBIDDEN, "Une convention est déjà rattachée à cette composante");
            }

            critereGestionJpaRepository.delete(critereGestion);
        }

        critereGestion = new CritereGestion();
        critereGestion.setId(critereGestionId);
        critereGestion.setLibelle(_composante.getLibelle());
        critereGestion.setCentreGestion(centreGestion);
        critereGestionJpaRepository.saveAndFlush(critereGestion);
        return _composante;
    }

    @GetMapping("/{id}/etapes")
    @Secure()
    public List<Etape> getEtapes(@PathVariable("id") int id) {
        List<Etape> etapes = apogeeService.getListEtape();
        List<CritereGestion> critereGestionsEtapes = critereGestionJpaRepository.findEtapes();
        etapes = etapes.stream().filter(e -> critereGestionsEtapes.stream().noneMatch(cg -> cg.getId().getCode().equalsIgnoreCase(e.getCode()) && cg.getCentreGestion().getId() != id)).collect(Collectors.toList());
        etapes.sort(Comparator.comparing(Etape::getCodeVrsEtp).thenComparing(Etape::getCode));
        return etapes;
    }

    @GetMapping("/{id}/centre-etapes")
    @Secure()
    public List<Etape> getCentreEtapes(@PathVariable("id") int id) {
        List<CritereGestion> critereGestionsEtapes = critereGestionJpaRepository.findEtapesByCentreId(id);
        List<Etape> etapes = new ArrayList<>();

        for (CritereGestion cg : critereGestionsEtapes) {
            if (cg != null) {
                Etape etape = new Etape();
                etape.setCode(cg.getId().getCode());
                etape.setCodeVrsEtp(cg.getId().getCodeVersionEtape());
                etape.setLibelle(cg.getLibelle());
                etapes.add(etape);
            }
        }

        return etapes;
    }

    @PostMapping("/{id}/add-etape")
    @Secure()
    public Etape addEtape(@PathVariable("id") int id, @RequestBody Etape _etape) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        CritereGestionId critereGestionId = new CritereGestionId();
        critereGestionId.setCode(_etape.getCode());
        critereGestionId.setCodeVersionEtape(_etape.getCodeVrsEtp());

        CritereGestion critereGestion = new CritereGestion();
        critereGestion.setId(critereGestionId);
        critereGestion.setLibelle(_etape.getLibelle());
        critereGestion.setCentreGestion(centreGestion);
        critereGestionJpaRepository.saveAndFlush(critereGestion);

        return _etape;
    }

    @DeleteMapping("/delete-etape/{codeEtape}/{codeVersion}")
    @Secure()
    public void deleteEtape(@PathVariable("codeEtape") String codeEtape, @PathVariable("codeVersion") String codeVersion) {
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(codeEtape, codeVersion);
        int idCentreGestion = critereGestion.getCentreGestion().getId();

        if (conventionJpaRepository.countConventionRattacheEtape(idCentreGestion, codeEtape, codeVersion) > 0) {
            throw new AppException(HttpStatus.FORBIDDEN, "Une convention est déjà rattachée à cette étape");
        }

        critereGestionJpaRepository.delete(critereGestion);
        critereGestionJpaRepository.flush();
    }

    @GetMapping("/confidentialite")
    @Secure()
    public List<Confidentialite> getConfidentialites() {
        return confidentialiteJpaRepository.findAll();
    }

    @GetMapping("etablissement-confidentialite")
    @Secure()
    public Confidentialite getEtablissementConfidentialite() {
        CentreGestion centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        return centreGestion.getCodeConfidentialite();
    }

    @PostMapping("/{id}/logo-centre")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion insertLogoCentre(@PathVariable("id") int id, @RequestParam(required = true) MultipartFile logo) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        String extension = FilenameUtils.getExtension(logo.getOriginalFilename());
        String nomFichier = DigestUtils.md5Hex(logo.getOriginalFilename()) + "." + extension;
        String nomReel = logo.getOriginalFilename();
        // Autorisation de l'upload d'images uniquement
        if (logo.getContentType() == null || !logo.getContentType().startsWith("image/")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le fichier doit être au format image");
        }

        Fichier fichier = centreGestion.getFichier();
        if (fichier == null) {
            fichier = new Fichier();
        }

        fichier.setNom(nomFichier);
        fichier.setNomReel(nomReel);
        fichier = fichierJpaRepository.saveAndFlush(fichier);

        try {
            String filename = this.getNomFichier(fichier.getId(), fichier.getNom());
            Path uploadLocation = Paths.get(this.getFilePath(filename));
            Files.copy(logo.getInputStream(), uploadLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'insertion du fichier : " + e.getMessage());
        }

        centreGestion.setFichier(fichier);
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @GetMapping(value = "/{id}/logo-centre", produces = MediaType.IMAGE_PNG_VALUE)
    @Secure()
    public ResponseEntity<byte[]> getLogoCentre(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        if (centreGestion.getFichier() != null) {
            byte[] image;
            try {
                String filename = this.getNomFichier(centreGestion.getFichier().getId(), centreGestion.getFichier().getNom());
                image = FileUtils.readFileToByteArray(new File(this.getFilePath(filename)));
                return ResponseEntity.ok().body(image);
            } catch (IOException e) {
                throw new AppException(HttpStatus.NOT_FOUND, "Erreur lors de la récupération du fichier");
            }
        }

        return null;
    }

    @PostMapping("/{id}/resize-logo")
    @Secure()
    public void resizeLogoCentre(@PathVariable("id") int id, @RequestBody List<Integer> dimensions) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        if (centreGestion.getFichier() != null) {
            byte[] image;
            try {
                String filename = this.getNomFichier(centreGestion.getFichier().getId(), centreGestion.getFichier().getNom());
                image = FileUtils.readFileToByteArray(new File(this.getFilePath(filename)));

                // Récupération de l'image originale
                InputStream in = new ByteArrayInputStream(image);
                BufferedImage originalImage = ImageIO.read(in);

                // Création d'une nouvelle image à partir de l'originale et des dimensions spécifiées
                Integer width = dimensions.get(0);
                Integer height = dimensions.get(1);
                Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resizedImage, 0, 0, null);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(outputImage, "png", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());

                Path uploadLocation = Paths.get(this.getFilePath(filename));
                Files.copy(is, uploadLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors du redimensionnement de l'image");
            }
        }
    }

    private String getFilePath(String filename) {
        return applicationBootstrap.getAppConfig().getDataDir() + "/centregestion/logos/" + filename;
    }

    private String getNomFichier(int idFichier, String nomFichier) {
        return idFichier + "_" + nomFichier;
    }
}
