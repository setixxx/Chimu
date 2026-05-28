package software.setixx.chimu.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Главный класс приложения Chimu API.
 * Запускает Spring Boot приложение и включает поддержку планировщика задач.
 */
@SpringBootApplication
@EnableScheduling
class ChimuApplication

fun main(args: Array<String>) {
	runApplication<software.setixx.chimu.api.ChimuApplication>(*args)
}
