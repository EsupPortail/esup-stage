package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.QuestionEvaluation;
import org.esup_portail.esup_stage.repository.QuestionEvaluationJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiController
@RequestMapping("/questions")
public class QuestionEvaluationController {

    @Autowired
    private QuestionEvaluationJpaRepository questionEvaluationJpaRepository;

    private record QuestionDto(
            String code,
            String texte,
            TypeQuestionEvaluation type,
            java.util.List<String> options,
            String bisQuestion,
            Boolean bisQuestionLowNotation,
            Boolean bisQuestionTrue,
            Boolean bisQuestionFalse
    ) {}

    @GetMapping
    public List<QuestionDto> getQuestions(@RequestParam(value = "expand", required = false) String expand) {
        var list = questionEvaluationJpaRepository.findAll();
        if (list.isEmpty()) throw new AppException(HttpStatus.NOT_FOUND,"aucune question trouvée");
        boolean withOptions = "options".equalsIgnoreCase(expand);
        return list.stream()
                .map(q -> new QuestionDto(
                        q.getCode(),
                        q.getTexte(),
                        q.getType(),
                        withOptions ? buildOptions(q) : List.of(),
                        q.getBisQuestion(),
                        q.getBisQuestionLowNotation(),
                        q.getBisQuestionTrue(),
                        q.getBisQuestionFalse()
                ))
                .toList();
    }

    @GetMapping("/{code}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public  QuestionEvaluation getQuestionEvaluation(@PathVariable("code") String code) {
        QuestionEvaluation question = questionEvaluationJpaRepository.findByCode(code);
        if(question == null) {
            throw new AppException(HttpStatus.NOT_FOUND,"aucune question trouvée");
        }
        return question;
    }

    @GetMapping("/etu")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<QuestionEvaluation> getQuestionsEtu() {
        List<QuestionEvaluation> questions = questionEvaluationJpaRepository.findByCodeContaining("ETU");
        if(questions.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND,"aucune question trouvée");
        }
        return questions;
    }

    @GetMapping("/ens")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<QuestionEvaluation> getQuestionsEns() {
        List<QuestionEvaluation> questions = questionEvaluationJpaRepository.findByCodeContaining("ENS");
        if(questions.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND,"aucune question trouvée");
        }
        return questions;
    }

    @GetMapping("/ent")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<QuestionEvaluation> getQuestionsEnt() {
        List<QuestionEvaluation> questions = questionEvaluationJpaRepository.findByCodeContaining("ENT");
        if(questions.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND,"aucune question trouvée");
        }
        return questions;
    }

    //=============== Helpers =================//

    private List<String> buildOptions(QuestionEvaluation q) {
        // si paramsJson contient {"options":[...]} ou {"items":[...]} on les renvoie
        List<String> fromJson = tryReadList(q.getParamsJson(), List.of("options","items"));
        if (!fromJson.isEmpty()) return fromJson;

        return switch (q.getType()) {
            case SCALE_LIKERT_5 -> List.of("Excellent","Très bien","Bien","Satisfaisant","Insuffisant");
            case SCALE_AGREEMENT_5 -> List.of("Tout à fait d'accord","Plutôt d'accord","Sans avis","Plutôt pas d'accord","Pas du tout d'accord");
            default -> List.of(); // YES_NO, TEXT n'ont pas d'options
        };
    }

    // parse très basique (Jackson idéalement)
    private List<String> tryReadList(String json, List<String> keys) {
        if (json == null || json.isBlank()) return List.of();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(json);
            for (String k : keys) {
                var arr = node.get(k);
                if (arr != null && arr.isArray()) {
                    List<String> out = new java.util.ArrayList<>();
                    arr.forEach(n -> out.add(n.asText()));
                    return out;
                }
            }
        } catch (Exception ignored) {}
        return List.of();
    }
}
