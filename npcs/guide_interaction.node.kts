@file:Import("../quests/raven_mansion/quest_data.kts")
@file:Attach(NpcEntity::class)

import java.util.UUID
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity

val GuideMet = RavenQuestData.GuideMet
val GuideVisits = RavenQuestData.GuideVisits
val GuideReputation = RavenQuestData.GuideReputation
val GuideTotalConversations =
    RavenQuestData.GuideTotalConversations
val GuideTalked = RavenQuestData.GuideTalked
    val GuideWarnings = dataKey<Int>("encore:guide_warnings") { 0 }
val GuideLastVisitor = dataKey<String>("encore:guide_last_visitor") { "никто" }
val lastInteractionTickByPlayer = mutableMapOf<UUID, Long>()

fun reputationStatus(reputation: Int): String = when {
    reputation >= 100 -> "легенда"
    reputation >= 60 -> "доверенный"
    reputation >= 30 -> "друг"
    reputation >= 10 -> "знакомый"
    else -> "чужак"
}

fun resetGuideData(player: Player) {
    player.data.remove(GuideMet)
    player.data.remove(GuideVisits)
    player.data.remove(GuideReputation)
    player.data.remove(GuideWarnings)
    player.data.remove(GuideTotalConversations)
    player.data.remove(GuideLastVisitor)
}

onInteract { event ->
    val player = event.player
    val playerUuid = player.uuid
    val now = player.level().gameTime

    val previousTick = lastInteractionTickByPlayer[playerUuid]
    if (previousTick != null && now - previousTick < 20L) {
        return@onInteract
    }
    lastInteractionTickByPlayer[playerUuid] = now

    if (player.isShiftKeyDown) {
        resetGuideData(player)
        data.remove(GuideTotalConversations)
        player.send("Эйден: Забудь всё, что было. Начнём знакомство заново.")
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Data] Данные сброшены для ${player.scoreboardName}"
        )
        return@onInteract
    }
    
    player.data[GuideTalked] = true
    val wasMet = player.data.getOrPut(GuideMet)
    val visits = player.data.update(GuideVisits) { it + 1 }
    val lastVisitor = player.data.update(GuideLastVisitor) { player.name.string }
    val reputation = if (!wasMet) {
        player.data[GuideMet] = true
        player.data.update(GuideReputation) { it + 5 }
    } else {
        player.data.update(GuideReputation) {
            (it + 1).coerceAtMost(100)
        }
    }

    val totalConversations = data.update(GuideTotalConversations) { it + 1 }

    var warnings = player.data.getOrPut(GuideWarnings)

    when {
        !wasMet -> {
            player.send("Эйден: Ты всё-таки пришёл, ${player.scoreboardName}.")
            player.send("Эйден: Я запомню наше знакомство.")
        }

        visits == 2 -> {
            player.send("Эйден: Вижу, ты вернулся. Старые ворота открываются после заката.")
        }

        visits >= 5 -> {
            player.send("Эйден: Зачем ты пришёл? Иди отсюда!")
        }

        else -> {
            warnings = player.data.update(GuideWarnings) { it + 1 }
            player.send("Эйден: Я тебе рад конечно, но давай, иди отсюда.")
        }
    }

    val status = reputationStatus(reputation)
    player.send("Данные: визиты=$visits, репутация=$reputation ($status), предупреждения=$warnings")

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Data] player=${player.scoreboardName}, " +
            "visits=$visits, reputation=$reputation ($status), " +
            "warnings=$warnings, " +
            "npcTotal=$totalConversations ," +
            "lastVisitor=$lastVisitor"
    )
}