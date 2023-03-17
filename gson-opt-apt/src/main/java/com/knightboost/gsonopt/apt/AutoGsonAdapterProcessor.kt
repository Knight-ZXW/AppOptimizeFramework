package com.knightboost.gsonopt.apt

import com.google.auto.service.AutoService
import com.knightboost.gsonopt.AutoGsonAdapter
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import java.beans.Visibility
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(Processor::class) @SupportedOptions("kapt.kotlin.generated") @SupportedSourceVersion(SourceVersion.RELEASE_7)
class AutoGsonAdapterProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(AutoGsonAdapter::class.java.name)
    }

    lateinit var typeUtils: Types;
    lateinit var elements: Elements;

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        typeUtils = processingEnv.typeUtils
        elements = processingEnv.elementUtils
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        System.out.println("注解处理器应用")
        val autoAdapters = roundEnv.getElementsAnnotatedWith(AutoGsonAdapter::class.java)
        if (autoAdapters.isEmpty()) {
            return false;
        }
        for (element in autoAdapters) {

            //todo 确认类的可访问性

        }



        return true
    }

    fun createJavaTypeAdapterFactory(
        sourceElements: Elements, elements: Elements, packageName: String, adapterName: String, qualifiedName: String
    ): TypeSpec? {
        var factory = TypeSpec.classBuilder(ClassName.get(packageName, "_" + adapterName))


        return null
    }
}
