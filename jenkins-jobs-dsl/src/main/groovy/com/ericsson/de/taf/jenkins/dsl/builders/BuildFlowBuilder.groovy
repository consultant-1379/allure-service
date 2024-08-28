package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.BuildFlowJob

class BuildFlowBuilder extends AbstractJobBuilder {

    final String buildFlowText

    BuildFlowBuilder(String name, String description,
                     String buildFlowText) {
        super(name, description)
        this.buildFlowText = buildFlowText
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            (delegate as BuildFlowJob).buildFlow buildFlowText
        }
        return job
    }

    @Override
    Job create(DslFactory factory) {
        factory.buildFlowJob name
    }
}
