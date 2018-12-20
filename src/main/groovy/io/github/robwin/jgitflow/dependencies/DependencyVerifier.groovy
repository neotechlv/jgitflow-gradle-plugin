package io.github.robwin.jgitflow.dependencies

import com.google.common.base.Strings;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import io.github.robwin.jgitflow.helper.ArtifactHelper;

class DependencyVerifier {

    static final String ALLOW_SNAPSHOTS_PROP_NAME = "allowSnapshots"
    static final String ALLOW_PROJECT_SNAPSHOTS_PROP_NAME = "allowProjectSnapshots"

    static void checkSnapshots(Project project) {
        if (isBlankOrTrue(ALLOW_SNAPSHOTS_PROP_NAME, project)) {
            return
        }
        boolean allowProjectGroupSnapshots = isBlankOrTrue(ALLOW_PROJECT_SNAPSHOTS_PROP_NAME, project)
        def snapshotDependencies = [] as Set
        project.allprojects.each { subProject ->
            subProject.configurations.each { configuration ->
                configuration.allDependencies.each { Dependency dependency ->
                    String group = dependency.group
                    String version = dependency.version
                    if (group == null || version == null) {
                        return
                    }
                    if ((!allowProjectGroupSnapshots || !group.startsWith(subProject.group))
                            && ArtifactHelper.isSnapshot(version)) {
                        snapshotDependencies.add("${group}:${dependency.name}:${version}")
                    }
                }
            }
        }
        if (!snapshotDependencies.isEmpty()) {
            throw new GradleException("Cannot start a release due to snapshot dependencies: ${snapshotDependencies}")
        }
    }

    private static boolean isBlankOrTrue(String propName, Project project) {
        if (!project.hasProperty(propName)) {
            return false
        }
        Object value = project.property(propName)
        return Strings.isNullOrEmpty(value) || Boolean.TRUE.equals(Boolean.valueOf(value))
    }

}