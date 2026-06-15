package software.setixx.chimu.api.service

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.setixx.chimu.api.domain.GameJam
import software.setixx.chimu.api.domain.GameJamStatus
import software.setixx.chimu.api.domain.RegistrationStatus
import software.setixx.chimu.api.repository.GameJamRepository
import software.setixx.chimu.api.repository.JamTeamRegistrationRepository
import software.setixx.chimu.api.repository.TeamMemberRepository
import software.setixx.chimu.api.service.RatingService
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
class JamSchedulerService(
    private val taskScheduler: TaskScheduler,
    private val gameJamRepository: GameJamRepository,
    private val executor: JamTransitionExecutor
) {
    private val log = LoggerFactory.getLogger(JamSchedulerService::class.java)
    private val scheduledTasks = ConcurrentHashMap<String, MutableList<ScheduledFuture<*>>>()

    @EventListener(ApplicationReadyEvent::class)
    fun restoreOnStartup() {
        log.info("Restoring jam transition tasks on startup")
        val activeStatuses = listOf(
            GameJamStatus.ANNOUNCED,
            GameJamStatus.REGISTRATION_OPEN,
            GameJamStatus.REGISTRATION_CLOSED,
            GameJamStatus.IN_PROGRESS,
            GameJamStatus.JUDGING
        )
        val activeJams = gameJamRepository.findAllByStatusIn(activeStatuses)
        activeJams.forEach { scheduleTransitions(it) }
        log.info("Restored tasks for ${activeJams.size} jams")
    }

    fun scheduleTransitions(jam: GameJam) {
        cancelExisting(jam.publicId.toString())

        val tasks = mutableListOf<ScheduledFuture<*>>()
        val now = Instant.now()
        val jamId = jam.publicId.toString()

        fun scheduleIfFuture(time: Instant, targetStatus: GameJamStatus) {
            if (time.isAfter(now)) {
                val future = taskScheduler.schedule(
                    { executor.execute(jamId, targetStatus) },
                    time
                )
                tasks += future
            } else {
                log.debug("Skipping past transition for jam $jamId -> $targetStatus (was at $time)")
            }
        }

        when (jam.status) {
            GameJamStatus.ANNOUNCED -> {
                scheduleIfFuture(jam.registrationStart, GameJamStatus.REGISTRATION_OPEN)
                scheduleIfFuture(jam.registrationEnd, GameJamStatus.REGISTRATION_CLOSED)
                scheduleIfFuture(jam.jamStart,        GameJamStatus.IN_PROGRESS)
                scheduleIfFuture(jam.judgingStart,    GameJamStatus.JUDGING)
                scheduleIfFuture(jam.judgingEnd,      GameJamStatus.COMPLETED)
            }
            GameJamStatus.REGISTRATION_OPEN -> {
                scheduleIfFuture(jam.registrationEnd, GameJamStatus.REGISTRATION_CLOSED)
                scheduleIfFuture(jam.jamStart,        GameJamStatus.IN_PROGRESS)
                scheduleIfFuture(jam.judgingStart,    GameJamStatus.JUDGING)
                scheduleIfFuture(jam.judgingEnd,      GameJamStatus.COMPLETED)
            }
            GameJamStatus.REGISTRATION_CLOSED -> {
                scheduleIfFuture(jam.jamStart,     GameJamStatus.IN_PROGRESS)
                scheduleIfFuture(jam.judgingStart, GameJamStatus.JUDGING)
                scheduleIfFuture(jam.judgingEnd,   GameJamStatus.COMPLETED)
            }
            GameJamStatus.IN_PROGRESS -> {
                scheduleIfFuture(jam.judgingStart, GameJamStatus.JUDGING)
                scheduleIfFuture(jam.judgingEnd,   GameJamStatus.COMPLETED)
            }
            GameJamStatus.JUDGING -> {
                scheduleIfFuture(jam.judgingEnd, GameJamStatus.COMPLETED)
            }
            else -> return
        }

        scheduledTasks[jamId] = tasks
        log.info("Scheduled ${tasks.size} transitions for jam $jamId (status=${jam.status})")
    }

    fun cancelExisting(jamId: String) {
        scheduledTasks.remove(jamId)?.forEach {
            it.cancel(false)
            log.info("Task for jam $jamId has been cancelled")
        }
    }
}