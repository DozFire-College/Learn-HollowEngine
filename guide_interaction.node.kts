@file:Attach(NpcEntity::class)

import java.util.UUID
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity

val conversationCountByPlayer = mutableMapOf<UUID, Int>()
val lastInteractionTickByPlayer = mutableMapOf<UUID, Long>()

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Guide Interaction] Узел прикреплён к NPC: uuid=$uuid")
}

onInteract { event ->
    val player = event.player
    val playerUuid = player.uuid
    val now = player.level().gameTime

    val previousTick = lastInteractionTickByPlayer[playerUuid]

    if (previousTick != null && now - previousTick < 60L) {
        return@onInteract
    }

    lastInteractionTickByPlayer[playerUuid] = now

    val conversationNumber =
        (conversationCountByPlayer[playerUuid] ?: 0) + 1

    conversationCountByPlayer[playerUuid] = conversationNumber

    when (conversationNumber) {
        1 -> {
            player.send("Эйден: Ты всё-таки пришёл, ${player.scoreboardName}.")
            player.send("Эйден: Вороны кружат над поместьем уже третью ночь.")
        }

        2 -> {
            player.send("Эйден: Старые ворота открываются только после заката.")
            player.send("${player.scoreboardName}")
        }
        3 -> {
            player.send("Эйден: Иди туда, тебя там ждут.")
        }

        else -> {
            player.send("Эйден: Пока мне больше нечего добавить.")
        }
    }

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Interaction] " +
            "player=${player.scoreboardName}, " +
            "conversation=$conversationNumber, " +
            "npc=$uuid"
    )
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Guide Interaction] Узел снят с NPC: uuid=$uuid")
}