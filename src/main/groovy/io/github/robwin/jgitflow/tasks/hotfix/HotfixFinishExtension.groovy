package io.github.robwin.jgitflow.tasks.hotfix

import com.atlassian.jgitflow.core.extension.ExtensionCommand
import com.atlassian.jgitflow.core.extension.impl.EmptyHotfixFinishExtension
import io.github.robwin.jgitflow.tasks.commands.ResetDevelopCommand
import io.github.robwin.jgitflow.tasks.commands.UpdateSnapshotToReleaseCommand
import io.github.robwin.jgitflow.tasks.commands.PrepareDevelopForMergeCommand
import org.gradle.api.Project

class HotfixFinishExtension extends EmptyHotfixFinishExtension {

    private final Project project
    private final String developVersion

    HotfixFinishExtension(Project project, String developVersion) {
        this.project = project
        this.developVersion = developVersion
    }

    @Override
    Iterable<ExtensionCommand> afterTopicCheckout() {
        [new UpdateSnapshotToReleaseCommand(project)]
    }

    @Override
    Iterable<ExtensionCommand> beforeDevelopMerge() {
        [new PrepareDevelopForMergeCommand(project)]
    }

    @Override
    Iterable<ExtensionCommand> afterDevelopMerge() {
        [new ResetDevelopCommand(project, developVersion)]
    }
}