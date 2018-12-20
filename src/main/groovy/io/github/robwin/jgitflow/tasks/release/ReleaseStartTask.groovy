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
import io.github.robwin.jgitflow.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.dependencies.DependencyVerifier
import io.github.robwin.jgitflow.helper.ArtifactHelper
import io.github.robwin.jgitflow.helper.GitHelper
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import io.github.robwin.jgitflow.tasks.AbstractCommandTask
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ReleaseStartTask extends AbstractCommandTask {

    @TaskAction
    void start() {

        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        Git git = flow.git()

        String releaseVersion = GradlePropertiesHelper.getVersionWithoutSnapshot(project)
        GitHelper.requireBranch(flow.getDevelopBranchName(), git)
        DependencyVerifier.checkSnapshots(project)
        validateReleaseVersion(releaseVersion)
        CredentialsProviderHelper.getCredentials(project)

        flow.releaseStart(releaseVersion)
                .setExtension(new ReleaseStartExtension(project, flow, releaseVersion))
                .setFetch(true)
                .setPush(isPushEnabled(project))
                .call()
        git.close()
    }

    private void validateReleaseVersion(String releaseVersion) {
        if (project.version == releaseVersion) {
            throw new GradleException("Release version '${releaseVersion}' and current version " +
                    "'${project.version}' must not be equal.")
        }
        if (ArtifactHelper.isSnapshot(releaseVersion)) {
            throw new GradleException("Release version must not be a snapshot version: ${releaseVersion}")
        }
    }

}