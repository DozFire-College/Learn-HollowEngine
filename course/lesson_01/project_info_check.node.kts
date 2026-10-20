@file:Import("project_info.kts")
import ru.hollowhorizon.hollowengine.HollowEngine


onStart {  
    val NPCs = NPC()
        HollowEngine.LOGGER.info("Название проекта: $Project")
        HollowEngine.LOGGER.info("Название главы: $Glava_Number")
        HollowEngine.LOGGER.info("Онлайн: $online")
        HollowEngine.LOGGER.info("NPC на игрока: $personalNPC")
        HollowEngine.LOGGER.info("НПС на сервере: $NPCs")


    check( online == 20  ){
    "НПС и онлайн не равное кол-во"
    }


}