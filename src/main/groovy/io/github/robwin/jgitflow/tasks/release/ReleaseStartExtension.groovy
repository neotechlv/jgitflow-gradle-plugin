package io.github.robwin.jgitflow.tasks.release

import com.atlassian.jgitflow.core.GitFlowConfiguration
import com.atlassian.jgitflow.core.JGitFlow
import com.atlassian.jgitflow.core.command.JGitFlowCommand
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException
import com.atlassian.jgitflow.core.extension.ExtensionCommand
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy
import com.atlassian.jgitflow.core.extension.impl.EmptyReleaseStartExtension
import io.github.robwin.jgitflow.helper.ArtifactHelper
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.Project

import static io.github.robwin.jgitflow.helper.GitHelper.commitGradlePropertiesFile

class ReleaseStartExtension extends EmptyReleaseStartExtension {

    private final Project project
    private final JGitFlow flow
    private final String releaseVersion

    ReleaseStartExtension(Project project, JGitFlow flow, String releaseVersion) {
        this.project = project
        this.flow = flow
        this.releaseVersion = releaseVersion
    }

    Iterable<ExtensionCommand> afterCreateBranch() {
        return [new ExtensionCommand() {

            void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand)
                    throws JGitFlowExtensionException {
                String projectVersion = GradlePropertiesHelper.getVersion(project)
                String newVersion = ArtifactHelper.toSnapshot(ArtifactHelper.incrementMinor(projectVersion))

                git.checkout().setName(flow.getDevelopBranchName()).call()
                GradlePropertiesHelper.updateProjectVersion(project, newVersion)

                commitGradlePropertiesFile(git, "Update project version to " + newVersion)

                git.checkout().setName(flow.getReleaseBranchPrefix() + releaseVersion).call()
            }

            ExtensionFailStrategy failStrategy() {
                ExtensionFailStrategy.ERROR
            }
        }]
    }

}