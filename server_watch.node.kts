var completedUpdates = 0L
var lastOnline = -1
var peakOnline = 0
var onlineUpdates = 0L
onLoad { context ->
    completedUpdates = context.tag.getLong("completed_updates")
    lastOnline = context.tag.getInt("last_online")
    peakOnline = context.tag.getInt("peakOnline")

    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Данные загружены")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Восстановлено обновлений: $completedUpdates")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Последний сохранённый онлайн: $lastOnline")
}

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Серверный узел запущен")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Путь узла: $path")
}

onUpdate(100.ticks) {
    completedUpdates += 1
    
    val players = server.playerList.players
    val currentOnline = players.size
    
    
    if (currentOnline != lastOnline) {
        onlineUpdates += 1 
        val names = players
            .map { player -> player.scoreboardName }
            .joinToString()
            .ifBlank { "никого" }
                
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Онлайн изменился: $lastOnline -> $currentOnline")
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Сейчас на сервере: $names")
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Homework 04] Обновлений онлайна $onlineUpdates")
    }

    if (completedUpdates % 6L == 0L) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[Lesson 04] Узел жив: " +
                "обновлений=$completedUpdates, онлайн=$currentOnline"
        )
    }
    lastOnline = currentOnline
        if (lastOnline > peakOnline){
            lastOnline = peakOnline
            ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("новый рекорд онлайна! $peakOnline")
        }
}

onSave { context ->
    context.tag.putLong("completed_updates", completedUpdates)
    context.tag.putInt("last_online", lastOnline)
    context.tag.putInt("peakOnline", peakOnline)
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Состояние узла записано в сохранение мира")
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Lesson 04] Серверный узел остановлен")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Homework 04] Всего обновлений onUpdate $completedUpdates")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Homework 04] Обновлений онлайна $onlineUpdates")
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info("[Homework 04] Пик онлайна за сессию $peakOnline")
}