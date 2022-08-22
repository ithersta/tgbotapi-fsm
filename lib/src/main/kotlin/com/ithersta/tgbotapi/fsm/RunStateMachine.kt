package com.ithersta.tgbotapi.fsm

import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.micro_utils.coroutines.ExceptionHandler
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.createSubContextAndDoWithUpdatesFilter
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.bot.SetMyCommands
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.utils.PreviewFeature

@OptIn(PreviewFeature::class)
suspend fun <BaseState : Any> TelegramBot.runStateMachine(
    repository: StateRepository<BaseState>,
    defaultExceptionHandler: ExceptionHandler<Unit>? = null,
    block: StateMachineScope<BaseState>.() -> Unit
) {
    val stateMachineScope = StateMachineScope<BaseState>(this)
    block(stateMachineScope)
    val handlers = stateMachineScope.handlers.toList()
    buildBehaviourWithLongPolling(
        defaultExceptionsHandler = defaultExceptionHandler
    ) {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { it.data.fromUserOrNull()?.from?.id }) { update ->
            val userId = update.data.fromUserOrNull()?.from?.id ?: return@subscribeSafelyWithoutExceptionsAsync
            createSubContextAndDoWithUpdatesFilter(stopOnCompletion = false) {
                handlers.firstOrNull { handler ->
                    handler(update, repository.get(userId)) {
                        repository.set(userId, it)
                        onStateChanged(userId, it, stateMachineScope)
                    }
                }
            }
        }
    }.join()
}

private suspend fun <BaseState : Any> RequestsExecutor.onStateChanged(
    userId: UserId,
    state: BaseState,
    scope: StateMachineScope<BaseState>
) {
    @Suppress("DeferredResultUnused")
    executeAsync(
        SetMyCommands(
            scope.commands.filterAvailable(state).map { BotCommand(it.command, it.description) },
            BotCommandScope.Chat(userId)
        )
    )
}
