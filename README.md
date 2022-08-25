# tgbotapi-fsm

Finite state machine DSL for Telegram Bot API.

## Sample code
````kotlin
stateMachine<DialogState, User>(
    getUser = { EmptyUser },
    stateRepository = InMemoryStateRepositoryImpl(EmptyState),
) {
    onException { userId, throwable ->
        sendTextMessage(userId, throwable.toString())
    }
    includeHelp()
    role<Admin> {
        anyState {
            onCommand("wow", "admin command") {
                sendTextMessage(it.chat, "You're an admin!")
            }
        }
    }
    role<EmptyUser> {
        state<EmptyState> {
            onTransition { sendTextMessage(it, "Empty state. You're $user") }
            onCommand("start", "register") { setState(WaitingForName) }
        }
        state<WaitingForName> {
            onTransition { sendTextMessage(it, "What's your name?") }
            onText { setState(WaitingForAge(it.content.text)) }
        }
        state<WaitingForAge> {
            onTransition { sendTextMessage(it, "What's your age?") }
            onText { message ->
                val age = message.content.text.toIntOrNull()?.takeIf { it > 0 } ?: run {
                    sendTextMessage(message.chat, "Invalid age")
                    return@onText
                }
                setState(WaitingForConfirmation(state.name, age))
            }
        }
        state<WaitingForConfirmation> {
            onTransition {
                sendTextMessage(it, "Confirm: ${state.name} ${state.age}. Yes/No")
            }
            onText("Yes") {
                sendTextMessage(it.chat, "Good!")
                setState(EmptyState)
            }
            onText("No") {
                sendTextMessage(it.chat, "Bad!")
                setState(EmptyState)
            }
            onText {
                sendTextMessage(it.chat, "Yes/No")
            }
        }
    }
}
````
