package setixx.software.Chimu

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
class ChimuApplication

fun main(args: Array<String>) {
	runApplication<ChimuApplication>(*args)
}
