import ru.hollowhorizon.hollowengine.common.events.SubscribeEvent
import ru.hollowhorizon.hollowengine.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hollowengine.common.scripting.nodes.EntityNodeRuntime

private val PLAYER_STORY_NODE =
    "scripts/encore/course/lesson_05/player_story.node.kts"

@SubscribeEvent
fun onPlayerJoin(event: PlayerEvent.Join) {
    val wasAttached = EntityNodeRuntime.attach(
        entity = event.player,
        path = PLAYER_STORY_NODE
    )

    val result = if (wasAttached) {
        "личный node прикреплён"
    } else {
        "личный node уже был прикреплён"
    }

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Lesson 05] ${event.player.scoreboardName}: $result"
    )
    
}
