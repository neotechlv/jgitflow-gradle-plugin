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
import io.github.robwin.jgitflow.dependencies.DependencyVerifier
import io.github.robwin.jgitflow.helper.ArtifactHelper
import io.github.robwin.jgitflow.helper.GitHelper
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import io.github.robwin.jgitflow.tasks.AbstractCommandTask
import org.eclipse.jgit.api.Git
import org.gradle.api.tasks.TaskAction

class HotfixStartTask extends AbstractCommandTask {

    static final String HOTFIX_VERSION_INCREMENT = "PATCH"

    @TaskAction
    void start() {
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        Git git = flow.git()

        GitHelper.requireBranch(flow.getMasterBranchName(), git)
        DependencyVerifier.checkSnapshots(project)
        CredentialsProviderHelper.getCredentials(project)

        String currentVersion = GradlePropertiesHelper.getVersion(project)
        String hotFixVersion = ArtifactHelper.incrementVersion(currentVersion, HOTFIX_VERSION_INCREMENT)
        flow.hotfixStart(hotFixVersion)
                .setFetch(true)
                .setPush(isPushEnabled(project))
                .setExtension(new HotFixStartExtension(project, flow, hotFixVersion))
                .call()

        git.close()
    }
}