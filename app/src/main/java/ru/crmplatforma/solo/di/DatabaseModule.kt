package ru.crmplatforma.solo.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.crmplatforma.solo.data.local.AppDatabase
import ru.crmplatforma.solo.data.local.dao.*
import javax.inject.Singleton

/**
 * Hilt модуль для базы данных.
 *
 * Предоставляет:
 * - AppDatabase (singleton)
 * - Все DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // Для разработки: уничтожить БД при изменении схемы
            // В продакшене заменить на миграции
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao {
        return database.clientDao()
    }

    @Provides
    fun provideServiceDao(database: AppDatabase): ServiceDao {
        return database.serviceDao()
    }

    @Provides
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao {
        return database.appointmentDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }

    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    fun provideICEDao(database: AppDatabase): ICEDao {
        return database.iceDao()
    }
}
