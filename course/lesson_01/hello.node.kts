
@file:Import("course_info.kts")

import ru.hollowhorizon.hollowengine.HollowEngine

onStart {
    val testValue = courseCheckValue()

    HollowEngine.LOGGER.info("=== ENCORE COURSE CHECK ===")
    HollowEngine.LOGGER.info("Движок: $COURSE_NAME")
    HollowEngine.LOGGER.info("Урок: $LESSON_NUMBER")
    HollowEngine.LOGGER.info("Проверка Kotlin: 20 + 22 = $testValue")

    check(testValue == 42) {
        "Проверка компилятора не пройдена"
    }

    HollowEngine.LOGGER.info("Урок 1: окружение работает")
}