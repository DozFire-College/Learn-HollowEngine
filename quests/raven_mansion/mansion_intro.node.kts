@file:Attach(ServerPlayer::class)
@file:Import("quest_data.kts")

import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity

private val CHECK_INTERVAL_MS = 500L
private val GUIDE_SEARCH_RADIUS = 48.0
private val GUIDE_ARRIVAL_DISTANCE = 6.0

private val MANSION_DIMENSION =
    "minecraft:overworld"

private val MANSION_ENTRANCE =
    BlockPos(40, -60, 14)

private val MANSION_ENTRANCE_RADIUS = 3.0

private val STEALTH_APPROACH_MARKER =
    BlockPos(34, -60, 6)

private val FORCE_APPROACH_MARKER =
    BlockPos(40, -60, 15)

private val MANSION_GATE =
    BlockPos(53, -60, 17)

private val MANSION_GATE_RADIUS = 3.0
private val APPROACH_CHOICE_MEMORY =
    "raven_mansion/approach_choice"

private val APPROACH_STEALTH = "stealth"
private val APPROACH_FORCE = "force"

private val APPROACH_CHECK_INTERVAL_MS = 250L
private val APPROACH_MARKER_RADIUS = 1.35

fun nearestLoadedGuide(
    player: ServerPlayer
): NpcEntity? {
    return player.serverLevel()
        .getEntitiesOfClass(
            NpcEntity::class.java,
            player.boundingBox.inflate(GUIDE_SEARCH_RADIUS)
        ) { npc ->
            "encore_guide" in npc.tags
        }
        .minByOrNull { npc ->
            player.distanceToSqr(npc)
        }
}

fun isNearGuide(player: ServerPlayer): Boolean {
    val guide = nearestLoadedGuide(player)
        ?: return false

    val maximumDistanceSquared =
        GUIDE_ARRIVAL_DISTANCE * GUIDE_ARRIVAL_DISTANCE

    return player.distanceToSqr(guide) <= maximumDistanceSquared
}
fun isNearMarker(
    player: ServerPlayer,
    marker: BlockPos
): Boolean {
    // Не засчитывать совпадение координат из другой размерности.
    if (player.level().dimension().location().toString() != MANSION_DIMENSION) {
        return false
    }
    val markerCenter = Vec3.atCenterOf(marker)
    val maximumDistanceSquared =
        APPROACH_MARKER_RADIUS * APPROACH_MARKER_RADIUS

    return player.position().distanceToSqr(markerCenter) <=
        maximumDistanceSquared
}
fun isAtMansionEntrance(
    player: ServerPlayer
): Boolean {
    val currentDimension =
        player.level().dimension().location().toString()

    if (currentDimension != MANSION_DIMENSION) {
        return false
    }

    val entrancePosition =
        Vec3.atCenterOf(MANSION_ENTRANCE)

    val maximumDistanceSquared =
        MANSION_ENTRANCE_RADIUS *
            MANSION_ENTRANCE_RADIUS

    return player.position()
        .distanceToSqr(entrancePosition) <=
        maximumDistanceSquared
}

fun isAtMansionGate(
    player: ServerPlayer
): Boolean {
    val currentDimension =
        player.level().dimension().location().toString()

    if (currentDimension != MANSION_DIMENSION) {
        return false
    }

    val gatePosition =
        Vec3.atCenterOf(MANSION_GATE)

    val maximumDistanceSquared =
        MANSION_GATE_RADIUS *
            MANSION_GATE_RADIUS

    return player.position()
        .distanceToSqr(gatePosition) <=
        maximumDistanceSquared
}
suspend fun waitForApproachChoice(): String {
    while (true) {
        val player = playerRef(uuid).resolve()

        if (isNearMarker(player, STEALTH_APPROACH_MARKER)) {
            return APPROACH_STEALTH
        }

        if (isNearMarker(player, FORCE_APPROACH_MARKER)) {
            return APPROACH_FORCE
        }

        delay(APPROACH_CHECK_INTERVAL_MS)
    }
}
suspend fun waitForCondition(
    condition: (ServerPlayer) -> Boolean,
) {
    while (true) {
        val player = playerRef(uuid).resolve()
        if (condition(player)) return
        delay(CHECK_INTERVAL_MS)
    }
}

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest] Node активен для $scoreboardName"
    )
}
suspend fun countStateEntry(
    stateName: String,
    counterKey: String = "debug_state_entries/$stateName"
): Int {
    val tag = stateTag()

    val nextValue = tag.getInt(counterKey) + 1
    tag.putInt(counterKey, nextValue)

    return nextValue
}

@State("arrival")
suspend fun arrival() {
    val player = playerRef(uuid).resolve()

    // После detach/attach свежий StateContext может снова начать с arrival.
    // Если вход уже пройден, не заставляем игрока снова искать проводника.
    if (player.data.getOrPut(RavenQuestData.IntroCompleted)) {
        chooseApproach()
        return
    }

    runOnce("arrival/objective_message_v1") {
        playerRef(uuid).resolve().send("§6Новое задание: Пепел Вороньего Поместья")
        playerRef(uuid).resolve().send("§7Цель: найдите проводника Эйдена.")

        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Raven Quest / SAFE] " +
                "player=${player.scoreboardName}, " +
                "state=arrival, " +
                "objective_message_v1=sent_first_time_only"
        )
    }

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest] ${player.scoreboardName}: " +
            "state=arrival"
    )

    waitForCondition(::isNearGuide)

    playerRef(uuid).resolve().send("§7Цель обновлена: поговорите с Эйденом.")

    talkToGuide()
}

@State("talk_to_guide")
suspend fun talkToGuide() {
    val player = playerRef(uuid).resolve()

    val talkToGuideEntries =
        countStateEntry(
            "talk_to_guide",
            "talk_to_guide_entries"
        )

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest / SAFE] " +
            "player=${player.scoreboardName}, " +
            "state=talk_to_guide, " +
            "talk_to_guide_entries=$talkToGuideEntries"
    )

    waitForCondition { currentPlayer ->
        currentPlayer.data[RavenQuestData.GuideTalked] == true
    }

    playerRef(uuid).resolve().send(
        "§7Цель обновлена: осмотрите ворота поместья."
    )

    inspectGate()
}

@State("inspect_gate")
suspend fun inspectGate() {
    val player = playerRef(uuid).resolve()

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest] ${player.scoreboardName}: " +
            "state=inspect_gate"
    )

    waitForCondition(::isAtMansionGate)

    val currentPlayer = playerRef(uuid).resolve()
    currentPlayer.data[RavenQuestData.GateInspected] = true

    currentPlayer.send(
        "§7Цель обновлена: доберитесь до входа в поместье."
    )

    enterMansion()
}

private val MANSION_COMPLETION_XP = 25

@State("enter_mansion")
suspend fun enterMansion() {
    val player = playerRef(uuid).resolve()
    val entryNumber = countStateEntry("enter_mansion")

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest / SAFE] " +
            "player=${player.scoreboardName}, " +
            "state=enter_mansion, entry=$entryNumber"
    )

    if (!player.data.getOrPut(RavenQuestData.MansionEntered)) {
        runOnce("enter_mansion/objective_message_v1") {
            playerRef(uuid).resolve().send(
                "§7Цель: доберитесь до входа в поместье."
            )
        }

        waitForCondition(::isAtMansionEntrance)
    }

    val currentPlayer = playerRef(uuid).resolve()

    runOnce("enter_mansion/completion_v1") {
        currentPlayer.data[RavenQuestData.MansionEntered] = true
        currentPlayer.data[RavenQuestData.IntroCompleted] = true

        if (!currentPlayer.data.getOrPut(RavenQuestData.EntranceRewardClaimed)) {
            currentPlayer.giveExperiencePoints(MANSION_COMPLETION_XP)
            currentPlayer.data[RavenQuestData.EntranceRewardClaimed] = true
            currentPlayer.send("§eНаграда: $MANSION_COMPLETION_XP очков опыта.")
        } else {
            currentPlayer.send("§8Награда за этот этап уже была получена.")
        }

        currentPlayer.send("§aЭтап завершён: вход в поместье найден.")
        currentPlayer.send("§8Продолжение откроется в следующих уроках.")
    }
    chooseApproach()
}
@State("choose_approach")
suspend fun chooseApproach() {
    val player = playerRef(uuid).resolve()


    val attempt = stateGetOrPut(
        RavenQuestData.ApproachSceneAttempt
    )

    val stepBeforeChoice = stateGetOrPut(
        RavenQuestData.ApproachSceneStep
    )

    if (stepBeforeChoice < 1) {
        player.send("§6Выберите способ проникновения.")
        player.send("§aСправа: пройти скрытый вход.")
        player.send("§cСлева: войти через главные ворота.")

        stateSet(
            RavenQuestData.ApproachSceneStep,
            1
        )
    }

    val approach = remember<String>(
        APPROACH_CHOICE_MEMORY
    ) {
        waitForApproachChoice()
    }

    val stepAfterChoice = stateGetOrPut(
        RavenQuestData.ApproachSceneStep
    )

    if (stepAfterChoice < 2) {
        when (approach) {
            APPROACH_STEALTH -> {
                player.send(
                    "§aВыбран скрытный путь через служебный двор."
                )
            }

            APPROACH_FORCE -> {
                player.send(
                    "§cВыбран прямой путь через главные ворота."
                )
            }

            else -> error(
                "Unknown approach stored in state: $approach"
            )
        }

        stateSet(
            RavenQuestData.ApproachConfirmed,
            true
        )

        stateSet(
            RavenQuestData.ApproachSceneStep,
            2
        )
    }

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest / Approach] " +
            "player=${player.scoreboardName}, " +
            "attempt=$attempt, " +
            "approach=$approach, " +
            "confirmed=" +
            stateGetOrPut(
                RavenQuestData.ApproachConfirmed
            )
    )

    approachChosen()
}
@State("approach_chosen")
suspend fun approachChosen() {
    val player = playerRef(uuid).resolve()

    val approach = remember<String>(
        APPROACH_CHOICE_MEMORY
    ) {
        error("Approach choice is missing")
    }

    val step = stateGetOrPut(
        RavenQuestData.ApproachSceneStep
    )

    if (step < 3) {
        val objective = when (approach) {
            APPROACH_STEALTH ->
                "§7Новая цель: найти техническое помещение"

            APPROACH_FORCE ->
                "§7Новая цель: Осмотрите на наличие солдат."

            else -> error("Unknown approach: $approach")
        }

        player.send(objective)

        stateSet(
            RavenQuestData.ApproachSceneStep,
            3
        )
    }
}

@State("reset_approach")
suspend fun resetApproach() {
    val player = playerRef(uuid).resolve()

    val choiceWasRemoved = forget(
        APPROACH_CHOICE_MEMORY
    )

    val stepWasRemoved = stateRemove(
        RavenQuestData.ApproachSceneStep
    )

    val confirmationWasRemoved = stateRemove(
        RavenQuestData.ApproachConfirmed
    )

    val nextAttempt = stateUpdate(
        RavenQuestData.ApproachSceneAttempt
    ) { current ->
        current + 1
    }

    player.send(
        "§eСцена выбора сброшена. Попытка №$nextAttempt."
    )

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest / Approach Reset] " +
            "player=${player.scoreboardName}, " +
            "choiceRemoved=$choiceWasRemoved, " +
            "stepRemoved=$stepWasRemoved, " +
            "confirmationRemoved=$confirmationWasRemoved, " +
            "attempt=$nextAttempt"
    )

    chooseApproach()
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Raven Quest] Node остановлен для " +
            scoreboardName
    )
}