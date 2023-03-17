package com.knightboost.gsonopt

/**
 * 在类上标记，为该类自动生成 Gson Adapter
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoGsonAdapter()
