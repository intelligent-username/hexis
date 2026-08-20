package com.loc.hexis.di

import com.loc.hexis.backup.BackupModule
import com.loc.hexis.database.DatabaseModule
import com.loc.hexis.platform.PlatformModule
import com.loc.hexis.shared.ui.di.UIModules
import com.loc.hexis.widgets.WidgetsModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        DatabaseModule::class,
        PlatformModule::class,
        BackupModule::class,
        WidgetsModule::class,
        UIModules::class,
    ]
)
class HexisModules