package software.setixx.chimu.di

import org.koin.core.module.Module
import org.koin.dsl.module
import software.setixx.chimu.data.local.JsTokenStorage
import software.setixx.chimu.data.local.TokenStorage

actual fun platformModule(): Module = module {
    single<TokenStorage> { JsTokenStorage() }
}