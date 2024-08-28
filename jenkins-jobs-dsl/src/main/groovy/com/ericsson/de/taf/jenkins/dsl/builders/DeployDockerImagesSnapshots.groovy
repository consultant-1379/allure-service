package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class DeployDockerImagesSnapshots extends FreeStyleJobBuilder {

    DeployDockerImagesSnapshots(String name, String description) {
        super(name, description)
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            scm {
                git {
                    remote {
                        name 'gm'
                        url "${GERRIT_MIRROR}/${GIT_PROJECT}"
                    }
                    remote {
                        name 'gc'
                        url "${GERRIT_CENTRAL}/${GIT_PROJECT}"
                    }
                    branch GIT_BRANCH
                    extensions {
                        cleanAfterCheckout()
                        disableRemotePoll()
                    }
                }
            }
            jdk(JDK_1_8_DOCKER)
            label(SLAVE_DOCKER_POD_H)
            steps {
                Maven.goal delegate, "clean install -DskipTests -PdockerImages"
            }
        }
    }
}
