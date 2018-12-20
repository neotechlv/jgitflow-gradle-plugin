/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.jgitflow.tasks.release

import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.AbstractCommandTask
import io.github.robwin.jgitflow.credentialsprovider.CredentialsProviderHelper
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ReleasePublishTask extends AbstractCommandTask {

    @TaskAction
    void publish() {
        String releaseVersion = project.properties['releaseVersion'] ?: project.properties['version']

        if (releaseVersion == null) {
            throw new GradleException('version or releaseVersion property have to be present')
        }

        CredentialsProviderHelper.getCredentials(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        flow.releasePublish(releaseVersion).releasePublish(releaseVersion).call()
        flow.git().close()
    }
}