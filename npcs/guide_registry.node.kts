import java.util.UUID
import kotlinx.coroutines.delay
import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hollowengine.common.coroutines.Ref
import ru.hollowhorizon.hollowengine.common.coroutines.entityRef
import ru.hollowhorizon.hollowengine.common.entities.NpcEntity
import ru.hollowhorizon.hollowengine.common.scripting.annotations.State
import ru.hollowhorizon.hollowengine.common.scripting.state.remember
import ru.hollowhorizon.hollowengine.common.scripting.story.functions.npcs.npc
import ru.hollowhorizon.hollowengine.common.scripting.util.players

fun playerPosition(player: ServerPlayer){
        val position = player.blockPosition()
        }
        
@State("ensure_guide")
suspend fun ensureGuide() {
    val guideName = "Эйден"
    val guideUuidText = remember<String>("guide_uuid") {
        val anchor = server.playerList.players.firstOrNull()
            ?: error("Для первого создания ${guideName} на сервере должен быть игрок")

        val createdGuide = npc(
            pos = anchor.position().add(2.0, 0.0, 2.0),
            name = guideName,
            world = anchor.level().dimension().location().toString(),
        )

        createdGuide.addTag("encore_guide")

        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Registry] ${createdGuide.name.string} создан впервые: uuid=${createdGuide.uuid}"
        )

        createdGuide.uuid.toString()
    }

    val guideUuid = UUID.fromString(guideUuidText)

    val guideRef: Ref<NpcEntity> = with(server) {
        entityRef<NpcEntity>(guideUuid)
    }
    

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[Guide Registry] Сохранённый UUID: $guideUuid"
    )

    while (true) {
        if (!guideRef.isLinkAlive) {
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
                "[Guide Registry] ${guideName} выгружен. Жду его загрузки..."
            )
            delay(15_000L)
            continue
        }

        val guide = guideRef.resolve()
        val guidePos = guide.position()
        val nearestPlayer = server.playerList.players.minByOrNull { player ->
        player.position().distanceToSqr(guidePos)
        }
        if (nearestPlayer == null) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Registry] ${guide.name.string} доступен, но онлайн-игроков нет"
        )
    } else {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Guide Registry] ${guide.name.string} доступен: uuid=${guide.uuid}, " +
                "dimension=${guide.level().dimension().location()}, " +
                "position=${guide.blockPosition()}" +
                "nearest=${nearestPlayer}"
        )

    }

    delay(15_000L)
    }
}