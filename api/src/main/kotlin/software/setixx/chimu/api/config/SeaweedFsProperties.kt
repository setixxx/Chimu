package software.setixx.chimu.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Свойства конфигурации SeaweedFS.
 * Содержит URL-адрес филера для взаимодействия с распределенным хранилищем файлов.
 */
@Component
@ConfigurationProperties(prefix = "seaweedfs")
class SeaweedFsProperties {
    var filerUrl: String = "http://seaweedfs-filer:8888"
}