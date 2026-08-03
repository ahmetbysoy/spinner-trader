package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface BacktestDao {
    @Query("SELECT * FROM backtest_results ORDER BY timestamp DESC")
    fun getAllBacktests(): Flow<List<BacktestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBacktest(result: BacktestEntity)

    @Query("DELETE FROM backtest_results WHERE id = :id")
    suspend fun deleteBacktest(id: Int)

    @Query("DELETE FROM backtest_results")
    suspend fun clearAll()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM whale_alerts ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("DELETE FROM whale_alerts")
    suspend fun clearAlerts()
}

@Database(
    entities = [BacktestEntity::class, AlertEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun backtestDao(): BacktestDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getDatabase(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    "crypto_quant_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
