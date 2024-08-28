package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.MavenJob

import static com.ericsson.de.taf.jenkins.dsl.Constants.*
import static com.ericsson.de.taf.jenkins.dsl.utils.Maven.MAVEN_OPTIONS
import static com.ericsson.de.taf.jenkins.dsl.utils.Maven.MAVEN_VERSION

@SuppressWarnings("GroovyAssignabilityCheck")
class ReleaseJobBuilder extends MavenJobBuilder {

    static final String DESCRIPTION = 'Release to Nexus'

    final String buildFlowJobName

    ReleaseJobBuilder(String name, String buildFlowJobName) {
        super(name, DESCRIPTION)
        this.buildFlowJobName = buildFlowJobName
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        buildMaven job
    }

    MavenJob buildMaven(MavenJob job) {
        job.with {
            blockOn(buildFlowJobName) {
                blockLevel 'GLOBAL'
            }
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
                        perBuildTag()
                        cleanAfterCheckout()
                        disableRemotePoll()
                    }
                    configure {
                        def ext = it / 'extensions'
                        def pkg = 'hudson.plugins.git.extensions.impl'
                        ext / "${pkg}.UserExclusion" << excludedUsers('Jenkins Release')
                        ext / "${pkg}.UserIdentity" << name('Jenkins Release')
                    }
                }
            }
            preBuildSteps {
                shell """\
                    export GIT_URL=\${GIT_URL_1}
                    
                    #cannot push back to gerrit mirror so need to set url to GC
                    repo=\$(echo \$GIT_URL | sed 's#.*OSS/##g')
                    
                    git remote set-url --push gc \${GERRIT_CENTRAL}/OSS/\${repo}
                    
                    #run script to check gerrit mirror sync
                    /proj/litpadm200/data/scripts/check_gerrit_sync.sh
                    
                    git checkout ${GIT_BRANCH} || git checkout -b ${GIT_BRANCH}
                    git reset --hard gm/${GIT_BRANCH}
                    """.stripIndent()
            }
            mavenInstallation MAVEN_VERSION
            goals """\
                ${MAVEN_OPTIONS} -Dresume=false -DlocalCheckout=true -pl \"!${JOBS_MODULE}\" 
                release:prepare -DpreparationGoals="clean install -DskipTests" 
                release:perform -Dgoals="clean deploy -DskipTests"
                """.stripIndent()
            mavenOpts '-XX:MaxPermSize=1024m'
            configure {
                it / 'runPostStepsIfResult' << name('SUCCESS')
            }
            publishers {
                git {
                    pushOnlyIfSuccess()
                    branch 'gc', GIT_BRANCH
                }
            }
        }
        return job
    }
}
