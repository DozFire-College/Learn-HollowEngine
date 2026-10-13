@file:Attach(NpcEntity::class)

import kotlinx.coroutines.CancellationException
import java.util.UUID
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity
import ru.hollowhorizon.hollowengine.common.npcs.actions.NpcAction
import ru.hollowhorizon.hollowengine.common.npcs.navigation.MoveResult

val lastInteractionTickByPlayer = mutableMapOf<UUID, Long>()
var activeLook: NpcAction<Unit>? = null
var activeLookOwner: String? = null

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Guide Actions] Узел активен на NPC: uuid=$uuid")
}

fun speedForDistance(distance: Double): Double? = when {
    distance < 2.0 -> null
    distance <= 5.0 -> 0.8
    else -> 2.0
}

onInteract { event ->
    val player = event.player
    val playerUuid = player.uuid
    val now = player.level().gameTime

    if (player.isShiftKeyDown) {
        val previousLook = activeLook
        if (previousLook != null) {
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
                "[Guide Actions] Старый LOOK отменён: owner=$activeLookOwner, status=${previousLook.status}"
            )

            if (previousLook.isActive) {
                previousLook.cancel()
            }
        }

        clearLookTarget()
        activeLook = null
        activeLookOwner = null

        player.send("Эйден: Продолжаю идти, но больше не смотрю на тебя.")
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Actions] LOOK остановлен игроком ${player.scoreboardName}, движение оставлено активным"
        )
        return@onInteract
    }

    val previousTick = lastInteractionTickByPlayer[playerUuid]
    if (previousTick != null && now - previousTick < 20L) {
        return@onInteract
    }
    lastInteractionTickByPlayer[playerUuid] = now

    val playerRef = with(server) {
        player.entityRef
    }

    val distance = distanceTo(player)
    val speed = speedForDistance((distance).toDouble())

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Actions] player=${player.scoreboardName}, distance=$distance, chosenSpeed=${speed ?: "NO_MOVE"}"
    )

    if (speed == null) {
        player.send("Эйден: Ты уже достаточно близко.")
    } else {
        player.send("Эйден: Подойду ближе.")
    }

    val movement = if (speed != null) {
        startMove(
            target = playerRef,
            options = MoveOptions(
                speed = speed,
                arrivalDistance = 2.0,
                unreachable = UnreachablePolicy.FAIL,
                unavailableTarget = UnavailableTargetPolicy.FAIL,
                targetMoveThreshold = 1.0
                
            )
    )
    } else {
        null
    }

    val previousLook = activeLook
    if (previousLook != null) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Actions] Старый LOOK отменён: owner=$activeLookOwner, status=${previousLook.status}"
        )

        if (previousLook.isActive) {
            previousLook.cancel()
        }
    }
    val look = startLookingAt(
        target = playerRef,
        duration = 5.seconds,
        maxAngularSpeed = 240f
    )
   
    activeLook = look
    activeLookOwner = player.scoreboardName

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Actions] Новый LOOK запущен: owner=$activeLookOwner, status=${look.status}"
    )

    try {
        val moveResult = movement?.await()
        when (moveResult) {
            MoveResult.Arrived -> {
                player.send("Эйден: Так лучше. Теперь я тебя слышу.")
            }


            MoveResult.Unreachable -> {
                player.send("Эйден: Между нами нет пути.")
            }

            MoveResult.TargetUnavailable -> {
                ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
                    "[Guide Actions] Цель стала недоступна: " +
                        player.scoreboardName
                )
            }

            null -> {
                ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
                    "[Guide Actions] Движение не требуется: player=${player.scoreboardName}"
                )
            }
        }

        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Actions] player=${player.scoreboardName}, " +
                "moveResult=$moveResult, " +
                "movementStatus=${movement?.status ?: "NO_MOVE"}, " +
                "lookStatus=${look.status}"
        )
    } catch (cancelled: CancellationException) {
        player.send("Эйден: Меня позвал другой путник.")

        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Actions] Действие для ${player.scoreboardName} отменено"
        )
    } finally {
        if (activeLook === look && look.isActive) {
            look.cancel()
        }
        if (activeLook === look) {
            activeLook = null
            activeLookOwner = null
        }
    }
}

onStop {
    stopMoving()
    clearLookTarget()

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Guide Actions] Узел остановлен на NPC: uuid=$uuid")
}
