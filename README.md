# tgbotapi-fsm

Finite state machine DSL for Telegram Bot API.

## Sample code
````kotlin
telegramBot(System.getenv("TOKEN")).runStateMachine<DialogState>(
    repository = InMemoryStateRepositoryImpl(EmptyState)
) {
    state<DialogState> {
        onHelpCommand()
    }
    state<EmptyState> {
        onCommand("start", "начать") {
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
````
