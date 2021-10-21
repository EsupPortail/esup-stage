package org.esup_portail.esup_stage.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
@Scope("singleton")
public class ApplicationBootstrap {
    private static final Logger logger	= LogManager.getLogger(ApplicationBootstrap.class);

    public	static	final	String			FPROPS_FILECONFIGURATOR	="application.properties";
    public	static	final	String			PROP_CONFIG_FILEPATH	="application.configfilepath";
    public	static	final	String			PROP_APPLI_VERSION		="application.version";

    // Attributs
    private	boolean isConfigured = false;
    private	String applicationVersion =null;
    private	String applicationConfigPath = null;
    private AppConfig appConfig = null;

    @PostConstruct
    public void initialize(){
        logger.info("BootStrap.initialize()");
        this.setConfigured(false);

        // lecture des 2 fichiers de properties
        final Properties appProperties	=new Properties();
        try {
            appProperties.load(ApplicationBootstrap.class.getClassLoader().getResourceAsStream(FPROPS_FILECONFIGURATOR));
        } catch (IOException ex) {
            logger.error("Can not read '<<classpath>>/"+FPROPS_FILECONFIGURATOR+"' file");
            ex.printStackTrace();
        }
        final Properties versionProperties	=new Properties();
        try {
            versionProperties.load(ApplicationBootstrap.class.getClassLoader().getResourceAsStream(FPROPS_FILECONFIGURATOR));
        } catch (IOException ex) {
            logger.error("Can not read '<<classpath>>/"+FPROPS_FILECONFIGURATOR+"' file");
            ex.printStackTrace();
        }


        // configuration principal
        if ( appProperties.containsKey(PROP_CONFIG_FILEPATH)){
            String configFilepath	=appProperties.getProperty(PROP_CONFIG_FILEPATH);
            this.setApplicationConfigPath(configFilepath);


            final Properties applicationProperties	=new Properties();
            try {
                // lecture de toutes les proprietes
                InputStream in = new FileInputStream(ResourceUtils.getFile(configFilepath));
                applicationProperties.load(in);

                // lecture de la configuration : AppConfig
                logger.info("app config...");
                this.setAppConfig( new AppConfig() );
                this.getAppConfig().initProperties( applicationProperties, "appli.");
                logger.info("app config : "+this.getAppConfig());

                // chargement des configurations effectué
                this.setConfigured(true);

            } catch (IOException ex) {
                logger.error("Can not read '"+configFilepath+"' file");
                logger.error("  exception : ", ex);
                logger.error("Erreur trace : "+ ex.getClass().getName());
                logger.error("  message : "+ ex.getMessage());
                logger.error("  cause   : "+ ex.getCause());
                StackTraceElement[] tab	=ex.getStackTrace();
                for(int i=0;i<tab.length;i++)
                    logger.error("    -- trace ["+i+"]  : "+ tab[i] );
                this.setConfigured(false);
            }
        } else {
            logger.error("- application configuration file manquante");
            this.setConfigured(false);
        }

        // version de l'application
        if ( versionProperties.containsKey(PROP_APPLI_VERSION) ){
            this.setApplicationVersion( versionProperties.getProperty(PROP_APPLI_VERSION) );
            logger.info("- application version : "+this.getApplicationVersion());
            this.setConfigured(true);
        } else {
            logger.error("- application version manquante");
            this.setConfigured(false);
        }

        logger.info("BootStrap.initialize -> "+this.isConfigured());
    }


    @PreDestroy
    public void destroy() {
        logger.info("BootStrap.destroy()");
    }

    public boolean isConfigured() {
        return isConfigured;
    }
    public void setConfigured(boolean isConfigured) {
        this.isConfigured = isConfigured;
    }

    /**
     * @return the applicationVersion
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }


    /**
     * @param applicationVersion the applicationVersion to set
     */
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }


    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    /**
     * @return the applicationConfigPath
     */
    public String getApplicationConfigPath() {
        return applicationConfigPath;
    }


    /**
     * @param applicationConfigPath the applicationConfigPath to set
     */
    protected void setApplicationConfigPath(String applicationConfigPath) {
        this.applicationConfigPath = applicationConfigPath;
    }
}
