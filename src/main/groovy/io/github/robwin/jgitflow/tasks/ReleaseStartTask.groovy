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
import io.github.robwin.jgitflow.tasks.helper.GitHelper
import io.github.robwin.jgitflow.tasks.helper.GradlePropertiesHelper
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile

class ReleaseStartTask extends AbstractCommandTask {

    static final String RELEASE_VERSION_PROP_NAME = "releaseVersion"
    static final String ALLOW_SNAPSHOTS_PROP_NAME = "allowSnapshotDependencies"
    static final String ALLOW_PROJECT_GROUP_SNAPSHOTS_PROP_NAME = "allowProjectGroupSnapshotDependencies"

    @TaskAction
    void start() {

        String releaseVersion = project.properties[RELEASE_VERSION_PROP_NAME] ?: GradlePropertiesHelper.getProjectVersion(project)
        validateReleaseVersion(releaseVersion)

        CredentialsProviderHelper.requireCredentials(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        //Make sure that the develop branch is used
        flow.git().checkout().setName(flow.getDevelopBranchName()).call()

        if (!getBooleanOrFalse(ALLOW_SNAPSHOTS_PROP_NAME)) {
            //Check that no library dependency is a snapshot
            checkThatNoDependencyIsASnapshot(getBooleanOrFalse(ALLOW_PROJECT_GROUP_SNAPSHOTS_PROP_NAME))
        }

        //Start a release
        def command = flow.releaseStart(releaseVersion)

        setCommandPrefixAndSuffix(command)

        if (project.hasProperty('baseCommit')) {
            String baseCommit = project.property('baseCommit')
            command.setStartCommit(baseCommit)
        }

        command.call()

        //Local working copy is now on release branch

        //Update the release version
        GradlePropertiesHelper.updateGradlePropertiesFile(project, releaseVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), getScmMessagePrefix(command) +
                "Updated gradle.properties for v" + releaseVersion + " release" + getScmMessageSuffix(command))

        flow.git().close()
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

    private void checkThatNoDependencyIsASnapshot(boolean allowProjectGroupSnapshotDependencies) {
        def snapshotDependencies = [] as Set
        project.allprojects.each { project ->
            project.configurations.each { configuration ->
                configuration.allDependencies.each { Dependency dependency ->
                    if ((!allowProjectGroupSnapshotDependencies || !dependency.group.startsWith(project.group))
                            && ArtifactHelper.isSnapshot(dependency.version)) {
                        snapshotDependencies.add("${dependency.group}:${dependency.name}:${dependency.version}")
                    }
                }
            }
        }
        if (!snapshotDependencies.isEmpty()) {
            throw new GradleException("Cannot start a release due to snapshot dependencies: ${snapshotDependencies}")
        }
    }

    private boolean getBooleanOrFalse(String propName) {
        return project.hasProperty(propName) ? Boolean.valueOf(project.property(propName)) : false
    }
}
