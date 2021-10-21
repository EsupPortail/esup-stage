package org.esup_portail.esup_stage.security;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.common.CasLayer;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenFactory {

	private static final Logger logger = LoggerFactory.getLogger(TokenFactory.class);
	private	static final char TOKEN_SEP	='|';

	public static String create(HttpServletRequest request) {
		String login=null;
		String auth=null;

		// lecture des infos CAS SAML
		if (request.getSession().getAttribute("casUser")!=null){
			login = ((CasLayer.CasUser)request.getSession().getAttribute("casUser")).getLogin();
			auth = "cas";
		} else if (request.getSession().getAttribute("tokenUser")!=null) {
			login = ((Utilisateur)request.getSession().getAttribute("tokenUser")).getId()+"";
			auth = "token";
		}

		if (login!=null) {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Date today = Calendar.getInstance().getTime();
			return TokenFactory.generationToken(login, df.format(today), "Azkjn32klklOP", auth);
		} else {
			return null;
		}
	}

	public static String generationTokenAuth(String login, Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return generationToken(login, df.format(date), "SeldeSurv3ill@nt", "token");
	}

	private static String generationToken(String login, String date, String sel, String auth) {
		String hash = TokenFactory.generateHash(login, date, sel);
		String token = login + TokenFactory.TOKEN_SEP + auth + TokenFactory.TOKEN_SEP + date + TokenFactory.TOKEN_SEP + hash;
		return new String(Base64.getEncoder().encode(token.getBytes()));
	}

	public static String generateHash(String login, String date, String sel) {
		return DigestUtils.md5Hex(login + TokenFactory.TOKEN_SEP + date + TokenFactory.TOKEN_SEP + sel);
	}

	public static Boolean matchTokenPattern(String token) {
		Pattern pattern = Pattern.compile("[-_A-Za-z0-9]+\\|[a-z]+\\|[0-9]{14}\\|.*$");
		Matcher matcher = pattern.matcher(token);
		return matcher.matches();
	}
}
