package software.setixx.chimu.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import software.setixx.chimu.data.remote.*
import software.setixx.chimu.data.repository.*
import software.setixx.chimu.domain.repository.*
import software.setixx.chimu.domain.usecase.*
import software.setixx.chimu.presentation.auth.login.LoginViewModel
import software.setixx.chimu.presentation.auth.register.RegisterViewModel
import software.setixx.chimu.presentation.jam.create.CreateJamViewModel
import software.setixx.chimu.presentation.jam.details.JamDetailsViewModel
import software.setixx.chimu.presentation.jam.edit.EditJamViewModel
import software.setixx.chimu.presentation.main.MainViewModel
import software.setixx.chimu.presentation.profile.ProfileViewModel
import software.setixx.chimu.presentation.splash.SplashViewModel
import software.setixx.chimu.presentation.team.TeamDetailsViewModel
import software.setixx.chimu.presentation.team.create.CreateTeamViewModel

val appModule = module {
    single { KtorClient(get()) }

    single { AuthApi(get<KtorClient>().httpClient) }
    single { GameJamApi(get<KtorClient>().httpClient) }
    single { JudgeApi(get<KtorClient>().httpClient) }
    single { LeaderboardApi(get<KtorClient>().httpClient) }
    single { UserApi(get<KtorClient>().httpClient) }
    single { ProjectApi(get<KtorClient>().httpClient) }
    single { ProjectFileApi(get<KtorClient>().httpClient) }
    single { RatingApi(get<KtorClient>().httpClient) }
    single { RatingCriteriaApi(get<KtorClient>().httpClient) }
    single { SkillApi(get<KtorClient>().httpClient) }
    single { SpecializationApi(get<KtorClient>().httpClient) }
    single { TeamApi(get<KtorClient>().httpClient) }
    single { TeamRegistrationsApi(get<KtorClient>().httpClient) }
    single { RoleUpgradeApi(get<KtorClient>().httpClient) }
    single { JamTransferApi(get<KtorClient>().httpClient) }


    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<GameJamRepository> { GameJamRepositoryImpl(get(), get()) }
    single<JudgeRepository> { JudgeRepositoryImpl(get(), get()) }
    single<LeaderboardRepository> { LeaderboardRepositoryImpl(get(), get()) }
    single<ProjectRepository> { ProjectRepositoryImpl(get(), get()) }
    single<ProjectFileRepository> { ProjectFileRepositoryImpl(get(), get()) }
    single<RatingCriteriaRepository> { RatingCriteriaRepositoryImpl(get(), get()) }
    single<RatingRepository> { RatingRepositoryImpl(get(), get()) }
    single<SkillRepository> { SkillRepositoryImpl(get(), get()) }
    single<SpecializationRepository> { SpecializationRepositoryImpl(get(), get()) }
    single<TeamRegistrationRepository> { TeamRegistrationRepositoryImpl(get(), get()) }
    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<RoleUpgradeRepository> { RoleUpgradeRepositoryImpl(get(), get()) }
    single<JamTransferRepository> { JamTransferRepositoryImpl(get(), get()) }

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
    factory { GetTeamDetailsUseCase(get()) }
    factory { UpdateTeamUseCase(get()) }
    factory { JoinTeamUseCase(get()) }
    factory { LeaveTeamUseCase(get()) }
    factory { DeleteTeamUseCase(get()) }
    factory { KickMemberUseCase(get()) }
    factory { UpdateMemberSpecializationUseCase(get()) }
    factory { RegenerateInviteTokenUseCase(get()) }
    factory { CreateJamUseCase(get()) }
    factory { GetJamDetailsUseCase(get()) }
    factory { UpdateJamUseCase(get()) }
    factory { DeleteJamUseCase(get()) }
    factory { GetJamRegistrationsUseCase(get()) }
    factory { RegisterTeamUseCase(get()) }
    factory { WithdrawTeamUseCase(get()) }
    factory { UpdateRegistrationStatusUseCase(get()) }
    factory { GetJamJudgesUseCase(get()) }
    factory { AssignJudgeUseCase(get()) }
    factory { UnassignJudgeUseCase(get()) }
    factory { GetJamCriteriaUseCase(get()) }
    factory { CreateJamCriteriaUseCase(get()) }
    factory { UpdateJamCriteriaUseCase(get()) }
    factory { DeleteJamCriteriaUseCase(get()) }
    factory { CreateTransferUseCase(get()) }
    factory { CancelTransferUseCase(get()) }
    factory { ReviewTransferUseCase(get()) }
    factory { GetTransferRequestsUseCase(get()) }
    factory { GetUserRoleUpgradesUseCase(get()) }
    factory { CreateRoleUpgradeUseCase(get()) }
    factory { ReviewRoleUpgradeUseCase(get()) }
    factory { GetAllRoleUpgradesUseCase(get()) }
    factory { CancelRoleUpgradeUseCase(get()) }

    // Project UseCases
    factory { GetTeamProjectsUseCase(get()) }
    factory { CreateProjectUseCase(get()) }
    factory { SubmitProjectUseCase(get()) }
    factory { ReturnDraftUseCase(get()) }
    factory { DisqualifyProjectUseCase(get()) }
    factory { GetJamProjectsUseCase(get()) }
    factory { GetProjectUseCase(get()) }
    factory { GetProjectFilesUseCase(get()) }
    factory { UploadProjectFileUseCase(get()) }
    factory { DownloadProjectFileUseCase(get()) }
    factory { DeleteProjectFileUseCase(get()) }


    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { CreateTeamViewModel(get(), get()) }
    viewModel { TeamDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CreateJamViewModel(get(), get()) }
    viewModel { JamDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { EditJamViewModel(get(), get()) }
}