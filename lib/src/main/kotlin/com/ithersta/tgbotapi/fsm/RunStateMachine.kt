package com.ithersta.tgbotapi.fsm

import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.createSubContextAndDoWithUpdatesFilter
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.utils.PreviewFeature

@OptIn(PreviewFeature::class)
suspend fun <BaseState : Any> TelegramBot.runStateMachine(
    repository: StateRepository<BaseState>,
    block: StateMachineScope<BaseState>.() -> Unit
) {
    val stateMachineScope = StateMachineScope<BaseState>(this)
    block(stateMachineScope)
    val handlers = stateMachineScope.handlers.toList()
    buildBehaviourWithLongPolling {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { it.data.fromUserOrNull()?.from?.id }) { update ->
            val userId = update.data.fromUserOrNull()?.from?.id ?: return@subscribeSafelyWithoutExceptionsAsync
            createSubContextAndDoWithUpdatesFilter(stopOnCompletion = false) {
                handlers.first { handler ->
                    handler(update, repository.get(userId)) { repository.set(userId, it) }
                }
            }
        }
    }.join()
}
