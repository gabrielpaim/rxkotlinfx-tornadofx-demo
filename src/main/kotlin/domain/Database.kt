package domain

import domain.persistence.Persistence
import org.koin.core.context.GlobalContext

// TODO delete
val db: Persistence by lazy { GlobalContext.get().get() }