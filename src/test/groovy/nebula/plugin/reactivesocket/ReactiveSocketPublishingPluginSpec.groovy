package nebula.plugin.reactivesocket

import spock.lang.Specification

/**
 * Created by jryan on 7/10/14.
 */
class ReactiveSocketPublishingPluginSpec extends Specification {
    def 'testCalculateUrlFromOrigin'(String origin, String uri) {
        expect:
        ReactiveSocketPublishingPlugin.calculateUrlFromOrigin(origin) == uri

        where:
        origin                                           | uri
        'git@github.com:reactivex/rxjava-core.git'       | 'https://github.com/reactivex/rxjava-core'
        'ssh://git@github.com:reactivex/rxjava-core.git' | 'https://github.com/reactivex/rxjava-core'
        'https://github.com:reactivex/rxjava-core.git'   | 'https://github.com/reactivex/rxjava-core'

    }
    def 'testCalculateRepoFromOrigin'(String origin, String repo) {
        expect:
        ReactiveSocketPublishingPlugin.calculateRepoFromOrigin(origin) == repo

        where:
        origin                                           | repo
        'git@github.com:reactivex/rxjava-core.git'       | 'rxjava-core'
        'ssh://git@github.com:reactivex/rxjava-core.git' | 'rxjava-core'
        'https://github.com:reactivex/rxjava-core.git'   | 'rxjava-core'

    }
}
