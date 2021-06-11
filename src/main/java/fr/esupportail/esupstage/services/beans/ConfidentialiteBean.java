package fr.esupportail.esupstage.services.beans;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfidentialiteBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;

	private String label;

}
