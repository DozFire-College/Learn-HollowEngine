@file:Import("build_info.kts")

var targetSeconds = HOMEWORK_TARGET_SECONDS


onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[$HOMEWORK_PROJECT NODE] сосание запущено: $path"
    )
}
onUpdate(every = 20.ticks) {
    targetSeconds++

    if (targetSeconds <= 3) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[$HOMEWORK_PROJECT NODE] уютное сосание проходит $targetSeconds сек."
        )
    }
}
onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[$HOMEWORK_PROJECT NODE] node остановлен: $path"
    )
}