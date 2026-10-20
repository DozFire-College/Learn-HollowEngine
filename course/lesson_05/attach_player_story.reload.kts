import ru.hollowhorizon.hollowengine.common.events.SubscribeEvent
import ru.hollowhorizon.hollowengine.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hollowengine.common.scripting.nodes.EntityNodeRuntime
import ru.hollowhorizon.hollowengine.common.scripting.state.StateContext

private val PLAYER_STORY_NODE =
    "scripts/encore/course/lesson_05/player_story.node.kts"

private val MANSION_QUEST_NODE =
    "scripts/encore/quests/raven_mansion/mansion_intro.node.kts"

@SubscribeEvent
fun onPlayerJoin(event: PlayerEvent.Join) {
    val storyAttached = EntityNodeRuntime.attach(
        entity = event.player,
        path = PLAYER_STORY_NODE
    )

    val questAttached = EntityNodeRuntime.attach(
        entity = event.player,
        path = MANSION_QUEST_NODE,
        context = StateContext(
            nextState = "arrival"
        )
    )

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Player Nodes] ${event.player.scoreboardName}: " +
            "storyAttached=$storyAttached, " +
            "questAttached=$questAttached"
    )
}