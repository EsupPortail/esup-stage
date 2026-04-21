package org.esup_portail.esup_stage.service.apogee;

import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.model.RegimeInscriptionApogee;
import org.esup_portail.esup_stage.repository.RegimeInscriptionApogeeJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegimeInscriptionApogeeService {

    private static final String TEM_EN_SERV_OUI = "O";
    private static final String TEM_EN_SERV_NON = "N";

    @Autowired
    RegimeInscriptionApogeeJpaRepository regimeInscriptionApogeeJpaRepository;

    @Autowired
    ApogeeService apogeeService;

    @Transactional
    public List<RegimeInscriptionDto> synchroniserDepuisApogee() {
        return synchroniserDepuisApogee(apogeeService.getRegimesInscriptions());
    }

    @Transactional
    public List<RegimeInscriptionDto> synchroniserDepuisApogee(List<RegimeInscriptionDto> regimesInscriptions) {
        LocalDateTime dateDerniereMaj = LocalDateTime.now();
        List<RegimeInscriptionDto> regimes = regimesInscriptions != null ? regimesInscriptions : List.of();
        Map<String, String> regimesApogeeParCode = regimes.stream()
                .filter(regime -> regime != null && hasText(regime.getCode()))
                .collect(Collectors.toMap(
                        regime -> regime.getCode().trim(),
                        regime -> hasText(regime.getLibelle()) ? regime.getLibelle().trim() : regime.getCode().trim(),
                        (ancienLibelle, nouveauLibelle) -> nouveauLibelle,
                        LinkedHashMap::new
                ));

        Map<String, RegimeInscriptionApogee> regimesExistantsParCode = regimeInscriptionApogeeJpaRepository.findAll().stream()
                .collect(Collectors.toMap(RegimeInscriptionApogee::getCode, regime -> regime));

        List<RegimeInscriptionApogee> regimesASauvegarder = new ArrayList<>();

        for (Map.Entry<String, String> regimeApogee : regimesApogeeParCode.entrySet()) {
            String code = regimeApogee.getKey();
            RegimeInscriptionApogee regime = regimesExistantsParCode.remove(code);
            if (regime == null) {
                regime = new RegimeInscriptionApogee();
                regime.setCode(code);
            }
            regime.setLibelle(regimeApogee.getValue());
            regime.setTemEnServ(TEM_EN_SERV_OUI);
            regime.setDateDerniereMaj(dateDerniereMaj);
            regimesASauvegarder.add(regime);
        }

        for (RegimeInscriptionApogee regimeAbsentApogee : regimesExistantsParCode.values()) {
            regimeAbsentApogee.setTemEnServ(TEM_EN_SERV_NON);
            regimeAbsentApogee.setDateDerniereMaj(dateDerniereMaj);
            regimesASauvegarder.add(regimeAbsentApogee);
        }

        regimeInscriptionApogeeJpaRepository.saveAll(regimesASauvegarder);
        regimeInscriptionApogeeJpaRepository.flush();

        return getRegimesInscriptions();
    }

    public List<RegimeInscriptionDto> getRegimesInscriptions() {
        return regimeInscriptionApogeeJpaRepository.findByTemEnServOrderByLibelle(TEM_EN_SERV_OUI).stream()
                .map(ri -> new RegimeInscriptionDto(ri.getCode(), ri.getLibelle()))
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
