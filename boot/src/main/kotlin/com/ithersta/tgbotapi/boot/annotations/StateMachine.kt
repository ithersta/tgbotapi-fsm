package com.ithersta.tgbotapi.boot.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StateMachine(val baseQueryKClass: KClass<out Any> = Any::class)
