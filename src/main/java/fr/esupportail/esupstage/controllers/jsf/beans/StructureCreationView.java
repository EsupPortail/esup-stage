package fr.esupportail.esupstage.controllers.jsf.beans;

import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import org.springframework.web.bind.annotation.RequestParam;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("structureCreationView")
@ViewScoped
public class StructureCreationView implements Serializable {

		private Structure structure;
		public Structure getStructure() {
				return this.structure;
		}
		public void updateStructure(@RequestParam String lastname) {
			//	this.structure.setLastname(lastname);
			//	return this.service.updateStudent(this.student)
		}
}
