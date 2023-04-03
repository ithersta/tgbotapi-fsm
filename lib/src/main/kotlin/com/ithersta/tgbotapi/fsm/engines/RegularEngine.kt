package com.ithersta.tgbotapi.fsm.engines

import com.ithersta.tgbotapi.fsm.engines.repository.StateRepository
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.sourceChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sourceUser
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.bot.SetMyCommands
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.PreviewFeature

typealias ExceptionHandler<K> = suspend RequestsExecutor.(K, Throwable) -> Unit

class RegularEngine<BS : Any, BU : Any, K : Any> internal constructor(
    private val stateMachine: StateMachine<BS, BU, K>,
    private val getKey: (Update) -> K?,
    private val getUser: (K) -> BU,
    private val getScope: (K) -> BotCommandScope,
    private val stateRepository: StateRepository<K, BS>,
    private val exceptionHandler: ExceptionHandler<K>
) {
    fun BehaviourContext.collectUpdates() {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { getKey(it) }) { update ->
            val key = getKey(update) ?: return@subscribeSafelyWithoutExceptionsAsync
            runCatching {
                with(stateMachine) {
                    dispatch(
                        update = update,
                        dataAccessor = DataAccessor(
                            key = key,
                            getUser = { getUser(key) },
                            getStateStackData = { stateRepository.get(key) },
                            setStateStackData = { stateRepository.set(key, it) },
                            rollbackStateStackData = { stateRepository.rollback(key) },
                            refreshCommands = { refreshCommands(it, getScope(key)) }
                        )
                    )
                }
            }.onFailure {
                exceptionHandler(bot, key, it)
            }
        }
    }

    private suspend fun RequestsExecutor.refreshCommands(commands: List<BotCommand>, scope: BotCommandScope) {
        @Suppress("DeferredResultUnused")
        executeAsync(SetMyCommands(commands, scope))
    }
}

fun <BS : Any, BU : Any, K : Any> StateMachine<BS, BU, K>.regularEngine(
    getKey: (Update) -> K?,
    getUser: (K) -> BU,
    getScope: (K) -> BotCommandScope,
    stateRepository: StateRepository<K, BS>,
    exceptionHandler: ExceptionHandler<K>
) = RegularEngine(
    stateMachine = this,
    getKey = getKey,
    getUser = getUser,
    getScope = getScope,
    stateRepository = stateRepository,
    exceptionHandler = exceptionHandler
)

@OptIn(PreviewFeature::class)
fun <BS : Any, BU : Any> StateMachine<BS, BU, UserId>.regularEngine(
    getUser: (UserId) -> BU,
    stateRepository: StateRepository<UserId, BS>,
    exceptionHandler: ExceptionHandler<UserId>
) = regularEngine(
    getKey = { it.sourceUser()?.id ?: it.sourceChat()?.id?.chatId?.let { ChatId(it) } },
    getUser = getUser,
    getScope = { BotCommandScope.Chat(it) },
    stateRepository = stateRepository,
    exceptionHandler = exceptionHandler
)
