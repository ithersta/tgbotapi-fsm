---
title: "@StateMachine"
description: ""
lead: ""
draft: false
images: []
weight: 110
toc: false
---

Аннотацией `@StateMachine` помечается свойство
типа `StateMachine`.

```kotlin
@StateMachine
val stateMachine = stateMachine<DialogState, Unit, UserId>(
    ...
) { ... }
```

Это включает кодогенерацию некоторых полезных вещей для этого конечного автомата.

{{< alert icon="TL;DR"  >}}
1. Можно не использовать sealed иерархию для состояний и запросов.
2. Можно пользоваться типобезопасными Data Callback Queries.
3. Можно не указывать базовые типы. Например, можно писать

```kotlin
RoleFilterBuilder<User.Admin>
```
вместо
```kotlin
RoleFilterBuilder<DialogState, User, User.Admin, UserId>
```
{{< /alert >}}

# SerializersModule

Кодогенератор ищет всех наследников указанного базового состояния,
а также всех наследников базового типа запроса и автоматически регистрирует их
для полиморфной сериализации. Таким образом, это позволяет обойтись
без sealed классов, которые накладывают ограничение на пакет, где должны
находиться их наследники.

Пример сгенерированного кода:

```kotlin
val stateMachineSerializersModule: SerializersModule = SerializersModule {
  polymorphic(DialogState::class) {
    subclass(EmptyState::class)
    subclass(MultipleChoiceState::class)
  }
  polymorphic(Query::class) {
    subclass(SelectQuery::class)
    subclass(UnselectQuery::class)
  }
}
```

# ProtoBuf

Со сгенерированным `SerializersModule` создаётся сериализатор
`ProtoBuf`, который в
дальнейшем используется в реализации репозитория состояний и
для типобезопасных Data Callback Queries.

```kotlin
val protoBuf: ProtoBuf = ProtoBuf { serializersModule = stateMachineSerializersModule }
```

# Repository

Генерируется функция-фабрика для репозитория с подставленными
`ProtoBuf` и базовыми типами.

```kotlin
public fun sqliteStateRepository(historyDepth: Int): SqliteStateRepository<DialogState> =
    SqliteStateRepository.create(historyDepth = historyDepth, protoBuf = protoBuf)
```

# Type Aliases

Генерируются некоторые type aliases с подставленными базовыми типами.

```kotlin
public typealias StateMachineBuilder =
    StateMachineBuilder<DialogState, Unit, UserId>

public typealias RoleFilterBuilder<User> =
    RoleFilterBuilder<DialogState, Unit, User, UserId>

public typealias StateFilterBuilder<State, User> =
    StateFilterBuilder<DialogState, Unit, State, User, UserId>
```

# Menu

Генерируется функция `menu` с подставленными базовыми типами

```kotlin
public fun <U : Unit> menu(
  messageText: String,
  state: DialogState,
  block: MenuBuilder<DialogState, Unit, U>.() -> Unit,
): Menu<DialogState, Unit, U> = menu(messageText = messageText, state = state, block = block)
```

# CallbackQueryTriggers

Генерируются функции `onDataCallbackQuery` и `dataButton`, которые
внутри себя используют сгенерированный `ProtoBuf`.
