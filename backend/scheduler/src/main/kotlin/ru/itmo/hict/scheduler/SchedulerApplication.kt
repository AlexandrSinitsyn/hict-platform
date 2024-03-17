package ru.itmo.hict.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableKafka
@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class],
)
class SchedulerApplication

fun main(args: Array<String>) {
	runApplication<SchedulerApplication>(*args)
}
