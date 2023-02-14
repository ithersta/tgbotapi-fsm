package com.ithersta.tgbotapi.sample.menu

import com.ithersta.tgbotapi.sample.menu.generated.menu

val sampleMenu = menu<Unit>(messageText = "Главное меню", state = MenuStates.Main) {
    submenu(text = "Опция один", messageText = "Опция один", state = MenuStates.OptionOne) {
        button(text = "Закрыть меню", targetState = DialogState.Empty)
        backButton(text = "Назад")
    }
}
