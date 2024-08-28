package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_CENTRAL
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_BRANCH
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_PROJECT
import static com.ericsson.de.taf.jenkins.dsl.Constants.JDK_SYSTEM
import static com.ericsson.de.taf.jenkins.dsl.Constants.SLAVE_TAF_MAIN

class ChangeLogBuilder extends FreeStyleJobBuilder {
    final String mavenGoal

    ChangeLogBuilder(String name,
                     String description,
                     String mavenGoal ) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            scm {
                git {
                    remote {
                        name 'gc'
                        url "${GERRIT_CENTRAL}/${GIT_PROJECT}"
                    }
                    branch GIT_BRANCH
                    extensions {
                        cleanBeforeCheckout()
                    }
                }
            }
            jdk(JDK_SYSTEM)
            label(SLAVE_TAF_MAIN)
            steps{
                Maven.goal delegate, mavenGoal
                shell('cd ${PWD}/target\n' +
                        'targetDir=/proj/PDU_OSS_CI_TAF/taflanding/allure-release\n' +
                        'rm -f ${targetDir}/changelog.html\n' +
                        'cp changelog.html ${targetDir}/changelog.html')
            }
        }
    }
}