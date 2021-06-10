package fr.esupportail.esupstage.utils.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("YesNoConverter")
public class YesNoConverter implements Converter<Boolean> {

	private final static String YES = "Oui";

	private final static String NO = "Non";

	@Override
	public Boolean getAsObject(final FacesContext context, final UIComponent component, final String value) {
		final Boolean result;
		if (YES.equalsIgnoreCase(value)) {
			result = Boolean.TRUE;
		} else if (NO.equalsIgnoreCase(value)) {
			result = Boolean.FALSE;
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Boolean value) {
		final String result;
		if (null == value) {
			result = null;
		} else if (value) {
			result = YES;
		} else {
			result = NO;
		}
		return result;
	}

}
