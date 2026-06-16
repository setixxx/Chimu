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
import software.setixx.chimu.presentation.jam.details.judging.JudgingViewModel
import software.setixx.chimu.presentation.jam.details.leaderboard.LeaderboardViewModel
import software.setixx.chimu.presentation.jam.details.management.ManagementViewModel
import software.setixx.chimu.presentation.jam.details.project.ProjectViewModel
import software.setixx.chimu.presentation.jam.details.overview.OverviewViewModel
import software.setixx.chimu.presentation.jam.edit.EditJamViewModel
import software.setixx.chimu.presentation.main.MainViewModel
import software.setixx.chimu.presentation.profile.own.OwnProfileViewModel
import software.setixx.chimu.presentation.splash.SplashViewModel
import software.setixx.chimu.presentation.team.details.TeamDetailsViewModel
import software.setixx.chimu.presentation.team.create.CreateTeamViewModel
import software.setixx.chimu.presentation.profile.alien.AlienProfileViewModel
import software.setixx.chimu.presentation.profile.roleupgrade.RoleUpgradeViewModel

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
    single { JamPublicationApi(get<KtorClient>().httpClient) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            get(),
            get(),
            get()
        )
    }
    single<GameJamRepository> {
        GameJamRepositoryImpl(
            get(),
            get()
        )
    }
    single<JudgeRepository> {
        JudgeRepositoryImpl(
            get(),
            get()
        )
    }
    single<LeaderboardRepository> {
        LeaderboardRepositoryImpl(
            get(),
            get()
        )
    }
    single<ProjectRepository> {
        ProjectRepositoryImpl(
            get(),
            get()
        )
    }
    single<ProjectFileRepository> {
        ProjectFileRepositoryImpl(
            get(),
            get()
        )
    }
    single<RatingCriteriaRepository> {
        RatingCriteriaRepositoryImpl(
            get(),
            get()
        )
    }
    single<RatingRepository> {
        RatingRepositoryImpl(
            get(),
            get()
        )
    }
    single<SkillRepository> {
        SkillRepositoryImpl(
            get(),
            get()
        )
    }
    single<SpecializationRepository> {
        SpecializationRepositoryImpl(
            get(),
            get()
        )
    }
    single<TeamRegistrationRepository> {
        TeamRegistrationRepositoryImpl(
            get(),
            get())
    }
    single<TeamRepository> {
        TeamRepositoryImpl(
            get(),
            get()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            get(),
            get()
        )
    }
    single<RoleUpgradeRepository> {
        RoleUpgradeRepositoryImpl(
            get(),
            get()
        )
    }
    single<JamTransferRepository> {
        JamTransferRepositoryImpl(
            get(),
            get()
        )
    }
    single<JamPublicationRepository> {
        JamPublicationRepositoryImpl(
            get(),
            get()
        )
    }

    factory { ObserveSelectedJamUseCase(get()) }
    factory { ObserveUserTeamsUseCase(get()) }
    factory { ObserverUserUseCase(get()) }
    factory { ObserveJamsUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { GetSavedEmailUseCase(get()) }
    factory { CheckAuthStatusUseCase(get()) }
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
    factory { GetUserByIdUseCase(get()) }
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
    factory { DeleteProjectUseCase(get()) }
    factory { GetJudgeProgressUseCase(get()) }
    factory { GetJamStatisticsUseCase(get()) }
    factory { PublishJamUseCase(get()) }
    factory { UploadJamBannerUseCase(get()) }
    factory { DeleteJamBannerUseCase(get()) }
    factory { CancelJamUseCase(get()) }
    factory { GetLeaderboardUseCase(get()) }
    factory { GetMyRatingsUseCase(get()) }
    factory { RateProjectUseCase(get()) }
    factory { DeleteProjectRatingUseCase(get()) }
    factory { UpdateProjectRatingUseCase(get()) }
    factory { GetProjectRatingsUseCases(get()) }
    factory { DeleteProfileUseCase(get()) }
    factory { GetUserByNicknameUseCase(get()) }
    factory { UpdateProjectUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
    factory { ForceJamStatusUseCase(get()) }
    single { DownloadProjectFileUseCase(get()) }

    viewModel {
        SplashViewModel(get())
    }
    viewModel {
        LoginViewModel(
            get(),
            get())
    }
    viewModel {
        RegisterViewModel(get())
    }
    viewModel {
        MainViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        OwnProfileViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        CreateTeamViewModel(
            get(),
            get())
    }
    viewModel {
        TeamDetailsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get())
    }
    viewModel {
        CreateJamViewModel(
        get(),
        get())
    }
    viewModel {
        EditJamViewModel(
        get(),
        get()
        )
    }

    viewModel {
        JamDetailsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        OverviewViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        ProjectViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        JudgingViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        ManagementViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        LeaderboardViewModel(
            get(),
            get())
    }
    viewModel { params ->
        AlienProfileViewModel(
            userId = params.get(),
            getUserByIdUseCase = get()
        )
    }

    viewModel {
        RoleUpgradeViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
