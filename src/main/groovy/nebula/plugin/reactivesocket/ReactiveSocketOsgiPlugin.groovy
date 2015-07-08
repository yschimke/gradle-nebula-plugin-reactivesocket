package nebula.plugin.reactivesocket

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.osgi.OsgiManifest
import org.gradle.api.plugins.osgi.OsgiPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * Apply OSGI specific fields
 */
class ReactiveSocketOsgiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(OsgiPlugin)

        // TODO Use scm info to determine DocURL

        project.tasks.matching { it.name == 'jar' }.all { Jar jarTask ->
            project.plugins.withType(JavaPlugin) {
                // OsgiPlugin will only set the manifest to a OsgiManifest if the JavaPlugin is applied
                jarTask.manifest {
                    // Add some static typing to these calls
                    OsgiManifest manifest = (OsgiManifest) delegate

                    manifest.name = project.name
                    manifest.instruction 'Bundle-Vendor', 'ReactiveSocket'
                    manifest.instruction 'Bundle-DocURL', 'https://github.com/ReactiveSocket'

                    // Legacy from what we had test in the compile classpath
                    manifest.instruction 'Import-Package', '!org.junit,!junit.framework,!org.mockito.*,*'
                }
            }
        }
    }
}
