package software.setixx.chimu.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import software.setixx.chimu.data.local.TokenStorage
import software.setixx.chimu.data.local.createTokenStorage
import software.setixx.chimu.data.remote.AuthApi
import software.setixx.chimu.data.remote.KtorClient
import software.setixx.chimu.data.repository.AuthRepositoryImpl
import software.setixx.chimu.domain.repository.AuthRepository
import software.setixx.chimu.domain.usecase.*
import software.setixx.chimu.presentation.auth.login.LoginViewModel
import software.setixx.chimu.presentation.auth.register.RegisterViewModel
import software.setixx.chimu.presentation.home.HomeViewModel
import software.setixx.chimu.presentation.splash.SplashViewModel

val appModule = module {
    single { KtorClient(get()) }
    single { AuthApi(get<KtorClient>().httpClient) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { GetSavedEmailUseCase(get()) }
    factory { CheckAuthStatusUseCase(get()) }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
}