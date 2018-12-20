package io.github.robwin.jgitflow.tasks.commands

import com.atlassian.jgitflow.core.GitFlowConfiguration
import com.atlassian.jgitflow.core.command.JGitFlowCommand
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException
import com.atlassian.jgitflow.core.extension.ExtensionCommand
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Project

import static io.github.robwin.jgitflow.helper.GitHelper.commitGradlePropertiesFile

class PrepareDevelopForMergeCommand implements ExtensionCommand {

    private final Project project

    PrepareDevelopForMergeCommand(Project project) {
        this.project = project
    }

    @Override
    void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand)
            throws JGitFlowExtensionException {
        if (!git.status().call().getConflicting().isEmpty()) {
            throw new GradleException("Conflict after merging release into master. " +
                    "Solve it and finish task manually.")
        }
        String releaseVersion = gitFlowCommand.getBranchName()
        GradlePropertiesHelper.updateProjectVersion(project, releaseVersion)
        commitGradlePropertiesFile(git, "Update develop version to ${releaseVersion} to " +
                "avoid conflicts when merging into develop")
    }

    @Override
    ExtensionFailStrategy failStrategy() {
        ExtensionFailStrategy.ERROR
    }
}