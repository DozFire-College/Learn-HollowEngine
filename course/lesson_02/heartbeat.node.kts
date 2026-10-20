@file:Import("shared_values.kts")

var secondsAlive: Int = 0

onStart {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[$LESSON_02_LABEL NODE] сосание запущено: $path"
    )
}

onUpdate(every = 20.ticks) {
    secondsAlive++

    if (secondsAlive <= 3) {
        ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
            "[$LESSON_02_LABEL NODE] уютное сосание проходит $secondsAlive сек."
        )
    }
}

onStop {
    ru.hollowhorizon.hollowengine.HollowEngine.LOGGER.info(
        "[$LESSON_02_LABEL NODE] node остановлен: $path"
    )
}