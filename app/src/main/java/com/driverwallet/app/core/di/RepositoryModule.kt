package com.driverwallet.app.core.di

import com.driverwallet.app.feature.debt.data.DebtRepositoryImpl
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.settings.data.SettingsRepositoryImpl
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.shared.data.TransactionRepositoryImpl
import com.driverwallet.app.shared.data.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(
        impl: DebtRepositoryImpl,
    ): DebtRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository
}
