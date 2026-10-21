
import ru.hollowhorizon.hollowengine.common.scripting.annotations.State
import ru.hollowhorizon.hollowengine.common.scripting.state.waitUntil
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.player.send
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val BELL_WAIT_NAME = "raven_clock/midnight_bell"

private val TEST_MODE = true
private val TEST_DELAY_SECONDS = 90L

private val EVENT_ZONE = ZoneId.of("Europe/Berlin")
private val BELL_TIME = LocalTime.of(0, 0)

private val LOCAL_FORMAT =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")

fun nextBellInstant(
    now: Instant = Instant.now()
): Instant {
    if (TEST_MODE) {
        return now.plusSeconds(TEST_DELAY_SECONDS)
    }

    val nowInZone = now.atZone(EVENT_ZONE)

    var candidate = nowInZone
        .toLocalDate()
        .atTime(BELL_TIME)
        .atZone(EVENT_ZONE)

    if (!candidate.toInstant().isAfter(now)) {
        candidate = candidate.plusDays(1)
    }

    return candidate.toInstant()
}

fun localTimeText(instant: Instant): String {
    return LOCAL_FORMAT.format(
        instant.atZone(EVENT_ZONE)
    )
}

fun ringBell() {
    val firedAt = Instant.now()

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Clock] Midnight bell: " +
            localTimeText(firedAt)
    )

    server.playerList.players.forEach { player ->
        player.send(
            "§5Ада постучала в колокол, она вас спасла "
        )
    }
}

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Clock] Node started: $path"
    )
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Clock] Node stopped: $path"
    )
}

@State("daily_bell")
suspend fun dailyBell() {
    while (true) {
        val target = nextBellInstant()

        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Raven Clock] Next bell: " +
                localTimeText(target) +
                ", testMode=$TEST_MODE"
        )

        waitUntil(
            name = BELL_WAIT_NAME,
            instant = target
        )

        ringBell()
    }
}
