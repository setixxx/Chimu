package software.setixx.chimu.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "seaweedfs")
class SeaweedFsProperties {
    var filerUrl: String = "http://seaweedfs-filer:8888"
}