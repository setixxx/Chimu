package software.setixx.chimu.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import software.setixx.chimu.data.remote.*
import software.setixx.chimu.data.repository.*
import software.setixx.chimu.domain.repository.*
import software.setixx.chimu.domain.usecase.*
import software.setixx.chimu.presentation.auth.login.LoginViewModel
import software.setixx.chimu.presentation.auth.register.RegisterViewModel
import software.setixx.chimu.presentation.main.MainViewModel
import software.setixx.chimu.presentation.profile.ProfileViewModel
import software.setixx.chimu.presentation.splash.SplashViewModel
import software.setixx.chimu.presentation.team.CreateTeamViewModel

val appModule = module {
    single { KtorClient(get()) }

    single { AuthApi(get<KtorClient>().httpClient) }
    single { GameJamApi(get<KtorClient>().httpClient) }
    single { TeamApi(get<KtorClient>().httpClient) }
    single { ProjectApi(get<KtorClient>().httpClient) }
    single { ProfileApi(get<KtorClient>().httpClient) }
    single { SpecializationApi(get<KtorClient>().httpClient) }
    single { SkillApi(get<KtorClient>().httpClient) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<GameJamRepository> { GameJamRepositoryImpl(get(), get()) }
    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }
    single<ProjectRepository> { ProjectRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<SpecializationRepository> { SpecializationRepositoryImpl(get(), get()) }
    single<SkillRepository> { SkillRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { GetSavedEmailUseCase(get()) }
    factory { CheckAuthStatusUseCase(get()) }
    factory { GetActiveJamsUseCase(get()) }
    factory { GetAllJamsUseCase(get()) }
    factory { GetUserTeamsUseCase(get()) }
    factory { GetUserProjectsUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { GetAllSpecializationsUseCase(get()) }
    factory { GetAllSkillsUseCase(get()) }
    factory { CreateTeamUseCase(get()) }

    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { CreateTeamViewModel(get(), get()) }
}