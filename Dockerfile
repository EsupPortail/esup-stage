# Configuration de l'image Tomcat pour exécution de l'application
FROM tomcat:10.1.48-jre21-temurin-noble AS tomcat-server
ARG VERSION
COPY target/esup-stage-$VERSION.war /usr/local/tomcat/webapps/ROOT.war

# Installer tzdata pour gérer les fuseaux horaires (si ce n'est pas déjà inclus)
RUN apt-get update && apt-get install -y tzdata

# Définir le fuseau horaire sur Europe/Paris
ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

CMD ["catalina.sh", "run"]