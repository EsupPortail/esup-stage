# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.154.2/containers/java/.devcontainer/base.Dockerfile

# [Choice] Java version: 8, 11, 15
ARG VARIANT="15"

FROM openjdk:${VARIANT}

COPY ./build/libs/esupstage-*.jar /app/esupstage.jar

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

CMD [ "java", "-jar", "/app/esupstage.jar" ]
