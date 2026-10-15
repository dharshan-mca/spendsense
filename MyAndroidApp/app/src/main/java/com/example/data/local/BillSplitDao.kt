package com.example.data.local

import androidx.room.*
import com.example.data.model.BillSplitEntity
import com.example.data.model.BillSplitWithParticipants
import com.example.data.model.SplitParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillSplitDao {

    @Transaction
    @Query("SELECT * FROM bill_splits ORDER BY dateMillis DESC")
    fun getAllSplitsWithParticipants(): Flow<List<BillSplitWithParticipants>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: BillSplitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<SplitParticipantEntity>)

    @Update
    suspend fun updateSplit(split: BillSplitEntity)

    @Update
    suspend fun updateParticipant(participant: SplitParticipantEntity)

    @Query("UPDATE split_participants SET isPaid = :isPaid WHERE id = :participantId")
    suspend fun updateParticipantPaidStatus(participantId: Long, isPaid: Boolean)

    @Query("UPDATE bill_splits SET isSettled = :isSettled WHERE id = :splitId")
    suspend fun updateSplitSettledStatus(splitId: Long, isSettled: Boolean)

    @Query("DELETE FROM bill_splits WHERE id = :id")
    suspend fun deleteSplitById(id: Long)

    @Query("DELETE FROM split_participants")
    suspend fun deleteAllParticipants()

    @Query("DELETE FROM bill_splits")
    suspend fun deleteAllSplits()
}
