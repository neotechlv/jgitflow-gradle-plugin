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

class ResetDevelopCommand implements ExtensionCommand {

    private final Project project
    private final String developVersion

    ResetDevelopCommand(Project project, String developVersion) {
        this.project = project
        this.developVersion = developVersion
    }

    @Override
    void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand)
            throws JGitFlowExtensionException {
        if (!git.status().call().getConflicting().isEmpty()) {
            throw new GradleException("Conflict after merging master into develop. " +
                    "Solve it and finish task manually.")
        }
        GradlePropertiesHelper.updateProjectVersion(project, developVersion)
        commitGradlePropertiesFile(git, "Update develop version back to pre-merge state - ${developVersion}")
    }

    @Override
    ExtensionFailStrategy failStrategy() {
        ExtensionFailStrategy.ERROR
    }
}