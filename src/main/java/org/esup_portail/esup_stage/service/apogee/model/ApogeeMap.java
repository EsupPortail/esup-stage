package org.esup_portail.esup_stage.service.apogee.model;

import java.util.LinkedHashMap;
import java.util.List;

public class ApogeeMap {
    private List<RegimeInscription> regimeInscription;
    private LinkedHashMap<String,String> StudentSteps;
    private LinkedHashMap<String, String> StudentsEtapesVets;
    private LinkedHashMap<String, String> StudentsEtapesVetsPedago;
    private LinkedHashMap<String,String> StudentStudys;
    private List<ElementPedagogique> listeELPs;
    private List<EtapeInscription> listeEtapeInscriptions;

    public List<RegimeInscription> getRegimeInscription() {
        return regimeInscription;
    }

    public void setRegimeInscription(List<RegimeInscription> regimeInscription) {
        this.regimeInscription = regimeInscription;
    }

    public LinkedHashMap<String, String> getStudentSteps() {
        return StudentSteps;
    }

    public void setStudentSteps(LinkedHashMap<String, String> studentSteps) {
        StudentSteps = studentSteps;
    }

    public LinkedHashMap<String, String> getStudentsEtapesVets() {
        return StudentsEtapesVets;
    }

    public void setStudentsEtapesVets(LinkedHashMap<String, String> studentsEtapesVets) {
        StudentsEtapesVets = studentsEtapesVets;
    }

    public LinkedHashMap<String, String> getStudentsEtapesVetsPedago() {
        return StudentsEtapesVetsPedago;
    }

    public void setStudentsEtapesVetsPedago(LinkedHashMap<String, String> studentsEtapesVetsPedago) {
        StudentsEtapesVetsPedago = studentsEtapesVetsPedago;
    }

    public LinkedHashMap<String, String> getStudentStudys() {
        return StudentStudys;
    }

    public void setStudentStudys(LinkedHashMap<String, String> studentStudys) {
        StudentStudys = studentStudys;
    }

    public List<ElementPedagogique> getListeELPs() {
        return listeELPs;
    }

    public void setListeELPs(List<ElementPedagogique> listeELPs) {
        this.listeELPs = listeELPs;
    }

    public List<EtapeInscription> getListeEtapeInscriptions() {
        return listeEtapeInscriptions;
    }

    public void setListeEtapeInscriptions(List<EtapeInscription> listeEtapeInscriptions) {
        this.listeEtapeInscriptions = listeEtapeInscriptions;
    }
}
