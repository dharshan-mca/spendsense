package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "bill_splits")
data class BillSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val paidBy: String = "You",
    val dateMillis: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false,
    val note: String = ""
)

@Entity(
    tableName = "split_participants",
    foreignKeys = [
        ForeignKey(
            entity = BillSplitEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("splitId")]
)
data class SplitParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val splitId: Long,
    val name: String,
    val shareAmount: Double,
    val isPaid: Boolean = false
)

data class BillSplitWithParticipants(
    @Embedded
    val split: BillSplitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "splitId"
    )
    val participants: List<SplitParticipantEntity>
)
