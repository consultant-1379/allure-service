/*
Jenkins Job DSL API Viewer - https://jenkinsci.github.io/job-dsl-plugin/
Jenkins Job DSL Playground - http://job-dsl.herokuapp.com/
*/


import com.ericsson.cifwk.taf.ui.builders.*
import com.ericsson.de.taf.jenkins.dsl.builders.BuildFlowBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.ChangeLogBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.DeployReleasedDockerImages
import com.ericsson.de.taf.jenkins.dsl.builders.DeployToTestEnv
import com.ericsson.de.taf.jenkins.dsl.builders.DocsBuildJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.DocsPublishJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.GerritJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.MasterBuildFlowBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.ReleaseJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.SimpleJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.SonarQubeGerritJobBuilder
import com.ericsson.de.taf.jenkins.dsl.builders.DeployDockerImagesSnapshots
import javaposse.jobdsl.dsl.DslFactory

def mvnUnitTest = "clean install"
def mvnITest = "${mvnUnitTest} -Pitest"
def mvnDeploy = "clean deploy -DskipTests"
def mvnPushDocker = "clean package -DskipTests -PdockerImages -DpushImage"
def mvnChangeLog = "com.ericsson.cifwk.taf:tafchangelog-maven-plugin:1.0.14:generate -X -e"


def unitTests = 'Unit tests'
def iTests = 'Integration tests'
def snapshots = 'Snapshots deployment'
def deployDockerSnapshots ='Deploy Allure service docker snapshot images to Artifactory'
def deployTestEnv = 'Deploy Test Environment'
def deployDockerRelease = 'Deploy allure service docker release image to Artifactory'
def releaseFlow = 'Release flow to release and build image'
def changeLogDescription = 'Creates the changelog html page and uploads to taflanding'

def aa = new GerritJobBuilder('AA-gerrit-unit-tests', unitTests, mvnUnitTest)
def ab = new GerritJobBuilder('AB-gerrit-integration-tests', iTests, mvnITest)
def ac = new SonarQubeGerritJobBuilder('AC-gerrit-sonar-qube')

def ba = new SimpleJobBuilder('BA-unit-tests', unitTests, mvnUnitTest)
def bb = new SimpleJobBuilder('BB-integration-tests', iTests, mvnITest)

def bc = new SimpleJobBuilder('BC-deploy-snapshots', snapshots, mvnDeploy)
def bd = new DeployDockerImagesSnapshots('BD-Deploy-Docker-images-snapshot', deployDockerSnapshots)
def be = new DeployToTestEnv('BE-Deploy-To-Test_Env', deployTestEnv)

def build = new MasterBuildFlowBuilder('B-build-flow',
        """\
        parallel(
            { 
                build '${ba.name}' 
            },
            { 
                build '${bb.name}' 
            }
        )
        parallel(
            { 
                build '${bc.name}'
                build '${bd.name}'
                build '${be.name}'
            }
        )
        """.stripIndent())

def ca = new ReleaseJobBuilder('CA-release', build.name)
def cb = new DeployReleasedDockerImages('CB-Deploy-Released-Docker-images', deployDockerRelease, mvnPushDocker)
def cc = new DocsBuildJobBuilder('CC-docs-build')
def cd = new DocsPublishJobBuilder('CD-docs-publish', cc.name)
def ce = new ChangeLogBuilder('CE-Changelog', changeLogDescription, mvnChangeLog)

def buildReleaseFlow = new BuildFlowBuilder('C-release-flow', releaseFlow,
        """\
        build '${ca.name}'
        build '${cb.name}'
        build '${cc.name}'
        build '${cd.name}'
        build '${ce.name}'
        """.stripIndent())

[aa, ab, ac, ba, bb, bc, bd, be, ca, cb, cc, cd, ce, build, buildReleaseFlow]*.build(this as DslFactory)
