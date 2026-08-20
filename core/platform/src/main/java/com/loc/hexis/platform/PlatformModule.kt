package com.loc.hexis.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.loc.hexis.core.data.datastore.DatastoreFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.loc.hexis.core.data")
class PlatformModule {
    @Single
    fun getDatastore(factory: DatastoreFactory): DataStore<Preferences> =
        factory.getPreferencesDataStore()
}
