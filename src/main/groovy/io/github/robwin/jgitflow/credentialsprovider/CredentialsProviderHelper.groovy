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
package io.github.robwin.jgitflow.credentialsprovider

import com.atlassian.jgitflow.core.JGitFlowReporter
import com.google.common.base.Strings
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.Project

class CredentialsProviderHelper {

    static final String GIT_USERNAME_PROP_NAME = "username"
    static final String GIT_PASSWORD_PROP_NAME = "password"

    static void getCredentials(Project project) {
        String username = getCredential(GIT_USERNAME_PROP_NAME, project)
        String password = getCredential(GIT_PASSWORD_PROP_NAME, project)
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            JGitFlowReporter.get().debugText(getClass().getSimpleName(), "using provided username and password")
            CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider(username, password))
        }
    }

    private static getCredential(String propName, Project project) {
        String property = property(project, propName)
        return Strings.isNullOrEmpty(property) ? readInput(project, propName) : property;
    }

    private static String property(Project project, String propName) {
        return project.hasProperty(propName) ? project.property(propName) : null
    }

    private static String readInput(Project project, String propName) {
        if(System.console() != null) {
            return new String(System.console().readPassword("\nPlease enter git ${propName}: "))
        } else {
            project.ant.input(message: "\nPlease enter git ${propName}:", addproperty: propName)
            return project.ant.properties[propName]
        }
    }

}