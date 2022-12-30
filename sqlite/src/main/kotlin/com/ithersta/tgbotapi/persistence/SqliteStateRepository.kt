package com.ithersta.tgbotapi.persistence

import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

@OptIn(ExperimentalSerializationApi::class)
class SqliteStateRepository<S : Any>(
    val historyDepth: Int,
    jdbc: String,
    stateKType: KType
) : StateRepository<UserId, S> {
    val serializer = serializer(stateKType)

    init {
        Database.connect(jdbc, "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(UserStates, SequenceNumbers)
        }
    }

    override fun get(key: UserId): List<S>? = runCatching {
        val serialized = transaction {
            val sequenceNumber = SequenceNumbers
                .select { SequenceNumbers.id eq key.chatId }
                .map { it[SequenceNumbers.sequenceNumber] }
                .first()
            UserStates
                .select {
                    (UserStates.userId eq key.chatId) and
                            (UserStates.sequenceNumber eq sequenceNumber)
                }
                .map { it[UserStates.state] }
                .first()
        }
        ProtoBuf.decodeFromByteArray(serializer, serialized) as List<S>
    }.getOrNull()

    override fun set(key: UserId, stateStack: List<S>) {
        transaction {
            val sequenceNumber = SequenceNumbers
                .select { SequenceNumbers.id eq key.chatId }
                .map { it[SequenceNumbers.sequenceNumber] }
                .firstOrNull()?.plus(1) ?: 0
            UserStates.replace {
                it[UserStates.userId] = key.chatId
                it[UserStates.sequenceNumber] = sequenceNumber
                it[UserStates.state] = ProtoBuf.encodeToByteArray(serializer, stateStack)
            }
            SequenceNumbers.replace {
                it[SequenceNumbers.id] = key.chatId
                it[SequenceNumbers.sequenceNumber] = sequenceNumber
            }
            UserStates.deleteWhere {
                (UserStates.userId eq key.chatId) and
                        (UserStates.sequenceNumber eq (sequenceNumber - historyDepth))
            }
        }
    }

    override fun rollback(key: UserId): List<S>? = runCatching {
        val serialized = transaction {
            val sequenceNumber = SequenceNumbers
                .select { SequenceNumbers.id eq key.chatId }
                .map { it[SequenceNumbers.sequenceNumber] }
                .first() - 1
            UserStates
                .select {
                    (UserStates.userId eq key.chatId) and
                            (UserStates.sequenceNumber eq sequenceNumber)
                }
                .map { it[UserStates.state] }
                .first()
                .also {
                    SequenceNumbers.replace {
                        it[SequenceNumbers.id] = key.chatId
                        it[SequenceNumbers.sequenceNumber] = sequenceNumber
                    }
                }
        }
        ProtoBuf.decodeFromByteArray(serializer, serialized) as List<S>
    }.getOrNull()

    companion object {
        inline fun <reified S : Any> create(
            historyDepth: Int,
            jdbc: String = "jdbc:sqlite:states.db"
        ) = SqliteStateRepository<S>(
            historyDepth = historyDepth,
            jdbc = jdbc,
            stateKType = List::class.createType(listOf(KTypeProjection.invariant(S::class.starProjectedType)))
        )
    }
}
