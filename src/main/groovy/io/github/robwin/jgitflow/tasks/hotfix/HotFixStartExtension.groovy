package io.github.robwin.jgitflow.tasks.hotfix

import com.atlassian.jgitflow.core.GitFlowConfiguration
import com.atlassian.jgitflow.core.JGitFlow
import com.atlassian.jgitflow.core.command.JGitFlowCommand
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException
import com.atlassian.jgitflow.core.extension.ExtensionCommand
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy
import com.atlassian.jgitflow.core.extension.impl.EmptyHotfixStartExtension
import io.github.robwin.jgitflow.helper.ArtifactHelper
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.Project

import static io.github.robwin.jgitflow.helper.GitHelper.commitGradlePropertiesFile

class HotFixStartExtension extends EmptyHotfixStartExtension {

    private final Project project
    private final JGitFlow flow
    private final String hotFixVersion

    HotFixStartExtension(Project project, JGitFlow flow, String hotFixVersion) {
        this.project = project
        this.flow = flow
        this.hotFixVersion = hotFixVersion
    }

    Iterable<ExtensionCommand> afterCreateBranch() {
        return [new ExtensionCommand() {

            void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand)
                    throws JGitFlowExtensionException {
                String snapshotVersion = ArtifactHelper.toSnapshot(hotFixVersion)
                GradlePropertiesHelper.updateProjectVersion(project, snapshotVersion)
                commitGradlePropertiesFile(git, "Start hotfix ${hotFixVersion} and update " +
                        "gradle.properties to version ${snapshotVersion}")
            }

            ExtensionFailStrategy failStrategy() {
                ExtensionFailStrategy.ERROR
            }
        }]
    }
}