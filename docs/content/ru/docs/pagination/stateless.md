---
title: "inlineKeyboardPager"
description: ""
lead: ""
draft: false
images: []
weight: 2020
toc: false
---

Проще всего посмотреть на полном примере:

```kotlin
val samplePager = inlineKeyboardPager("samplePager") { <-- уникальный id
    val items = repository.getPaginated(…, offset, limit)
    val count = repository.count(…)
    inlineKeyboard {
        items.forEach {
            row {
                dataButton(it.name, …)
            }
        }
        navigationRow(count)
    }
}

state<...> {
    onTransition {
        sendTextMessage(
            chatId = it, 
            text = "Странички", 
            replyMarkup = with(samplePager) { firstPage }
        )
    }
}
```

Объявляется внутри `role<...>` на одном уровне с `state<...>`.

Лямбда внутри `inlineKeyboardPager` получает `offset` (отступ) и `limit` (количество предметов на странице) и должна возвращать `inlineKeyboard`
с содержанием текущей страницы.

`navigationRow` создаёт строку с кнопками назад, вперёд и счётчиком страниц.

Вопросы?
 
