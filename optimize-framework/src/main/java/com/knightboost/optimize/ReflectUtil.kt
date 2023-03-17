package com.knightboost.optimize

object ReflectUtil {

    fun getClass(className:String):Class<*>?{
        return try {
            Class.forName(className)
        }catch (e:Exception){
            null
        }
    }
}