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
package io.github.robwin.jgitflow.tasks

import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.tasks.helper.ArtifactHelper
import io.github.robwin.jgitflow.tasks.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.BranchTrackingStatus
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile

class HotfixStartTask extends AbstractCommandTask {

    static final String HOTFIX_VERSION_INCREMENT = "PATCH"

    @TaskAction
    void start() {

        CredentialsProviderHelper.requireCredentials(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        Git git = flow.git()

        String masterBranch = flow.getMasterBranchName()
        String currentBranch = git.getRepository().getBranch()

        if (currentBranch != masterBranch) {
            throw new GradleException("Please switch to " + masterBranch
                    + " branch. Currently using: " + currentBranch)
        }

        git.fetch().call()

        BranchTrackingStatus status = BranchTrackingStatus.of(git.getRepository(), currentBranch)
        if (status.getBehindCount() > 0) {
            throw new GradleException("Please update your branch to match remote")
        }

        String currentVersion = GradlePropertiesHelper.getProjectVersion(project)
        String hotFixVersion = ArtifactHelper.incrementVersion(currentVersion, HOTFIX_VERSION_INCREMENT)

        def command = flow.hotfixStart(hotFixVersion)

        setCommandPrefixAndSuffix(command)

        //Start a hotFix
        if (project.hasProperty('baseCommit')) {
            String baseCommit = project.property('baseCommit')
            command.setStartCommit(baseCommit)
        }

        command.call()

        GradlePropertiesHelper.updateGradlePropertiesFile(project, hotFixVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), getScmMessagePrefix(command) + "Start hotfix " + hotFixVersion +
                " and update gradle.properties to version " + hotFixVersion + " " + getScmMessageSuffix(command))

        flow.git().close()
    }
}
