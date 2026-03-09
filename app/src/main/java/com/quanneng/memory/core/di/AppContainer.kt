package com.quanneng.memory.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.quanneng.memory.core.datastore.MultiInstanceWidgetPreferences
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.core.widget.WidgetUpdater
import com.quanneng.memory.features.flashthought.data.FlashThoughtDao
import com.quanneng.memory.features.flashthought.data.FlashThoughtDatabase
import com.quanneng.memory.features.flashthought.data.FlashThoughtRepository
import com.quanneng.memory.features.widget.data.WidgetDataSource
import com.quanneng.memory.features.widget.data.WidgetRepository

/**
 * DataStore扩展
 */
private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_preferences"
)

/**
 * 应用依赖注入容器
 * 手动依赖注入，避免使用复杂框架
 */
class AppContainer(context: Context) {

    private val dataStore: DataStore<Preferences> = context.widgetDataStore

    private val dispatcherProvider = DispatcherProvider()

    val widgetDataSource: WidgetDataSource = MultiInstanceWidgetPreferences(
        dataStore = dataStore,
        dispatchers = dispatcherProvider
    )

    val widgetRepository: WidgetRepository = WidgetRepository(
        dataSource = widgetDataSource,
        dispatchers = dispatcherProvider
    )

    val widgetUpdater: WidgetUpdater = WidgetUpdater(
        context = context,
        repository = widgetRepository,
        dispatchers = dispatcherProvider
    )

    // Room 数据库
    private val flashThoughtDatabase: FlashThoughtDatabase by lazy {
        Room.databaseBuilder(
            context,
            FlashThoughtDatabase::class.java,
            "flash_thoughts.db"
        ).build()
    }

    val flashThoughtDao: FlashThoughtDao = flashThoughtDatabase.flashThoughtDao()
    val flashThoughtRepository: FlashThoughtRepository = FlashThoughtRepository(
        dao = flashThoughtDao,
        dispatchers = dispatcherProvider
    )
}
