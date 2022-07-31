package com.example.user

import com.example.user.vm.emailRegex
import com.example.user.vm.telRegex
import org.junit.Test
import org.junit.Assert.*


class LoginViewModelUnitTest {

    @Test
    fun regexTest(){
        assertTrue(telRegex.matches("13837101234"))
        assertFalse(telRegex.matches(""))
        assertFalse(telRegex.matches("asdf123"))

        assertTrue(emailRegex.matches("test@qq.com"))
        assertTrue(emailRegex.matches("g_test@q.com"))
        assertFalse(emailRegex.matches("@qq.com"))
        assertFalse(emailRegex.matches("test.com"))
        assertFalse(emailRegex.matches("@.com"))
        assertFalse(emailRegex.matches("@."))


    }

}