FROM debian:buster-slim as builder

COPY target/ .

RUN mkdir /app
RUN cp -r ./esupstage-*/* /app

WORKDIR /app


FROM openjdk:11-jre-slim

WORKDIR /app

ARG UID
ARG GID
ENV UID=${UID:-1000}
ENV GID=${UID:-1000}

USER $UID:$GIG

COPY --chown="${UID}:${GID}" --from=builder /app/* /app

CMD java -classpath "/app/classes:/app/lib/*" fr.esupportail.esupstage.Application
