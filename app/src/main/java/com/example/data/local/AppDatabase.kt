package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BrokerAccountEntity
import com.example.data.model.PaperTradeEntity
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.StrategyEntity
import com.example.data.model.WatchlistItemEntity

@Database(
    entities = [
        StrategyEntity::class,
        PaperTradeEntity::class,
        PortfolioPositionEntity::class,
        BrokerAccountEntity::class,
        WatchlistItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun strategyDao(): StrategyDao
    abstract fun paperTradeDao(): PaperTradeDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun brokerDao(): BrokerDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "algo_trading_hub.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
