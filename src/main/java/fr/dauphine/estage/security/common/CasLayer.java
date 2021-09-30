package fr.dauphine.estage.security.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dauphine.estage.exception.ApplicationClientException;
import fr.dauphine.estage.exception.ApplicationClientInterruption;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CasLayer {
    private static final Logger logger	= LogManager.getLogger(CasLayer.class);
    public	final static String	CASRESPONSE_ENCODING	="UTF-8";

    private String casUrl	=null;

    public CasLayer(String casUrl) {
        this.casUrl = casUrl;
    }


    public CasUser getCasUser(String ticket, String url) throws ApplicationClientException {
        String signature	=this.getClass().getSimpleName()+".getLogin(ticket="+ticket+", url="+url+")";
        logger.info(signature);

        CasUser casUser	= null;
        // Pre requis
        if( ticket!=null && url!=null ) {
            String	strCasValidateUrl	=null;
            String	strCasResponse		=null;
            try {
                // CAS VALIDATE URL
                strCasValidateUrl = this.casUrl
                        .replace("{service}", URLEncoder.encode(url, "UTF-8") )
                        .replace("{ticket}", URLEncoder.encode(ticket, "UTF-8") );

                // Appel GET
                DefaultHttpClient client = new DefaultHttpClient();
                try {
                    HttpGet get = new HttpGet(strCasValidateUrl);
                    get.addHeader("accept", "application/json");
                    logger.debug("call -> do call ...");
                    HttpResponse response = client.execute(get);
                    logger.debug("call -> parse response ...");
                    String encoding	=CASRESPONSE_ENCODING;
                    if ( response.getEntity().getContentEncoding()!=null && response.getEntity().getContentEncoding().getValue()!=null )
                        encoding=response.getEntity().getContentEncoding().getValue();
                    String body = IOUtils.toString( response.getEntity().getContent(), encoding);
                    logger.debug("call -> body response : "+body);
                    strCasResponse	=body;

                    logger.debug(signature+" -> http-body-response : "+strCasResponse);

                    if( response.getStatusLine().getStatusCode()<200 || response.getStatusLine().getStatusCode()>203 ) {
                        logger.warn("invalid http code response : "+response.getStatusLine().getStatusCode());
                        logger.warn("WebService response :\n");
                        logger.warn( body );
                        // parsing de la reponse json (parsing de l'exception)
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
                        try {
                            // l'api de conversion JSON vers ApplicationClientInterruption (ne supporte pas les sauts de lignes)
                            ApplicationClientInterruption ai = mapper.readValue(strCasResponse.replaceAll("\n", ""), ApplicationClientInterruption.class);
                            throw new ApplicationClientException(ai);
                        } catch (IOException e) {
                            logger.info("l'application web n'arrive pas a parser l'exception de webservice > ["+e.getClass()+"] "+e);
                            throw new ApplicationClientException(new ApplicationClientInterruption(response.getStatusLine().getStatusCode(), "Une erreur interne est survenue", "l'application webservice a declencher une erreur inattendu par l'application web, probablement un probléme de droit d'accés"));
                        }
                    } else {

                        // ANALYSE DU JSON RESULTAT

                        try {
                            JSONObject result = new JSONObject(strCasResponse);
                            logger.debug(signature + " -> result : "+ result);
                            if(  	result!=null
                                    && result.has("serviceResponse")
                                    && result.getJSONObject("serviceResponse").has("authenticationSuccess")
                                    && result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").has("user")) {
                                casUser = new CasUser();
                                casUser.setLogin(getStringValue(result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess"), "user"));
                                casUser.setMail(getStringValue(result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").getJSONObject("attributes"), "mail"));
                                casUser.setNom(getStringValue(result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").getJSONObject("attributes"), "sn"));
                                casUser.setPrenom(getStringValue(result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").getJSONObject("attributes"), "givenName"));
                                casUser.setNumEtudiant(getStringValue(result.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").getJSONObject("attributes"), "uid"));

                                logger.info(signature + " -> login :" + casUser.getLogin());
                            }
                        }catch(org.json.JSONException ex) {
                            logger.error("Exception : "+ex.getClass().getName());
                            logger.error("   message : "+ex.getMessage());
                            logger.error("Context : cas validate");
                            logger.error("   > url     : "+casUrl);
                            logger.error("   > response: "+strCasResponse);
                            logger.error("Trace :");
                            StackTraceElement[] tabex=ex.getStackTrace();
                            for(int i=0;i<tabex.length;i++){
                                logger.error("  ["+i+"] "+tabex[i].toString());
                            }
                        }

                    }
                } catch (Exception e) {
                    logger.error("["+e.getClass()+"] "+e);
                } finally {
                    client.getConnectionManager().shutdown();
                }
            }catch (UnsupportedEncodingException ex) {
                logger.error("Exception : "+ex.getClass().getName());
                logger.error("   message : "+ex.getMessage());
                logger.error("Context : cas validate");
                logger.error("   generate validate url : "+this.casUrl);
                logger.error("   > param url    : "+url);
                logger.error("   > param ticket : "+ticket);
                logger.error("Trace :");
                StackTraceElement[] tabex=ex.getStackTrace();
                for(int i=0;i<tabex.length;i++){
                    logger.error("  ["+i+"] "+tabex[i].toString());
                }
            }catch(org.json.JSONException ex) {
                logger.error("Exception : "+ex.getClass().getName());
                logger.error("   message : "+ex.getMessage());
                logger.error("Context : cas validate");
                logger.error("   > url     : "+strCasValidateUrl);
                logger.error("   > response: "+strCasResponse);
                logger.error("Trace :");
                StackTraceElement[] tabex=ex.getStackTrace();
                for(int i=0;i<tabex.length;i++){
                    logger.error("  ["+i+"] "+tabex[i].toString());
                }
            }


        } else {
            logger.warn(signature+" -> missing parameters");
        }

        return casUser;
    }

    public String getStringValue(JSONObject jsonObject, String attrName) {
        String value = null;
        Object obj = jsonObject.get(attrName);
        if (obj != null) {

            if (obj instanceof String)
                value = (String) obj;
            else if (obj instanceof JSONArray && ((JSONArray) obj).length() > 0 && ((JSONArray) obj).get(0) instanceof String)
                value = (String) ((JSONArray) obj).get(0);

        }
        return value;
    }

    public String getCasUrl() {
        return casUrl;
    }

    public void setCasUrl(String casUrl) {
        this.casUrl = casUrl;
    }

    // TODO parse object SAML

    public static class CasUser {
        private String login;
        private String mail;
        private String nom;
        private String prenom;
        private String numEtudiant;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getPrenom() {
            return prenom;
        }

        public void setPrenom(String prenom) {
            this.prenom = prenom;
        }

        public String getNumEtudiant() {
            return numEtudiant;
        }

        public void setNumEtudiant(String numEtudiant) {
            this.numEtudiant = numEtudiant;
        }
    }
}
