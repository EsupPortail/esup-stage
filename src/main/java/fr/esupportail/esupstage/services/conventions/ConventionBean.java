package fr.esupportail.esupstage.services.conventions;

import fr.esupportail.esupstage.controllers.jsf.beans.conventions.StudentBean;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;

public class ConventionBean {
    StudentBean student;

    static ConventionBean map(Convention convention){
        ConventionBean convBean = new ConventionBean();
        convBean.student = StudentBean.map(convention.getEtudiant());
        return convBean;
    }
}
