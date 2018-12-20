package io.github.robwin.jgitflow.tasks

import com.atlassian.jgitflow.core.JGitFlow
import com.atlassian.jgitflow.core.ReleaseMergeResult
import io.github.robwin.jgitflow.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.dependencies.DependencyVerifier
import io.github.robwin.jgitflow.helper.GitHelper
import io.github.robwin.jgitflow.helper.GradlePropertiesHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class AbstractFinalizeTask extends AbstractCommandTask {

    protected void checkRequirements(String prefix, Git git, Project project) {
        GitHelper.requireCurrentBranchToStartWith(prefix, git)
        DependencyVerifier.checkSnapshots(project)
        CredentialsProviderHelper.getCredentials(project)
    }

    protected String checkoutDevelop(String developBranchName, Git git, Project project) {
        git.checkout().setName(developBranchName).call()
        return GradlePropertiesHelper.readVersionFromGradleFile(project)
    }

    protected void handleReleaseMergeResult(ReleaseMergeResult mergeResult, JGitFlow flow,
                                            Logger logger) {
        if (!mergeResult.wasSuccessful()) {
            if (mergeResult.masterHasProblems()) {
                logger.error("Error merging into ${flow.getMasterBranchName()}:")
                logger.error(mergeResult.getMasterResult().toString())
            }
            if (mergeResult.developHasProblems()) {
                logger.error("Error merging into ${flow.getDevelopBranchName()}:")
                logger.error(mergeResult.getDevelopResult().toString())
            }
            throw new GradleException("Error while merging!")
        }
    }

}