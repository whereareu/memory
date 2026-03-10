package com.quanneng.memory.core.dispatchers

import kotlin.annotation.AnnotationTarget

/**
 * 标记函数应在 IO 调度器上执行
 * 用于数据层函数，确保线程安全
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class IoDispatcher
