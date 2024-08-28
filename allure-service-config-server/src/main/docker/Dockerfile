FROM armdocker.rnd.ericsson.se/proj_taf_te/allure_base_images/frolvlad/alpine-oraclejre8:slim

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

# Set the reset cache variable
ENV REFRESHED_AT 2017-11-10

RUN apk add --update curl && \
    rm -rf /var/cache/apk/*

ADD allure-service-config-server.jar app.jar
RUN sh -c 'touch /app.jar'

RUN mkdir -p /var/allure-service/config

EXPOSE 8888

VOLUME ["/var/allure-service/config"]

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]