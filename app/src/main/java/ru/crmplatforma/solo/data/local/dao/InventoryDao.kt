package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.InventoryItemEntity
import ru.crmplatforma.solo.data.local.entity.InventoryOperationEntity

@Dao
interface InventoryDao {

    // === Items ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun getItemByIdFlow(id: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItemsFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItemsFlow(query: String): Flow<List<InventoryItemEntity>>

    // Low stock (quantity <= minQuantity и minQuantity > 0)
    @Query("SELECT * FROM inventory_items WHERE minQuantity > 0 AND quantity <= minQuantity ORDER BY name ASC")
    fun getLowStockFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE minQuantity > 0 AND quantity <= minQuantity")
    fun getLowStockCountFlow(): Flow<Int>

    // === Operations ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: InventoryOperationEntity)

    @Query("SELECT * FROM inventory_operations WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun getOperationsByItemFlow(itemId: String): Flow<List<InventoryOperationEntity>>

    @Query("SELECT * FROM inventory_operations WHERE itemId = :itemId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentOperations(itemId: String, limit: Int = 10): List<InventoryOperationEntity>

    // === Комбинированные операции ===

    // Приход
    @Transaction
    suspend fun addStock(itemId: String, quantity: Double, note: String?, operation: InventoryOperationEntity) {
        insertOperation(operation)
        increaseQuantity(itemId, quantity)
    }

    // Расход
    @Transaction
    suspend fun removeStock(itemId: String, quantity: Double, note: String?, operation: InventoryOperationEntity) {
        insertOperation(operation)
        decreaseQuantity(itemId, quantity)
    }

    @Query("UPDATE inventory_items SET quantity = quantity + :delta, synced = 0 WHERE id = :id")
    suspend fun increaseQuantity(id: String, delta: Double)

    @Query("UPDATE inventory_items SET quantity = quantity - :delta, synced = 0 WHERE id = :id")
    suspend fun decreaseQuantity(id: String, delta: Double)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, synced = 0 WHERE id = :id")
    suspend fun setQuantity(id: String, newQuantity: Double)

    // === Синхронизация ===

    @Query("SELECT * FROM inventory_items WHERE synced = 0")
    suspend fun getUnsyncedItems(): List<InventoryItemEntity>

    @Query("UPDATE inventory_items SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markItemSynced(id: String, serverId: String)

    @Query("SELECT * FROM inventory_operations WHERE synced = 0")
    suspend fun getUnsyncedOperations(): List<InventoryOperationEntity>

    @Query("UPDATE inventory_operations SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markOperationSynced(id: String, serverId: String)
}
