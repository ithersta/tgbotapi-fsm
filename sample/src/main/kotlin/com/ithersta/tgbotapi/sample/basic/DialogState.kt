package com.ithersta.tgbotapi.sample.basic

import com.ithersta.tgbotapi.pagination.PagerState
import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogState

@Serializable
object EmptyState : DialogState

@Serializable
object WaitingForName : DialogState

@Serializable
class WaitingForAge(val name: String) : DialogState

@Serializable
class WaitingForConfirmation(val name: String, val age: Int) : DialogState

object MenuStates {
    @Serializable
    object Main : DialogState

    @Serializable
    object SendInfo : DialogState

    @Serializable
    object GetStats : DialogState

    @Serializable
    object AddUsers : DialogState
}

object SendStates {
    @Serializable
    object ToAll : DialogState

    @Serializable
    object ToTrackers : DialogState

    @Serializable
    object ChooseTeams : DialogState
}

object GetStatsStates {
    @Serializable
    object Teams : DialogState

    @Serializable
    object Trackers : DialogState
}

object AddUsersStates {
    @Serializable
    object WaitingForDocument : DialogState

    @Serializable
    object CuratorDeeplink : DialogState
}

@Serializable
object GetProtocolsState : DialogState

@Serializable
data class Pager(
    val pagerState: PagerState
) : DialogState
