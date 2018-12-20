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
import com.atlassian.jgitflow.core.ReleaseMergeResult
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import io.github.robwin.jgitflow.tasks.AbstractFinalizeTask
import org.eclipse.jgit.api.Git
import org.gradle.api.tasks.TaskAction

class HotfixFinishTask extends AbstractFinalizeTask {

    @TaskAction
    void finish() {
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        Git git = flow.git()

        checkRequirements(flow.getHotfixBranchPrefix(), git, project)
        String developVersion = checkoutDevelop(flow.getDevelopBranchName(), git, project)
        String hotFixVersion = GradlePropertiesHelper.getVersionWithoutSnapshot(project)
        ReleaseMergeResult mergeResult = flow.hotfixFinish(hotFixVersion)
                .setExtension(new HotfixFinishExtension(project, developVersion))
                .setFetch(true)
                .setPush(isPushEnabled(project))
                .call()

        handleReleaseMergeResult(mergeResult, flow, logger)
        git.close()
    }
}
