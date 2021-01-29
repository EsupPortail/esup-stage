# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.154.2/containers/java/.devcontainer/base.Dockerfile

# [Choice] Java version: 8, 11, 15
ARG VARIANT="15"

FROM gradle:jdk${VARIANT} AS builder

WORKDIR /app

COPY . /app

RUN gradle bootJar

FROM openjdk:${VARIANT}

COPY --from=builder /app/build/libs/esupstage-*.jar /app/esupstage.jar

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

CMD [ "java", "-jar", "/app/esupstage.jar" ]
