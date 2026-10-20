import ru.hollowhorizon.hollowengine.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.player.send

ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
    "[LESSON 02] join_listener.reload.kts загружен"
)

@SubscribeEvent
fun onPlayerJoin(event: PlayerEvent.Join) {
    event.player send "§a[HollowEngine] уютное сосание работает"
}