package com.wangzan101.qusin.core.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityListTest {
    private val qusin = PermissionCommands.accessibilityService("com.wangzan101.qusin")
    private val qusinLong = "com.wangzan101.qusin/com.wangzan101.qusin.a11y.QusinAccessibilityService"
    private val other = "com.example.reader/.ReaderService"
    private val other2 = "org.sample.keys/org.sample.keys.KeyService"

    @Test
    fun emptyOrUnsetValues() {
        listOf(null, "", "   ", "null", "null\n", ":", " : ").forEach { raw ->
            assertEquals("raw=$raw", emptyList<String>(), AccessibilityList.entries(raw))
            assertFalse(AccessibilityList.contains(raw, qusin))
            assertEquals(qusin, AccessibilityList.with(raw, qusin))
            assertEquals("", AccessibilityList.without(raw, qusin))
        }
    }

    @Test
    fun appendKeepsOtherServicesInOrder() {
        assertEquals("$other:$qusin", AccessibilityList.with(other, qusin))
        assertEquals("$other:$other2:$qusin", AccessibilityList.with("$other:$other2\n", qusin))
    }

    @Test
    fun alreadyPresentIsNotDuplicated() {
        assertEquals("$other:$qusin", AccessibilityList.with("$other:$qusin", qusin))
        // 系统里存的是完整类名写法,也算已经有了
        assertEquals("$qusinLong:$other", AccessibilityList.with("$qusinLong:$other", qusin))
        assertTrue(AccessibilityList.contains(qusinLong, qusin))
    }

    @Test
    fun removeKeepsOthersAndHandlesBothSpellings() {
        assertEquals("$other:$other2", AccessibilityList.without("$other:$qusin:$other2", qusin))
        assertEquals(other, AccessibilityList.without("$qusinLong:$other", qusin))
        assertEquals(other, AccessibilityList.without(other, qusin))
        assertEquals("", AccessibilityList.without(qusin, qusin))
    }

    @Test
    fun strayColonsAndSpacesAreCleanedUp() {
        assertEquals("$other:$other2:$qusin", AccessibilityList.with(":$other:: $other2 :", qusin))
        assertEquals("$other:$other2", AccessibilityList.without("$other::$qusin:$other2:", qusin))
    }

    @Test
    fun differentServiceOfSamePackageIsNotConfused() {
        val sibling = "com.wangzan101.qusin/.a11y.OtherService"
        assertFalse(AccessibilityList.contains(sibling, qusin))
        assertEquals(sibling, AccessibilityList.without("$sibling:$qusin", qusin))
    }
}
