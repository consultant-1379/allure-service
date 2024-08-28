package com.ericsson.de.taf.jenkins.dsl.utils

import javaposse.jobdsl.dsl.helpers.step.StepContext

final class Maven {

    static final String MAVEN_VERSION = 'Maven 3.3.3'
    static final String MAVEN_OPTIONS = '-e -U -V'

    private Maven() {
    }

    static def goal(StepContext steps, goal) {
        steps.with {
            maven {
                mavenInstallation MAVEN_VERSION
                goals "${MAVEN_OPTIONS} ${goal}"
                mavenOpts '-Xms256m'
                mavenOpts '-Xmx512m'
            }
        }
    }
}
