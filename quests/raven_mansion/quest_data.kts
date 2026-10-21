import ru.hollowhorizon.hollowengine.common.data.dataKey

object RavenQuestData {
    val GuideMet =
        dataKey<Boolean>("encore:guide_met") { false }

    val GuideVisits =
        dataKey<Int>("encore:guide_visits") { 0 }

    val GuideReputation =
        dataKey<Int>("encore:guide_reputation") { 0 }

    val GuideTotalConversations =
        dataKey<Int>("encore:guide_total_conversations") { 0 }

    val GuideTalked =
        dataKey<Boolean>(
            "encore:raven_mansion/guide_talked"
        ) { false }

    val MansionEntered =
        dataKey<Boolean>(
            "encore:raven_mansion/mansion_entered"
        ) { false }

    val GateInspected =
        dataKey<Boolean>(
            "encore:raven_mansion/gate_inspected"
        ) { false }
    val EntranceRewardClaimed =
    dataKey<Boolean>(
        "encore:raven_mansion/entrance_reward_claimed"
    ) { false }

    val IntroCompleted =
        dataKey<Boolean>(
            "encore:raven_mansion/intro_completed"
        ) { false }
    val ApproachSceneStep =
    dataKey<Int>(
        "encore:raven_mansion/state/approach_scene_step"
    ) { 0 }

    val ApproachSceneAttempt =
        dataKey<Int>(
            "encore:raven_mansion/state/approach_scene_attempt"
        ) { 1 }
    
    val ApproachConfirmed =
        dataKey<Boolean>(
            "encore:raven_mansion/state/approach_confirmed"
        ) { false }
}