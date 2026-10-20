import ru.hollowhorizon.hollowengine.common.attachments.sync.onJoin
import ru.hollowhorizon.hollowengine.common.coroutines.playerRef
import ru.hollowhorizon.hollowengine.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.player.send
import ru.hollowhorizon.hollowengine.common.scripting.util.players
import ru.hollowhorizon.hollowengine.common.utils.currentServer

ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
    "[homework] welcome.reload.kts загружен"
)


@SubscribeEvent
fun onPlayerJoin(event: PlayerEvent.Join) {
    val playerName = event.player.name.string
    event.player send "$playerName зашёл на сервер"
}
