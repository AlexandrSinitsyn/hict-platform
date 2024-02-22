package ru.itmo.hict.authorization

import liquibase.integration.spring.SpringLiquibase
import liquibase.integration.spring.SpringResourceAccessor
import liquibase.resource.Resource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import javax.sql.DataSource

@TestConfiguration
class LiquibaseConfig(
    private val dataSource: DataSource,
) {
    private class MigrationsAccessor(resourceLoader: ResourceLoader) : SpringResourceAccessor(resourceLoader) {
        override fun get(path: String): Resource? {
            return super.get("file:../../migrations/db/changelog/$path")
        }
    }

    private class CustomSpringLiquibase : SpringLiquibase() {
        override fun createResourceOpener(): SpringResourceAccessor {
            return MigrationsAccessor(getResourceLoader())
        }
    }

    @Bean
    fun liquibase(): SpringLiquibase {
        val liquibase = CustomSpringLiquibase()
        liquibase.dataSource = dataSource
        liquibase.changeLog = "db.changelog-master.yml"
        return liquibase
    }
}
