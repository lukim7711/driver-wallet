package com.driverwallet.app.core.di

import com.driverwallet.app.feature.report.data.AndroidCsvFileExporter
import com.driverwallet.app.feature.report.domain.CsvFileExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExporterModule {

    @Binds
    abstract fun bindCsvFileExporter(
        impl: AndroidCsvFileExporter,
    ): CsvFileExporter
}
