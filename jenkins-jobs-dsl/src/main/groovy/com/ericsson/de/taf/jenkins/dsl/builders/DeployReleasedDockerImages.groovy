package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_CENTRAL
import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_MIRROR
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_BRANCH
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_PROJECT
import static com.ericsson.de.taf.jenkins.dsl.Constants.JDK_1_8_DOCKER
import static com.ericsson.de.taf.jenkins.dsl.Constants.SLAVE_DOCKER_POD_H

class DeployReleasedDockerImages extends FreeStyleJobBuilder {

    final String mavenGoal

    DeployReleasedDockerImages(String name,
                               String description,
                               String mavenGoal) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    String readFile(String path, def dslFactory) {
        return dslFactory.readFileFromWorkspace('jenkins-jobs-dsl/shell/' + "${path}")
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
                    }
                }
            }
            jdk(JDK_1_8_DOCKER)
            label(SLAVE_DOCKER_POD_H)

            steps {
                shell """\
                    export GIT_URL=\${GIT_URL_1}

                    #cannot fetch tag if remote url not set
                    repo=\$(echo \$GIT_URL | sed 's#.*OSS/##g')

                    git remote set-url origin \${GERRIT_CENTRAL}/OSS/\${repo}
                    """.stripIndent()

                shell(readFile('latest_tag.sh', factory))

                Maven.goal delegate, mavenGoal
            }

        }
    }
}
