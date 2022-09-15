package com.ithersta.tgbotapi.sample

import com.ithersta.tgbotapi.pagination.PagerState

sealed interface DialogState
object EmptyState : DialogState
object WaitingForName : DialogState
class WaitingForAge(val name: String) : DialogState
class WaitingForConfirmation(val name: String, val age: Int) : DialogState

object MenuStates {
    object Main : DialogState
    object SendInfo : DialogState
    object GetStats : DialogState
    object AddUsers : DialogState
}

object SendStates {
    object ToAll : DialogState
    object ToTrackers : DialogState
    object ChooseTeams : DialogState
}

object GetStatsStates {
    object Teams : DialogState
    object Trackers : DialogState
}

object AddUsersStates {
    object WaitingForDocument : DialogState
    object CuratorDeeplink : DialogState
}

object GetProtocolsState : DialogState

data class Pager(
    val pagerState: PagerState
) : DialogState
