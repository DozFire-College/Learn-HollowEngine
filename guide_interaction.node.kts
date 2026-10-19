@file:Attach(NpcEntity::class)

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.util.UUID
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity
import ru.hollowhorizon.hollowengine.common.npcs.actions.NpcAction
import ru.hollowhorizon.hollowengine.common.npcs.navigation.MoveResult

val lastInteractionTickByPlayer = mutableMapOf<UUID, Long>()
var activeLook: NpcAction<Unit>? = null
var activeLookOwner: String? = null

fun speedForDistance(distance: Double): Double? = when {
    distance < 2.0 -> null
    distance <= 5.0 -> 0.8
    else -> 2.0
}

fun guideMoveOptions(speed: Double) = MoveOptions(
    speed = speed,
    arrivalDistance = 2.0,
    unreachable = UnreachablePolicy.FAIL,
    unavailableTarget = UnavailableTargetPolicy.FAIL,
    targetMoveThreshold = 1.0
)

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Guide Actions] Узел активен на NPC: uuid=$uuid")
}

onInteract { event ->
    val player = event.player
    val playerUuid = player.uuid
    val now = player.level().gameTime

    // Shift + ПКМ: гасим взгляд, но движение оставляем активным
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

    val distance = position().distanceTo(player.position())
    val initialSpeed = speedForDistance(distance)

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Actions] player=${player.scoreboardName}, distance=$distance, chosenSpeed=${initialSpeed ?: "NO_MOVE"}"
    )

    if (initialSpeed == null) {
        player.send("Эйден: Ты уже достаточно близко.")
    } else {
        player.send("Эйден: Подойду ближе.")
    }

    // Конфликт взглядов: старый LOOK отменяем перед запуском нового
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

    // Движение с постоянным пересчётом скорости по расстоянию
    var currentSpeed = initialSpeed ?: 0.8
    var movement: NpcAction<MoveResult>? = if (initialSpeed != null) {
        startMove(target = playerRef, options = guideMoveOptions(initialSpeed))
    } else {
        null
    }
    var moveResult: MoveResult? = null
    var movedOnce = movement != null

    try {
        while (true) {
            if (!playerRef.isLinkAlive || level().dimension() != player.level().dimension()) {
                moveResult = MoveResult.TargetUnavailable
                break
            }

            val currentDistance = position().distanceTo(player.position())
            if (currentDistance <= 2.0) {
                moveResult = if (movedOnce) MoveResult.Arrived else null
                break
            }

            val requiredSpeed = speedForDistance(currentDistance) ?: 0.8
            val isMovementActive = movement?.isActive == true

            if (!isMovementActive || requiredSpeed != currentSpeed) {
                if (isMovementActive) {
                    movement?.cancel()
                }

                currentSpeed = requiredSpeed
                movement = startMove(target = playerRef, options = guideMoveOptions(currentSpeed))
                movedOnce = true
            }

            delay(250L)
        }

        stopMoving()

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
        if (movement?.isActive == true) {
            movement?.cancel()
        }
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
