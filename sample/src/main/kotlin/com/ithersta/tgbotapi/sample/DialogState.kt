package com.ithersta.tgbotapi.sample

sealed interface DialogState
object EmptyState : DialogState
object WaitingForName : DialogState
class WaitingForAge(val name: String) : DialogState
class WaitingForConfirmation(val name: String, val age: Int) : DialogState
