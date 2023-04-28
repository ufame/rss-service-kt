package com.service.schemas

import org.jetbrains.exposed.sql.Database

object ServiceLocator {
    lateinit var database: Database
}
fun configureDatabases() {
    ServiceLocator.database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
}