package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class DocsPublishJobBuilder extends FreeStyleJobBuilder {

    static final String DESCRIPTION = 'Documentation publishing'

    final String docsBuildJobName

    DocsPublishJobBuilder(String name, String docsBuildJobName) {
        super(name, DESCRIPTION)
        this.docsBuildJobName = docsBuildJobName
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            steps {
                copyArtifacts(docsBuildJobName) {
                    buildSelector {
                        latestSuccessful()
                    }
                    includePatterns "${DOCS_PATH}/${DOCS_ZIP}"
                }
                shell """\
                    targetDir=/proj/PDU_OSS_CI_TAF/taflanding
                    rm -rf \${targetDir}/${JOBS_PREFIX}
                    unzip ${DOCS_PATH}/${DOCS_ZIP} -d \${targetDir}/${JOBS_PREFIX}
                    """.stripIndent()
            }
        }
    }
}
