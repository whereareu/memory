package com.quanneng.memory

import android.app.Application
import com.quanneng.memory.core.di.AppContainer

/**
 * Application入口
 * 持有DI容器单例
 */
class MemoryApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
