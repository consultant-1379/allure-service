package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class DeployToTestEnv extends FreeStyleJobBuilder {

    DeployToTestEnv(String name, String description) {
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
            label(SLAVE_ALLURE_TEST_EVN)
            steps {
                shell("""
                        if [ ! -d '/var/log/allure-service' ] 
                        then 
                            mkdir /var/log/allure-service;
                            chmod 777 /var/log/allure-service; 
                        fi
                        """)
                shell("chmod +x  jenkins-jobs-dsl/shell/docker/test/deploy.sh")
                shell("chmod +x  jenkins-jobs-dsl/shell/docker/test/run.sh")
                shell("jenkins-jobs-dsl/shell/docker/test/deploy.sh")
            }
        }
    }
}
