<head>
    <title>Allure Service Installation</title>
</head>

# Introduction
The document describes how to run Allure Service.
The service could run by CLI or within container. 

# Run locally
Execute the following class in project in order to run `allure-service` locally:
```
com.ericsson.de.allure.service.AllureServiceApplication
```
Once it run, the service available at http://localhost:8080/ address.

# Run on container environment
There is basic pipelines which perform build and deploy to the certain environment, see https://fem119-eiffel004.lmera.ericsson.se:8443/jenkins/view/Allure-Service/
