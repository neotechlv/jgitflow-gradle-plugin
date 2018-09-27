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
import com.atlassian.jgitflow.core.ReleaseMergeResult
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.tasks.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile

class HotfixFinishTask extends AbstractCommandTask {

    @TaskAction
    void finish() {

        CredentialsProviderHelper.requireCredentials(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        Git git = flow.git()

        String currentBranch = git.getRepository().getBranch()
        if (!currentBranch.startsWith(flow.getHotfixBranchPrefix())) {
            throw new GradleException("Are you on hotfix branch?")
        }

        git.fetch().call()

        String hotfixVersion = GradlePropertiesHelper.getProjectVersion(project)

        flow.git().checkout().setName(flow.getDevelopBranchName()).call()
        String snapshotVersion = GradlePropertiesHelper.readPropertyFromGradleFile(project, "version")

        GradlePropertiesHelper.updateGradlePropertiesFile(project, hotfixVersion)
        commitGradlePropertiesFile(flow.git(),
                "Updated gradle.properties to hotfix version '${hotfixVersion}' to avoid merge conflicts")

        //checkout hotfix branch
        flow.git().checkout().setName(flow.getHotfixBranchPrefix() + hotfixVersion).call()

        def command = flow.hotfixFinish(hotfixVersion)

        setCommandPrefixAndSuffix(command)

        ReleaseMergeResult mergeResult = command.call()
        if (!mergeResult.wasSuccessful()) {
            if (mergeResult.masterHasProblems()) {
                logger.error("Error merging into " + flow.getMasterBranchName() + ":")
                logger.error(mergeResult.getMasterResult().toString())
            }

            if (mergeResult.developHasProblems()) {
                logger.error("Error merging into " + flow.getDevelopBranchName() + ":")
                logger.error(mergeResult.getDevelopResult().toString())
            }
            throw new GradleException("Error while merging hotfix!")
        } else {
            GradlePropertiesHelper.updateGradlePropertiesFile(project, snapshotVersion)
            commitGradlePropertiesFile(flow.git(),
                    "Updated gradle.properties version '${snapshotVersion}' back to pre-merge state")
        }
        flow.git().close()
    }
}
