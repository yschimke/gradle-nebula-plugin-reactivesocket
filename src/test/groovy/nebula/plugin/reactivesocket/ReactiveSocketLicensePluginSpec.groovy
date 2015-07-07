package nebula.plugin.reactivesocket

import nebula.test.ProjectSpec
import org.gradle.api.plugins.JavaPlugin

class ReactiveSocketLicensePluginSpec extends ProjectSpec {
    def 'lazily save file'() {
        when:
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(ReactiveSocketLicensePlugin)

        then:
        def HEADER = new File(projectDir, 'build/license/HEADER')
        !HEADER.exists()

        when:
        def headerTask = project.tasks.getByName('writeLicenseHeader')
        headerTask.getActions().each { it.execute(headerTask) }

        then:
        HEADER.exists()
    }
}
