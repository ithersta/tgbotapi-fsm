package com.ithersta.tgbotapi.persistence

import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

@OptIn(ExperimentalSerializationApi::class)
class SqliteStateRepository<S : Any>(
    jdbc: String,
    stateKType: KType
) : StateRepository<UserId, S> {
    val serializer = serializer(stateKType)

    init {
        Database.connect(jdbc, "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(UserStates)
        }
    }

    override fun get(key: UserId): List<S>? = runCatching {
        val serialized = transaction {
            UserStates.select { UserStates.id eq key.chatId }.map { it[UserStates.state] }
        }.first()
        ProtoBuf.decodeFromByteArray(serializer, serialized) as List<S>
    }.getOrNull()

    override fun set(key: UserId, stateStack: List<S>) {
        transaction {
            UserStates.replace {
                it[UserStates.id] = key.chatId
                it[UserStates.state] = ProtoBuf.encodeToByteArray(serializer, stateStack)
            }
        }
    }

    companion object {
        inline fun <reified S : Any> create(
            jdbc: String = "jdbc:sqlite:state.db"
        ) = SqliteStateRepository<S>(
            jdbc = jdbc,
            stateKType = List::class.createType(listOf(KTypeProjection.invariant(S::class.starProjectedType)))
        )
    }
}
