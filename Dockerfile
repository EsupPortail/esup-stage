# Configuration de l'image Tomcat pour exécution de l'application
FROM tomcat:10.1.35-jre21-temurin-noble AS tomcat-server
ARG VERSION
COPY target/esup-stage-$VERSION.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]