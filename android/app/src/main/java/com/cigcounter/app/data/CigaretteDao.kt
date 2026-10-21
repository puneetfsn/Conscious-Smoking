package com.cigcounter.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CigaretteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CigaretteLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<CigaretteLog>): List<Long>

    @Query("SELECT * FROM cigarette_logs WHERE isSkipped = 0 ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    suspend fun getLatestSmoked(): CigaretteLog?

    @Query("SELECT * FROM cigarette_logs WHERE isSkipped = 0 ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    fun getLatestSmokedFlow(): Flow<CigaretteLog?>

    @Query("DELETE FROM cigarette_logs WHERE id = (SELECT id FROM cigarette_logs ORDER BY timestampMillis DESC, id DESC LIMIT 1)")
    suspend fun deleteLatest(): Int

    @Query("DELETE FROM cigarette_logs")
    suspend fun deleteAll(): Int

    // Counts for consumed cigarettes
    @Query("SELECT COUNT(*) FROM cigarette_logs WHERE timestampMillis >= :fromMillis AND isSkipped = 0")
    fun countSmokedFromFlow(fromMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cigarette_logs WHERE timestampMillis >= :fromMillis AND isSkipped = 0")
    suspend fun countSmokedFrom(fromMillis: Long): Int

    @Query("SELECT COUNT(*) FROM cigarette_logs WHERE timestampMillis >= :fromMillis AND timestampMillis < :toMillis AND isSkipped = 0")
    suspend fun countSmokedBetween(fromMillis: Long, toMillis: Long): Int

    // Ghost Counter: count resisted cravings
    @Query("SELECT COUNT(*) FROM cigarette_logs WHERE timestampMillis >= :fromMillis AND isSkipped = 1")
    fun countSkippedFromFlow(fromMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cigarette_logs WHERE timestampMillis >= :fromMillis AND isSkipped = 1")
    suspend fun countSkippedFrom(fromMillis: Long): Int

    // Raw log streams
    @Query("SELECT * FROM cigarette_logs WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis ASC")
    fun getLogsFromFlow(fromMillis: Long): Flow<List<CigaretteLog>>

    @Query("SELECT * FROM cigarette_logs WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis ASC")
    suspend fun getLogsFrom(fromMillis: Long): List<CigaretteLog>

    @Query("SELECT * FROM cigarette_logs ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<CigaretteLog>>

    // Full export for local JSON backup
    @Query("SELECT * FROM cigarette_logs ORDER BY timestampMillis ASC")
    suspend fun getAllLogs(): List<CigaretteLog>
}
