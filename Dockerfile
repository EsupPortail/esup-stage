#FROM maven:3.9.5-eclipse-temurin-21 AS backend-build

# Copier le code source dans l'image
#WORKDIR /app
#COPY . /app

# Construire le fichier WAR
#RUN mvn clean package


# Étape 2 : Configurer l'image Tomcat pour exécuter l'application
FROM tomcat:10.1-jdk21-temurin-noble AS tomcat-server

COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

RUN mkdir -p /etc/estage \
    && mkdir -p /etc/data/centregestion/consigne-documents \
    && mkdir -p /etc/data/centregestion/logo \
    && mkdir -p /etc/data/images

COPY etc/estage/estage.properties /etc/estage/estage.properties

ENV APPLICATION_CONFIG_FILEPATH=file:///etc/estage/estage.properties

EXPOSE 8080

CMD ["catalina.sh", "run"]

