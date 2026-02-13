package software.setixx.chimu.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import software.setixx.chimu.data.local.AndroidTokenStorage
import software.setixx.chimu.data.local.TokenStorage

actual fun platformModule(): Module = module {
    single<TokenStorage> {
        val context = get<Context>()
        AndroidTokenStorage(context)
    }
}