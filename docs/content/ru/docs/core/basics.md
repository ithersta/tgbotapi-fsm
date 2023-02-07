---
title: "Минимальный пример"
description: ""
lead: ""
draft: false
images: []
weight: 010
toc: false
---

Рассмотрим минимальный пример конечного автомата -- сделаем счётчик.

```kotlin
val stateMachine = stateMachine<DialogState, Unit>(
  getUser = { },
  stateRepository = SqliteStateRepository.create(historyDepth = 3),
  initialState = EmptyState,
  includeHelp = true
) {
...
}
```

Идём построчно.

### Пользователь

Если в боте предусмотрены разные роли, то нужно создать базовый класс (или интерфейс)
пользователя с нужными подтипами (= ролями). Например:

```kotlin
sealed interface User {
  object Admin : User
  object Regular : User
}
```

Аргумент getUser принимает функцию `(UserId) -> User`, то есть функцию, которая
по id пользователя Telegram возвращает объект типа `User`. Впоследствии
это используется для определения роли пользователя.

Для минимального примера нам роли не нужны, поэтому зададим `getUser` так:

```kotlin
getUser = { }
```

### Состояния

Нужно создать базовый класс (или интерфейс) состояния
и все конкретные типы состояния наследовать от него.
Обычно в боте есть пустое состояние. Также нам понадобится
состояние счётчика, которое будет хранить число.

```kotlin
interface DialogState
object EmptyState : DialogState
class CounterState(val number: Int = 0) : DialogState
```

Для доступа к состояниям пользователей конечный автомат использует `StateRepository`.
Существуют две готовые реализации:

##### 1. Хранящий состояния в памяти

```kotlin
stateRepository = InMemoryStateRepositoryImpl(historyDepth = 3)
```

Удобно для экспериментов.

##### 2. Хранящий состояния в базе данных

```kotlin
stateRepository = SqliteStateRepository.create(historyDepth = 3)
```

Состояния в базе данных переживают перезагрузки и обновления бота.

`SqliteStateRepository` требует, чтобы состояния были полиморфно сериализуемыми, поэтому
нам придётся переписать нашу иерархию состояний следующим образом:

```kotlin
@Serializable
sealed interface DialogState

@Serializable
object EmptyState : DialogState

@Serializable
class CounterState(val number: Int = 0) : DialogState
```

Обе реализации поддерживают откат состояния. `historyDepth` -- количество
хранимых последних состояний, от него зависит максимальное количество откатов.

### Начальное состояние

```kotlin
initialState = EmptyState
```

Аргумент `initialState` -- какое состояние задаётся сначала всем пользователям.

### Генерация /help

```kotlin
includeHelp = true
```

Если `includeHelp = true`, то команда `/help` будет сгенерирована автоматически для всех ролей
на основе объявленных `onCommand`.

[Полный код](https://github.com/ithersta/tgbotapi-fsm/tree/main/sample/src/main/kotlin/com/ithersta/tgbotapi/sample/helloworld)
