package fr.esupportail.esupstage.services.conventions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.esupportail.esupstage.controllers.jsf.beans.conventions.StudentBean;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConventionBean {

    private static Map<Integer, ConventionBean> instances = new HashMap<>();

    private Integer id;
    private boolean acceptedGuidlines = false;
    private StudentBean student;

    // TODO: map ConventionBean to Convention params to save all infos
    private String institution = "Flavien CADET";

    private LocalDate[] localPeriode = new LocalDate[] { LocalDate.now(), LocalDate.now() };

    private String etape = "1";

    private boolean validated = false;
    private boolean avenant = false;
    private String anneeUniv = "2020-2021";

    public ConventionBean() {
        this.id = ConventionBean.instances.size();
        ConventionBean.instances.put(this.id, this);
    }

    static ConventionBean map(Convention convention) {
        ConventionBean convBean = new ConventionBean();
        convBean.student = StudentBean.map(convention.getEtudiant());
        return convBean;
    }

    public void acceptGuidlines() {
        this.acceptedGuidlines = true;
    }

    public static ConventionBean get(Integer id) {
        return ConventionBean.instances.get(id);
    }

    public static List<ConventionBean> getAll() {
        return new ArrayList<ConventionBean>(ConventionBean.instances.values());
    }
}
