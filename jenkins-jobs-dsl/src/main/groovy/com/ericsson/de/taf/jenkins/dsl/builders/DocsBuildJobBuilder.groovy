package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Git
import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class DocsBuildJobBuilder extends FreeStyleJobBuilder {

    static final String DESCRIPTION = 'Documentation building'

    DocsBuildJobBuilder(String name) {
        super(name, DESCRIPTION)
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            scm {
                Git.simple delegate
            }
            steps {
                Maven.goal delegate, "-pl ${DOCS_MODULE} clean site"
                shell """\
                    cd ${DOCS_PATH}
                    zip ${DOCS_ZIP} -r *
                    """.stripIndent()
            }
            publishers {
                archiveArtifacts "${DOCS_PATH}/${DOCS_ZIP}"
            }
        }
    }
}
