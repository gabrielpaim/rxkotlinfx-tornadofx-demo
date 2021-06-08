package app

import domain.persistence.Persistence
import domain.persistence.rxjava_jdbc.RxjavaJdbcPersistence
import org.koin.dsl.module

val mainModule = module {

    single<Persistence> { RxjavaJdbcPersistence.create("jdbc:sqlite:") }

    // Add more dependencies

}