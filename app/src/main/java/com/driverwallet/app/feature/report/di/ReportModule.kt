package com.driverwallet.app.feature.report.di

import com.driverwallet.app.feature.report.data.AndroidFileExporter
import com.driverwallet.app.feature.report.domain.FileExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportModule {

    @Binds
    @Singleton
    abstract fun bindFileExporter(
        impl: AndroidFileExporter,
    ): FileExporter
}
