package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BrokerAccountEntity
import com.example.data.model.PaperTradeEntity
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.StrategyEntity
import com.example.data.model.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    @Query("SELECT * FROM strategies ORDER BY createdAt DESC")
    fun getAllStrategies(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE id = :id")
    suspend fun getStrategyById(id: String): StrategyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: StrategyEntity)

    @Query("DELETE FROM strategies WHERE id = :id")
    suspend fun deleteStrategyById(id: String)
}

@Dao
interface PaperTradeDao {
    @Query("SELECT * FROM paper_trades ORDER BY openedAt DESC")
    fun getAllTrades(): Flow<List<PaperTradeEntity>>

    @Query("SELECT * FROM paper_trades WHERE status = 'OPEN' ORDER BY openedAt DESC")
    fun getOpenTrades(): Flow<List<PaperTradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: PaperTradeEntity)

    @Update
    suspend fun updateTrade(trade: PaperTradeEntity)

    @Query("DELETE FROM paper_trades WHERE id = :id")
    suspend fun deleteTradeById(id: String)

    @Query("DELETE FROM paper_trades")
    suspend fun clearAllTrades()
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_positions ORDER BY currentPrice * quantity DESC")
    fun getAllPositions(): Flow<List<PortfolioPositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPositions(positions: List<PortfolioPositionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PortfolioPositionEntity)

    @Query("DELETE FROM portfolio_positions WHERE symbol = :symbol")
    suspend fun deletePosition(symbol: String)
}

@Dao
interface BrokerDao {
    @Query("SELECT * FROM broker_accounts ORDER BY lastSyncTime DESC")
    fun getAllBrokers(): Flow<List<BrokerAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroker(broker: BrokerAccountEntity)

    @Query("UPDATE broker_accounts SET isConnected = :connected, pingLatencyMs = :latency WHERE id = :id")
    suspend fun updateConnectionStatus(id: String, connected: Boolean, latency: Long)

    @Query("DELETE FROM broker_accounts WHERE id = :id")
    suspend fun deleteBroker(id: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE symbol = :symbol)")
    fun isInWatchlist(symbol: String): Flow<Boolean>
}
