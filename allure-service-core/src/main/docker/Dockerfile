FROM armdocker.rnd.ericsson.se/proj_taf_te/allure_base_images/frolvlad/alpine-oraclejre8:slim

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

# Set the reset cache variable
ENV REFRESHED_AT 2017-05-10

RUN apk add --update curl && \
    rm -rf /var/cache/apk/*

ADD allure-service.jar app.jar
RUN sh -c 'touch /app.jar'

# avoid root user usage
RUN addgroup -S appuser \
    && adduser -S -g appuser appuser

# Create reports directory
RUN mkdir -p /var/allure-reports \
    && chown -R appuser:appuser /var/allure-reports

# Create log directory
RUN mkdir -p /var/log/allure-service \
    && chown -R appuser:appuser /var/log/allure-service

USER appuser

VOLUME ["/tmp", "/var/allure-reports", "/var/log/allure-service"]

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
