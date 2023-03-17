package retrofit2

import android.os.Build
import timber.log.Timber
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object RetrofitPreloadUtil {
    private var loadServiceMethod: Method? = null
    var initSuccess: Boolean = false
    //    private var serviceMethodCacheField:Map<Method,ServiceMethod<Any>>?=null
    private var serviceMethodCacheField: Field? = null

    init {
        try {
            serviceMethodCacheField = Retrofit::class.java.getDeclaredField("serviceMethodCache")
            serviceMethodCacheField?.isAccessible = true
            if (serviceMethodCacheField == null) {
                for (declaredField in Retrofit::class.java.declaredFields) {
                    if (Map::class.java.isAssignableFrom(declaredField.type)) {
                        declaredField.isAccessible =true
                        serviceMethodCacheField = declaredField
                        break
                    }
                }
            }
            loadServiceMethod = Retrofit::class.java.getDeclaredMethod("loadServiceMethod", Method::class.java)
            loadServiceMethod?.isAccessible = true
        } catch (e: Exception) {
            initSuccess = false
        }
    }

    /**
     * 预加载 目标service 的 相关函数，并注入到对应retrofit实例中
     */
    fun preloadClassMethods(retrofit: Retrofit, service: Class<*>, methodNames: Array<String>) {
        val field = serviceMethodCacheField ?: return
        val map = field.get(retrofit) as MutableMap<Method,ServiceMethod<Any>>

        for (declaredMethod in service.declaredMethods) {
            if (!isDefaultMethod(declaredMethod) && !Modifier.isStatic(declaredMethod.modifiers)
                && methodNames.contains(declaredMethod.name)) {
                try {
                    val parsedMethod = ServiceMethod.parseAnnotations<Any>(retrofit, declaredMethod) as ServiceMethod<Any>
                    map[declaredMethod] =parsedMethod
                } catch (e: Exception) {
                    Timber.e(e, "load method $declaredMethod for class $service failed")
                }
            }
        }

    }

    private fun isDefaultMethod(method: Method): Boolean {
        return Build.VERSION.SDK_INT >= 24 && method.isDefault;
    }

}
