<head>
    <title>Allure Service Configuration</title>
</head>

# Introduction

The Allure Service is configurable to suit your needs.

The configuration is stored in properties which can be changed at start-up or when the service is up.

## At start-up

Edit the allure-service.properties file which is located beside the docker-compose.yml file.

This file contains commented out properties with sample values.

Execute the following command to bring up the service:

```bash
docker-compose up -d
```

## When the service is up

Edit the allure-service.properties file which is located beside the docker-compose.yml file.

The config is refreshed every day at midnight.

You can make a POST request to the **/refresh** endpoint against the management port (81) to trigger a refresh of 
the configuration.

Example Request:

```
curl -v --request POST http://localhost:81/refresh
```

# Configuration Properties

## From a live system

If you have an Allure Service instance up and running you can see the current configuration by making a GET request to 
the **/env** endpoint on the management port (81). Concentrate on the applicationConfig json objects.

Example Request:

```
curl -v http://localhost:81/env
```

Example Response:

```json
{
  "profiles" : [ "test" ],
  "server.ports" : {
    "local.management.port" : 8081,
    "local.server.port" : 8080
  },
  //...
  "applicationConfig: [classpath:/application-test.yml]" : {
      "spring.profiles.active" : "test"
    },
    "applicationConfig: [classpath:/application.yml]" : {
      "spring.profiles.active" : "test",
      "spring.main.banner-mode" : "off",
      "spring.jackson.date-format" : "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "spring.jackson.serialization.INDENT_OUTPUT" : true,
      "endpoints.jmx.enabled" : false,
      "endpoints.actuator.enabled" : "true",
      "endpoints.jolokia.enabled" : false,
      "management.port" : 8081,
      "server.port" : 8080,
      "server.sessionTimeout" : 5000,
      "server.error.whitelabel.enabled" : false,
      "info.build.version" : "1.0.0-SNAPSHOT",
      "info.build.name" : "Allure Report Service Core",
      "info.build.groupId" : "com.ericsson.de",
      "info.build.artifact" : "allure-service-core",
      "info.build.date" : "2017-11-27 12:14",
      "jobs.cleanup.cron" : "0 */10 * * * *",
      "jobs.cleanup.report.lifetime.completed.hours" : 1,
      "jobs.cleanup.report.lifetime.uncompleted.hours" : 24,
      "refresh.cron" : "0 */10 * * * *",
      "upload.path" : "/tmp",
      "upload.dir.name" : "reports_007ef591-e647-4cea-ba67-461511a0969a"
    },
    //...
    "applicationConfig: [classpath:/bootstrap.yml]" : {
        "spring.application.name" : "allure-service",
        "spring.cloud.config.failFast" : true,
        "management.security.enabled" : false
      }
    }         
```

## The list of properties

Here is the list of user configuration properties for the allure service:

