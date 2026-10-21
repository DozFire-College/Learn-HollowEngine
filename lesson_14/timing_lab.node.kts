import kotlinx.coroutines.delay
import ru.hollowhorizon.hollowengine.common.scripting.annotations.State
import ru.hollowhorizon.hollowengine.common.scripting.state.stateTag
import ru.hollowhorizon.hollowengine.common.scripting.state.waitPersistent
import ru.hollowhorizon.hollowengine.common.scripting.state.waitRealtime
import ru.hollowhorizon.hollowengine.common.scripting.state.waitUntil
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.player.send
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

private val PLAIN_DELAY_MILLIS = 20_000L
private val PERSISTENT_TICKS = 200
private val REALTIME_DURATION = 20.seconds
private val ABSOLUTE_WAIT_SECONDS = 30L
private val NEW_ABSOLUTE_WAIT_SECONDS = 60L

fun announce(message: String) {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Timing Lab] $message")

    server.playerList.players.forEach { player ->
        player.send("§8[Timing Lab] §7$message")
    }
}

suspend fun countEntry(stateName: String): Int {
    val tag = stateTag()
    val key = "lesson_14/entries/$stateName"
    val next = tag.getInt(key) + 1

    tag.putInt(key, next)
    return next
}

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Timing Lab] Node started: $path")
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Timing Lab] Node stopped: $path")
}

@State("plain_delay")
suspend fun plainDelay() {
    val entry = countEntry("plain_delay")

    announce(
        "plain_delay: вход №$entry; " +
            "начинаю обычные 20 секунд"
    )

    delay(PLAIN_DELAY_MILLIS)

    announce("plain_delay: ожидание завершено")
}

@State("persistent_ticks")
suspend fun persistentTicks() {
    val entry = countEntry("persistent_ticks")

    announce(
        "persistent_ticks: вход №$entry; " +
            "жду 400 загруженных тиков"
    )

    waitPersistent(
        name = "lesson_14/ticks_400",
        ticks = PERSISTENT_TICKS
    )

    announce("persistent_ticks: дедлайн игровых тиков достигнут")
}

@State("realtime_duration")
suspend fun realtimeDuration() {
    val entry = countEntry("realtime_duration")

    announce(
        "realtime_duration: вход №$entry; " +
            "жду 20 реальных секунд"
    )

    waitRealtime(
        name = "lesson_14/realtime_20s",
        duration = REALTIME_DURATION
    )

    announce("realtime_duration: реальный дедлайн достигнут")
}

@State("absolute_instant")
suspend fun absoluteInstant() {
    val entry = countEntry("absolute_instant")
    val requestedDeadline =
        Instant.now().plusSeconds(ABSOLUTE_WAIT_SECONDS)

    announce(
        "absolute_instant: вход №$entry; " +
            "запрошенный дедлайн=$requestedDeadline"
    )

    waitUntil(
        name = "lesson_14/absolute_30s",
        instant = requestedDeadline
    )

    announce(
        "absolute_instant: завершено в ${Instant.now()}"
    )
@State("new_absolute_instant")
    suspend fun newAbsoluteInstant(){
        val entry = countEntry("new_absolute_instant")
        val requestedDeadline =
        Instant.now().plusSeconds(NEW_ABSOLUTE_WAIT_SECONDS)
    announce(
        "new_absolute_instant: вход №$entry; " +
            "запрошенный дедлайн=$requestedDeadline"
    )

    waitUntil(
        name = "lesson_14/absolute_60s",
        instant = requestedDeadline
    )

    announce(
        "absolute_instant: завершено в ${Instant.now()}"
    )
    }   
}