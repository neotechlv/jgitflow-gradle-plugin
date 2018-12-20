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
package io.github.robwin.jgitflow.tasks


import com.google.common.base.Strings
import org.gradle.api.DefaultTask
import org.gradle.api.Project

class AbstractCommandTask extends DefaultTask {

    private static final String PUSH_RELEASE_PROP_NAME = "push"

    protected static boolean isPushEnabled(Project project) {
        if (!project.hasProperty(PUSH_RELEASE_PROP_NAME)) {
            return false
        }
        Object value = project.property(PUSH_RELEASE_PROP_NAME)
        return Strings.isNullOrEmpty(value) || Boolean.TRUE.equals(Boolean.valueOf(value))
    }

}