import ru.hollowhorizon.hollowengine.common.utils.currentServerOrNull
import net.minecraft.server.level.ServerPlayer
import kotlin.math.roundToInt
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.player.send

fun healthPercent(player: ServerPlayer): Int {
    val maximum = player.maxHealth

    if (maximum <= 0f) {
        return 0
    }

    return ((player.health / maximum) * 100f).roundToInt()
}
fun healthStatus(percent: Int): String = when {
    percent <= 0 -> "мёртв"
    percent <= 25 -> "критическое"
    percent <= 60 -> "ранен"
    else -> "стабильное"
}
fun yPositionStatus(Y: Int): String = when {
    
    Y < 0 -> "Глубоко под зелмёй"
    Y > 0 && Y < 80 ->  "обычная высота"
    else -> "высоко"
}
fun playerLine(index: Int, player: ServerPlayer): String {
    val position = player.blockPosition()
    val dimension = player.level().dimension().location()
    val percent = healthPercent(player)
    val exp = player.experienceLevel
   
    return "${index + 1}. ${player.name.string} | " +
        "HP ${player.health}/${player.maxHealth} ($percent%) | " +
        "${healthStatus(percent)} | " +
        "мир=$dimension | " +
        "позиция=${position.x}, ${position.y}, ${position.z} |" +
        "${yPositionStatus(position.y)} |" +
        "Опыт= ${exp}"
}
onStart {
    val server = currentServerOrNull()

    if (server == null) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Сервер сейчас недоступен")
    } else {
        val players = server.playerList.players
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("=== ENCORE ONLINE REPORT ===")
         
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Игроков онлайн: ${players.size}")

        if (players.isEmpty()) {
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Сейчас на сервере никого нет")
        } else {
            players.forEachIndexed { index, player ->
                ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(playerLine(index, player))
                val percent = healthPercent(player)
                player.send(
              "Диагностика: здоровье $percent%, состояние - ${healthStatus(percent)}"
                )
            }
            val averageHealth = players
                .map(::healthPercent)
                .average()
                .roundToInt()

            val criticalPlayers = players.count { player ->
                healthPercent(player) <= 25
            }
            
            val playersPotitionOnOtherWorld = players.groupBy { player ->
                player.level().dimension().location().toString()
            }
            val playerWithLowestHealth = players.minByOrNull (::healthPercent)
            playersPotitionOnOtherWorld.forEach { (dimension, playersInDimension) ->
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("В измерени: $dimension игроков $playersInDimension ")   
            }
            val lowestName: String
            val lowestHealthPercent: Int?
               if (playerWithLowestHealth == null) {
                    lowestName = "нет"
                    lowestHealthPercent = null
                } else {
                        lowestName = playerWithLowestHealth.name.string
                        lowestHealthPercent = healthPercent(playerWithLowestHealth)
                }
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Среднее здоровье: $averageHealth%")
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Игроков в критическом состоянии: $criticalPlayers")
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("Игрок с наименьшим хп: $lowestName ($lowestHealthPercent%)")
            for (player in players) {
                if (healthPercent(player) <= 25) {
                player.send("У вас мало хп (${healthPercent(player)}%)")
                }
            }
        }
    }
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("=== REPORT FINISHED ===")
}