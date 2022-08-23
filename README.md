# tgbotapi-fsm

Finite state machine DSL for Telegram Bot API.

## Sample code
````kotlin
val stateMachine = stateMachine<Role, DialogState>(
    getRole = { null },
    stateRepository = InMemoryStateRepositoryImpl(EmptyState),
) {
    includeHelp()
    role(Admin) {
        anyState {
            onCommand("wow", "admin command") {
                sendTextMessage(it.chat, "You're an admin!")
            }
        }
    }
    withoutRole {
        state<EmptyState> {
            onCommand("start", "register") {
                setState(WaitingForName)
                sendTextMessage(it.chat, "What's your name?")
            }
        }
        state<WaitingForName> {
            onText {
                val name = it.content.text
                setState(WaitingForAge(name))
                sendTextMessage(it.chat, "What's your age?")
            }
        }
        state<WaitingForAge> {
            onText { message ->
                val age = message.content.text.toIntOrNull()?.takeIf { it > 0 } ?: run {
                    sendTextMessage(message.chat, "Invalid age")
                    return@onText
                }
                setState(WaitingForConfirmation(state.name, age).also {
                    sendTextMessage(message.chat, "Confirm: ${it.name} ${it.age}. Yes/No")
                })
            }
        }
        state<WaitingForConfirmation> {
            onText("Yes") {
                setState(EmptyState)
                sendTextMessage(it.chat, "Good!")
            }
            onText("No") {
                setState(EmptyState)
                sendTextMessage(it.chat, "Bad!")
            }
            onText {
                sendTextMessage(it.chat, "Yes/No")
            }
        }
    }
}
````
