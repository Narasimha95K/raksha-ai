package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scam_history")
data class ScamHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // CALL, SMS, WHATSAPP, URL, PAYMENT
    val input: String,
    val riskLevel: String, // LOW, MEDIUM, HIGH
    val confidenceScore: Int, // 0-100
    val category: String,
    val explanation: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scam_reports")
data class ScamReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reportType: String, // NUMBER, WEBSITE, SMS, SCREENSHOT
    val target: String,
    val category: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_blacklist")
data class BlacklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // NUMBER, WEBSITE
    val value: String, // e.g. "+91 90123 45678", "http://update-kyc-bank.in"
    val category: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScamDao {
    // History
    @Query("SELECT * FROM scam_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScamHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ScamHistoryEntity): Long

    @Query("DELETE FROM scam_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM scam_history")
    suspend fun clearHistory()

    // Reports
    @Query("SELECT * FROM scam_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScamReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(item: ScamReportEntity): Long

    @Query("DELETE FROM scam_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    // Blacklist
    @Query("SELECT * FROM local_blacklist ORDER BY timestamp DESC")
    fun getBlacklist(): Flow<List<BlacklistItemEntity>>

    @Query("SELECT * FROM local_blacklist WHERE value = :value LIMIT 1")
    suspend fun checkBlacklist(value: String): BlacklistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistItem(item: BlacklistItemEntity)

    @Query("DELETE FROM local_blacklist WHERE id = :id")
    suspend fun deleteBlacklistById(id: Int)
}

@Database(
    entities = [ScamHistoryEntity::class, ScamReportEntity::class, BlacklistItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RakshaDatabase : RoomDatabase() {
    abstract fun scamDao(): ScamDao

    companion object {
        @Volatile
        private var INSTANCE: RakshaDatabase? = null

        fun getDatabase(context: Context): RakshaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RakshaDatabase::class.java,
                    "raksha_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
