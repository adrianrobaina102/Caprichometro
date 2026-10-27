package com.example.caprichometro.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UsernameRulesTest {

    @Test
    fun `isValid devuelve true para nombres de usuario validos`() {
        assertTrue(UsernameRules.isValid("adrian_dev"))
        assertTrue(UsernameRules.isValid("user123"))
        assertTrue(UsernameRules.isValid("abc"))
        assertTrue(UsernameRules.isValid("un_nombre_largo"))
    }

    @Test
    fun `isValid devuelve false para nombres demasiado cortos o largos`() {
        assertFalse(UsernameRules.isValid("ad"))
        assertFalse(UsernameRules.isValid("este_nombre_es_demasiado_largo"))
    }

    @Test
    fun `isValid devuelve false para caracteres especiales no permitidos`() {
        assertFalse(UsernameRules.isValid("adrian@dev"))
        assertFalse(UsernameRules.isValid("user.name"))
        assertFalse(UsernameRules.isValid("user name"))
    }
}