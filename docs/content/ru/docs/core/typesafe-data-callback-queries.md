---
title: "Типобезопасные Data Callback Queries"
description: ""
lead: ""
date: 2022-09-20T16:17:33+03:00
lastmod: 2022-09-20T16:17:33+03:00
draft: false
images: []
weight: 100
toc: false
---

# Как было раньше

Data Callback Queries можно было обрабатывать только с помощью обработчика, принимающего
Regex:

```kotlin
onDataCallbackQuery(Regex("do_something \\d+ \\d+ .+")) {
    val tokens = it.data.split(" ")
    val personId = Person.Id(tokens[1].toLong())
    val someOtherId = OtherThing.Id(tokens[2].toLong())
    val word = tokens[3]
    doSomething(personId, someOtherId, word)
}
```

В другом месте соответствующая кнопка задавалась так:

```kotlin
dataButton("Сделать что-то невероятное", "do_something 2 3 word")
```

##### Почему это плохо

1. **Это небезопасно**. Можно, например, поменять формат запроса в одном месте
и забыть это сделать в остальных местах.
1. **Это муторно**. Нужно вручную парсить аргументы запроса. Часто в простых
обработчиках это самая длинная часть. Лучше бы обойтись без неё.

# Решение

Переложим всю эту работу на `kotlinx.serialization`.

Где-то должен быть объявлен базовый класс (или интерфейс) для всех запросов.
Он обязательно должен быть `@Serializable` и `sealed`, чтобы поддерживать
полиморфную сериализацию.

```kotlin
@Serializable
sealed interface Query
```

Сделаем класс для нашего запроса:

```kotlin
@Serializable
@SerialName("do")
class DoSomethingQuery(
    val personId: Person.Id,
    val someOtherId: OtherThing.Id,
    val text: String
) : Query
```

{{< alert icon="" >}}
**Важно!** Нужно указывать короткие (но уникальные) @SerialName для всех классов-запросов.
{{< details "Почему" >}}
Телеграм даёт максимум 64 байта на запрос.
Если не указать SerialName, то оно просто не влезет в эти 64 байта.
{{< details "Почему" >}}
В полиморфной сериализации имя класса тоже сериализуется, чтобы было понятно, какой
тип запроса мы получили. Так вот, если не указать SerialName, по умолчанию
возьмётся имя класса вместе с пакетом, в котором он находится.
{{< /details >}}
Кстати, для полей SerialName указывать не надо.
{{< details "Почему" >}}
Запросы сериализуются с помощью ProtoBuf.
Там для полей используются просто индексы, их имена не кодируются, в отличие от JSON.
{{< details "Почему" >}}
чё почему
{{< /details >}}
{{< /details >}}
{{< /details >}}
{{< /alert >}}

Теперь мы можем обрабатывать этот запрос так:

```kotlin
onDataCallbackQuery(DoSomethingQuery::class) { (data, query) ->
    doSomething(data.personId, data.someOtherId, data.word)
}
```

Это всё!

А так задаётся кнопка с запросом-классом:

```kotlin
dataButton("Сделать что-то невероятное", DoSomethingQuery(...))
```
