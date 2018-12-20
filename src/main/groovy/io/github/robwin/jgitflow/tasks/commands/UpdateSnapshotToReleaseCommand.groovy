package io.github.robwin.jgitflow.tasks.commands

import com.atlassian.jgitflow.core.GitFlowConfiguration
import com.atlassian.jgitflow.core.command.JGitFlowCommand
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException
import com.atlassian.jgitflow.core.extension.ExtensionCommand
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy
import io.github.robwin.jgitflow.dependencies.DependencyVerifier
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.Project

import static io.github.robwin.jgitflow.helper.GitHelper.commitGradlePropertiesFile

class UpdateSnapshotToReleaseCommand implements ExtensionCommand {

    private final Project project

    UpdateSnapshotToReleaseCommand(Project project) {
        this.project = project
    }

    @Override
    void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand)
            throws JGitFlowExtensionException {
        DependencyVerifier.checkSnapshots(project)
        String projectVersion = GradlePropertiesHelper.getVersionWithoutSnapshot(project)
        GradlePropertiesHelper.updateProjectVersion(project, projectVersion)
        commitGradlePropertiesFile(git, "Update version to ${projectVersion}")
    }

    @Override
    ExtensionFailStrategy failStrategy() {
        ExtensionFailStrategy.ERROR
    }
}