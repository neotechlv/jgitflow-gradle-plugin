package io.github.robwin.jgitflow.tasks.helper

import org.apache.tools.ant.BuildException
import org.gradle.api.GradleException
import org.gradle.api.Project

class GradlePropertiesHelper {

    static void updateGradlePropertiesFile(Project project, String version) {
        File propertiesFile = project.file(Project.GRADLE_PROPERTIES)
        if (!propertiesFile.file) {
            propertiesFile.append("version=${version}")
        } else {
            try {
                project.ant.replaceregexp(match: "^version=.*", replace: "version=${version}",
                        flags: 'g', byline: true) {
                    fileset(dir: project.projectDir, includes: Project.GRADLE_PROPERTIES)
                }
            } catch (BuildException e) {
                throw new GradleException("Failed to update version in ${Project.GRADLE_PROPERTIES}.  " +
                        "Check the property exists and is formatted correctly.")
            }
        }
    }

    static String readPropertyFromGradleFile(Project project, String prop) {
        File propertiesFile = project.file(Project.GRADLE_PROPERTIES)
        if (propertiesFile.file) {
            Properties properties = new Properties()
            properties.load(propertiesFile.newDataInputStream())
            return properties.getProperty(prop)
        } else {
            return null
        }
    }

    static String getProjectVersion(Project project) {
        if (!project.hasProperty('version')) {
            throw new GradleException('version or releaseVersion property have to be present')
        }
        String version = project.property('version') as String
        if (version == "unspecified") {
            throw new GradleException("Cannot get version property from ${Project.GRADLE_PROPERTIES}")
        }
        ArtifactHelper.removeSnapshot(version)
    }
}