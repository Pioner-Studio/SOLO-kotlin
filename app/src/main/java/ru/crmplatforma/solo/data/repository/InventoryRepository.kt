package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.InventoryDao
import ru.crmplatforma.solo.data.local.entity.InventoryItemEntity
import ru.crmplatforma.solo.data.local.entity.InventoryOperationEntity
import ru.crmplatforma.solo.data.local.entity.InventoryOperationType
import ru.crmplatforma.solo.data.local.entity.InventoryUnit
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InventoryRepository — работа со складом.
 */
@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    // === Queries: Items ===

    fun getAllItems(): Flow<List<InventoryItemEntity>> = inventoryDao.getAllItemsFlow()

    fun searchItems(query: String): Flow<List<InventoryItemEntity>> = inventoryDao.searchItemsFlow(query)

    fun getLowStockItems(): Flow<List<InventoryItemEntity>> = inventoryDao.getLowStockFlow()

    fun getLowStockCount(): Flow<Int> = inventoryDao.getLowStockCountFlow()

    fun getItemById(id: String): Flow<InventoryItemEntity?> = inventoryDao.getItemByIdFlow(id)

    // === Queries: Operations ===

    fun getOperationsByItem(itemId: String): Flow<List<InventoryOperationEntity>> {
        return inventoryDao.getOperationsByItemFlow(itemId)
    }

    // === Commands: Items ===

    suspend fun createItem(
        name: String,
        unit: InventoryUnit = InventoryUnit.PIECE,
        quantity: Double = 0.0,
        minQuantity: Double = 0.0,
        description: String? = null,
        category: String? = null
    ): InventoryItemEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val item = InventoryItemEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            unit = unit,
            quantity = quantity,
            minQuantity = minQuantity,
            description = description,
            category = category,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        inventoryDao.insertItem(item)
        return item
    }

    suspend fun updateItem(item: InventoryItemEntity) {
        val updated = item.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        inventoryDao.updateItem(updated)
    }

    suspend fun deleteItem(item: InventoryItemEntity) {
        inventoryDao.deleteItem(item)
    }

    // === Commands: Operations ===

    suspend fun addStock(itemId: String, quantity: Double, note: String? = null) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val operation = InventoryOperationEntity(
            id = UUID.randomUUID().toString(),
            itemId = itemId,
            type = InventoryOperationType.IN,
            quantity = quantity,
            note = note,
            synced = false,
            createdAt = now
        )
        inventoryDao.addStock(itemId, quantity, note, operation)
    }

    suspend fun removeStock(itemId: String, quantity: Double, note: String? = null) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val operation = InventoryOperationEntity(
            id = UUID.randomUUID().toString(),
            itemId = itemId,
            type = InventoryOperationType.OUT,
            quantity = quantity,
            note = note,
            synced = false,
            createdAt = now
        )
        inventoryDao.removeStock(itemId, quantity, note, operation)
    }

    suspend fun adjustStock(itemId: String, newQuantity: Double, note: String? = null) {
        val item = inventoryDao.getItemById(itemId) ?: return
        val delta = newQuantity - item.quantity

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val operation = InventoryOperationEntity(
            id = UUID.randomUUID().toString(),
            itemId = itemId,
            type = InventoryOperationType.ADJUST,
            quantity = delta,
            note = note ?: "Корректировка: ${item.quantity} → $newQuantity",
            synced = false,
            createdAt = now
        )
        inventoryDao.insertOperation(operation)
        inventoryDao.setQuantity(itemId, newQuantity)
    }

    // === Sync ===

    suspend fun getUnsyncedItems(): List<InventoryItemEntity> = inventoryDao.getUnsyncedItems()

    suspend fun markItemSynced(id: String, serverId: String) {
        inventoryDao.markItemSynced(id, serverId)
    }

    suspend fun getUnsyncedOperations(): List<InventoryOperationEntity> = inventoryDao.getUnsyncedOperations()

    suspend fun markOperationSynced(id: String, serverId: String) {
        inventoryDao.markOperationSynced(id, serverId)
    }
}
