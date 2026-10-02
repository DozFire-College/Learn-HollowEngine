@file:Attach(ServerPlayer::class)

import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hollowengine.common.scripting.util.players
import ru.hollowhorizon.hollowengine.common.utils.currentServer

var visits = 0
var onlineSeconds = 0L

fun lastKnownDimension(player: ServerPlayer): String {
    return player.level().dimension().location().toString()
}
onLoad { context ->
    visits = context.tag.getInt("visits")
    onlineSeconds = context.tag.getLong("onlineSeconds")
}

onStart {
    visits += 1
    
    send("§6Личная история загружена")
    send("§7Номер визита: §f$visits")
    send("Время игры: ${onlineSeconds}" )
    val dimention= lastKnownDimension(currentServer.players.first())
    send("Последнее измерение: ${dimention}")

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Lesson 05] Личный node запущен: визит=$visits"
    )
}
onUpdate(every = 20.ticks){
    onlineSeconds += 1L
}

onSave { context ->
    context.tag.putInt("visits", visits)
    context.tag.putLong("onlineSeconds", onlineSeconds)
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Lesson 05] Личный node остановлен: $path"
    )
}