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
package io.github.robwin.jgitflow.tasks.hotfix

import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.tasks.AbstractCommandTask
import org.gradle.api.tasks.TaskAction

class HotfixPublishTask extends AbstractCommandTask {

    @TaskAction
    void publish() {
        String hotfixName = project.property('hotfixName')
        CredentialsProviderHelper.getCredentials(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        flow.hotfixPublish(hotfixName).call()
        flow.git().close()
    }
}
