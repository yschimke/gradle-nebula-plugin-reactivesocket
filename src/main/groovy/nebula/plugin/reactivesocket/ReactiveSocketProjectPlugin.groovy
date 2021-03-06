/*
 * Copyright 2014-2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.reactivesocket

import nebula.core.GradleHelper
import nebula.core.ProjectType
import nebula.plugin.contacts.ContactsPlugin
import nebula.plugin.dependencylock.DependencyLockPlugin
import nebula.plugin.info.InfoPlugin
import nebula.plugin.override.NebulaOverridePlugin
import nebula.plugin.publishing.maven.MavenDeveloperPlugin
import nebula.plugin.publishing.maven.MavenManifestPlugin
import nebula.plugin.publishing.maven.MavenResolvedDependenciesPlugin
import nebula.plugin.publishing.maven.MavenScmPlugin
import nebula.plugin.publishing.publications.JavadocJarPlugin
import nebula.plugin.publishing.maven.MavenPublishPlugin
import nebula.plugin.publishing.publications.SourceJarPlugin
import nebula.plugin.responsible.NebulaFacetPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

class ReactiveSocketProjectPlugin implements Plugin<Project> {

    Project project

    @Override
    void apply(Project project) {
        this.project = project

        // When running as a multi-module build, we want to treat the rootProject differently, we'll call this a parent project
        ProjectType projectType = new ProjectType(project)

        if (projectType.isLeafProject || projectType.isRootProject) {
            // Repositories
            project.repositories {
                jcenter()
                maven { url 'https://dl.bintray.com/reactivesocket/ReactiveSocket' }
            }

            // IDE Support
            project.plugins.apply EclipsePlugin
            project.plugins.apply IdeaPlugin

            // Default Group
            def gradleHelper = new GradleHelper(project)
            gradleHelper.addDefaultGroup('io.reactivesocket')

            // Default description, a user would just specify it after applying our plugin
            project.description = project.name
        }

        project.plugins.apply ReactiveSocketReleasePlugin

        // Dummy tasks as the root level, instead of relying on pass-through
        if (projectType.isRootProject && !projectType.isLeafProject) {
            // Artifactory plugin does this for artifactoryPublish itself
            ['build'].each { concreteTaskName ->
                def concreteTask = project.task(concreteTaskName)
                project.subprojects {
                    tasks.matching { it.name == concreteTaskName }.all { subprojectBuildTask ->
                        concreteTask.dependsOn subprojectBuildTask
                    }
                }
            }
        }

        if (projectType.isLeafProject || projectType.isRootProject) {
            project.plugins.apply(ReactiveSocketPublishingPlugin)
        }

        // Info, needed on all projects, so that the publishing can look at scm values
        project.plugins.apply(InfoPlugin)

        if (projectType.isLeafProject) {
            project.plugins.apply(JavaBasePlugin)

            // Publishing
            project.plugins.apply(MavenResolvedDependenciesPlugin)
            project.plugins.apply(MavenDeveloperPlugin)
            project.plugins.apply(MavenManifestPlugin)
            project.plugins.apply(MavenScmPlugin)
            project.plugins.apply(JavadocJarPlugin)
            project.plugins.apply(SourceJarPlugin)

            // Contacts
            project.plugins.apply(ContactsPlugin)
            project.contacts {
                'netflixoss@netflix.com' {
                    github 'netflixgithub'
                    moniker 'Netflix Open Source Development'
                }
            }

            // Dependency Locking
            project.plugins.apply(DependencyLockPlugin)

            project.plugins.apply(NebulaOverridePlugin)

            // ReactiveX specific plugins
            project.plugins.apply(ReactiveSocketPerformancePlugin)
            project.plugins.apply(ReactiveSocketOsgiPlugin)
            project.plugins.apply(ReactiveSocketLicensePlugin)

            // Set Default java versions
            project.plugins.withType(JavaPlugin) {
                // Facets
                def facetPlugin = (NebulaFacetPlugin) project.plugins.apply(NebulaFacetPlugin)
                facetPlugin.extension.create('examples') {
                    parentSourceSet = 'main'
                }

                JavaPluginConvention convention = project.convention.getPlugin(JavaPluginConvention)
                convention.sourceCompatibility = JavaVersion.VERSION_1_8
                //convention.targetCompatibility = JavaVersion.VERSION_1_8
            }

            // TODO Publish javadoc back to Github for hosting
            project.tasks.withType(Javadoc) {
                failOnError = false

                options {
                    windowTitle = "ReactiveSocket Javadoc ${project.version}"

                    if (JavaVersion.current().isJava8Compatible()) {
                        options.addStringOption('Xdoclint:none', '-quiet')
                    }
                }
                options.addStringOption('top').value = '<h2 class="title" style="padding-top:40px">ReactiveSocket</h2>'
            }

            project.tasks.withType(Test) { Test testTask ->
                testTask.testLogging.exceptionFormat = 'full'
                testTask.testLogging.events "started"
                testTask.testLogging.displayGranularity = 2
            }
        }
    }
}
