package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.ReponseEvaluation;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseEvaluationJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@ApiController
@RequestMapping("/reponseEvaluation")
public class ReponseEvaluationController {

    @Autowired
    ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public ReponseEvaluation getById(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findById(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvée");
        }
        return reponseEvaluation;
    }

    @GetMapping("/getByConvention/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public ReponseEvaluation getByConvention(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        //if (reponseEvaluation == null) {
        //    reponseEvaluation = new ReponseEvaluation();
        //    Convention convention = conventionJpaRepository.findById(id);
        //    if (convention == null) {
        //        throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        //    }
        //    reponseEvaluation.setConvention(convention);
        //    return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
        //}
        //return reponseEvaluation;
        return new ReponseEvaluation();
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.CREATION})
    public ReponseEvaluation create(@Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        setReponseEvaluationData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public ReponseEvaluation update(@PathVariable("id") int id, @Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findById(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        setReponseEvaluationData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findById(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluationJpaRepository.delete(reponseEvaluation);
        reponseEvaluationJpaRepository.flush();
        return true;
    }

    private void setReponseEvaluationData(ReponseEvaluation reponseEvaluation, ReponseEtudiantFormDto reponseEtudiantFormDto) {

        reponseEvaluation.setReponseEtuI1(reponseEtudiantFormDto.getReponseEtuI1());
        reponseEvaluation.setReponseEtuI1bis(reponseEtudiantFormDto.getReponseEtuI1bis());
        reponseEvaluation.setReponseEtuI2(reponseEtudiantFormDto.getReponseEtuI2());
        reponseEvaluation.setReponseEtuI3(reponseEtudiantFormDto.getReponseEtuI3());
        reponseEvaluation.setReponseEtuI4a(reponseEtudiantFormDto.getReponseEtuI4a());
        reponseEvaluation.setReponseEtuI4b(reponseEtudiantFormDto.getReponseEtuI4b());
        reponseEvaluation.setReponseEtuI4c(reponseEtudiantFormDto.getReponseEtuI4c());
        reponseEvaluation.setReponseEtuI4d(reponseEtudiantFormDto.getReponseEtuI4d());
        reponseEvaluation.setReponseEtuI5(reponseEtudiantFormDto.getReponseEtuI5());
        reponseEvaluation.setReponseEtuI6(reponseEtudiantFormDto.getReponseEtuI6());
        reponseEvaluation.setReponseEtuI7(reponseEtudiantFormDto.getReponseEtuI7());
        reponseEvaluation.setReponseEtuI7bis1(reponseEtudiantFormDto.getReponseEtuI7bis1());
        reponseEvaluation.setReponseEtuI7bis1a(reponseEtudiantFormDto.getReponseEtuI7bis1a());
        reponseEvaluation.setReponseEtuI7bis1b(reponseEtudiantFormDto.getReponseEtuI7bis1b());
        reponseEvaluation.setReponseEtuI7bis2(reponseEtudiantFormDto.getReponseEtuI7bis2());
        reponseEvaluation.setReponseEtuI8(reponseEtudiantFormDto.getReponseEtuI8());
        reponseEvaluation.setReponseEtuII1(reponseEtudiantFormDto.getReponseEtuII1());
        reponseEvaluation.setReponseEtuII1bis(reponseEtudiantFormDto.getReponseEtuII1bis());
        reponseEvaluation.setReponseEtuII2(reponseEtudiantFormDto.getReponseEtuII2());
        reponseEvaluation.setReponseEtuII2bis(reponseEtudiantFormDto.getReponseEtuII2bis());
        reponseEvaluation.setReponseEtuII3(reponseEtudiantFormDto.getReponseEtuII3());
        reponseEvaluation.setReponseEtuII3bis(reponseEtudiantFormDto.getReponseEtuII3bis());
        reponseEvaluation.setReponseEtuII4(reponseEtudiantFormDto.getReponseEtuII4());
        reponseEvaluation.setReponseEtuII5(reponseEtudiantFormDto.getReponseEtuII5());
        reponseEvaluation.setReponseEtuII5a(reponseEtudiantFormDto.getReponseEtuII5a());
        reponseEvaluation.setReponseEtuII5b(reponseEtudiantFormDto.getReponseEtuII5b());
        reponseEvaluation.setReponseEtuII6(reponseEtudiantFormDto.getReponseEtuII6());
        reponseEvaluation.setReponseEtuIII1(reponseEtudiantFormDto.getReponseEtuIII1());
        reponseEvaluation.setReponseEtuIII1bis(reponseEtudiantFormDto.getReponseEtuIII1bis());
        reponseEvaluation.setReponseEtuIII2(reponseEtudiantFormDto.getReponseEtuIII2());
        reponseEvaluation.setReponseEtuIII2bis(reponseEtudiantFormDto.getReponseEtuIII2bis());
        reponseEvaluation.setReponseEtuIII3(reponseEtudiantFormDto.getReponseEtuIII3());
        reponseEvaluation.setReponseEtuIII3bis(reponseEtudiantFormDto.getReponseEtuIII3bis());
        reponseEvaluation.setReponseEtuIII4(reponseEtudiantFormDto.getReponseEtuIII4());
        reponseEvaluation.setReponseEtuIII5a(reponseEtudiantFormDto.getReponseEtuIII5a());
        reponseEvaluation.setReponseEtuIII5b(reponseEtudiantFormDto.getReponseEtuIII5b());
        reponseEvaluation.setReponseEtuIII5c(reponseEtudiantFormDto.getReponseEtuIII5c());
        reponseEvaluation.setReponseEtuIII5bis(reponseEtudiantFormDto.getReponseEtuIII5bis());
        reponseEvaluation.setReponseEtuIII6(reponseEtudiantFormDto.getReponseEtuIII6());
        reponseEvaluation.setReponseEtuIII6bis(reponseEtudiantFormDto.getReponseEtuIII6bis());
        reponseEvaluation.setReponseEtuIII7(reponseEtudiantFormDto.getReponseEtuIII7());
        reponseEvaluation.setReponseEtuIII7bis(reponseEtudiantFormDto.getReponseEtuIII7bis());
        reponseEvaluation.setReponseEtuIII8(reponseEtudiantFormDto.getReponseEtuIII8());
        reponseEvaluation.setReponseEtuIII8bis(reponseEtudiantFormDto.getReponseEtuIII8bis());
        reponseEvaluation.setReponseEtuIII9(reponseEtudiantFormDto.getReponseEtuIII9());
        reponseEvaluation.setReponseEtuIII9bis(reponseEtudiantFormDto.getReponseEtuIII9bis());
        reponseEvaluation.setReponseEtuIII10(reponseEtudiantFormDto.getReponseEtuIII10());
        reponseEvaluation.setReponseEtuIII11(reponseEtudiantFormDto.getReponseEtuIII11());
        reponseEvaluation.setReponseEtuIII12(reponseEtudiantFormDto.getReponseEtuIII12());
        reponseEvaluation.setReponseEtuIII13(reponseEtudiantFormDto.getReponseEtuIII13());
        reponseEvaluation.setReponseEtuIII14(reponseEtudiantFormDto.getReponseEtuIII14());
        reponseEvaluation.setReponseEtuIII15(reponseEtudiantFormDto.getReponseEtuIII15());
        reponseEvaluation.setReponseEtuIII15bis(reponseEtudiantFormDto.getReponseEtuIII15bis());
        reponseEvaluation.setReponseEtuIII16(reponseEtudiantFormDto.getReponseEtuIII16());
        reponseEvaluation.setReponseEtuIII16bis(reponseEtudiantFormDto.getReponseEtuIII16bis());
    }
}