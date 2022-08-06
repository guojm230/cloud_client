package com.example.base

import org.junit.Test
import java.lang.invoke.MethodHandles

class Test2{
    fun test(){

    }
}

class EventTest {

    @Test
    fun methodHandle(){
        val method = Test2::class.java.getMethod("test")
        method.isAccessible = true
        val handler = MethodHandles.lookup().unreflect(method)
        handler.invokeWithArguments(Test2())
        println()
    }

}