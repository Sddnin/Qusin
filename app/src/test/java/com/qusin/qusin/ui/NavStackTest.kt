package com.qusin.qusin.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavStackTest {
    @Test fun backReturnsToWhereYouCameFrom() {
        val nav = NavStack()
        assertEquals(Route.Chat, nav.current)
        assertFalse(nav.canGoBack)
        assertFalse(nav.back())

        // 侧栏进记忆,返回回到对话;从设置进记忆,返回回到设置
        nav.push(Route.Memory)
        assertTrue(nav.back())
        assertEquals(Route.Chat, nav.current)
        nav.push(Route.Settings)
        nav.push(Route.Memory)
        assertEquals(listOf(Route.Chat, Route.Settings, Route.Memory), nav.stack)
        nav.back()
        assertEquals(Route.Settings, nav.current)
    }

    @Test fun samePageIsNotPushedTwice() {
        val nav = NavStack()
        nav.push(Route.PhoneSetup)
        nav.push(Route.PhoneSetup)
        assertEquals(listOf(Route.Chat, Route.PhoneSetup), nav.stack)
    }

    @Test fun resetAndChatGoBackToTheRoot() {
        val nav = NavStack(listOf(Route.Settings, Route.Memory))
        assertEquals(listOf(Route.Chat, Route.Settings, Route.Memory), nav.stack)
        nav.push(Route.Chat)
        assertEquals(listOf(Route.Chat), nav.stack)
        nav.push(Route.Plans)
        nav.reset()
        assertEquals(listOf(Route.Chat), nav.stack)
    }

    @Test fun restoredStackAlwaysStartsWithChat() {
        assertEquals(listOf(Route.Chat, Route.Run), NavStack(listOf(Route.Chat, Route.Chat, Route.Run)).stack)
        assertEquals(listOf(Route.Chat), NavStack(emptyList()).stack)
    }

    @Test fun quickVoiceReturnsToTheActiveConversationMode() {
        val nav = NavStack(listOf(Route.Personal))
        nav.push(Route.QuickVoice)
        assertEquals(Route.QuickVoice, nav.current)
        assertEquals(listOf(Route.Chat, Route.Personal, Route.QuickVoice), nav.stack)
        assertTrue(nav.back())
        assertEquals(Route.Personal, nav.current)

        nav.show(Route.Chat)
        nav.push(Route.QuickVoice)
        assertTrue(nav.back())
        assertEquals(Route.Chat, nav.current)
    }

    @Test fun backFromAPageOpenedInTheDrawerReopensTheDrawer() {
        val nav = NavStack()
        nav.pushFromDrawer(Route.Settings)
        nav.push(Route.Memory)
        nav.back()
        assertFalse(nav.reopenDrawer)
        nav.back()
        assertTrue(nav.reopenDrawer)
        nav.drawerReopened()
        assertFalse(nav.reopenDrawer)
    }

    @Test fun pagesNotOpenedFromTheDrawerReturnToChatOnly() {
        val nav = NavStack()
        nav.push(Route.Settings)
        nav.back()
        assertFalse(nav.reopenDrawer)
    }

    @Test fun openingAConversationDoesNotReopenTheDrawer() {
        val nav = NavStack()
        val initialReset = nav.resetSerial
        nav.pushFromDrawer(Route.Plans)
        nav.reset()
        assertEquals(initialReset + 1, nav.resetSerial)
        assertFalse(nav.reopenDrawer)
        nav.push(Route.Settings)
        nav.back()
        assertFalse(nav.reopenDrawer)
    }
    @Test fun navigationDirectionTracksPushAndBack() {
        val nav = NavStack()
        assertTrue(nav.transitionForward)
        nav.push(Route.Settings)
        assertTrue(nav.transitionForward)
        nav.back()
        assertFalse(nav.transitionForward)
        nav.show(Route.Personal)
        assertTrue(nav.transitionForward)
        nav.reset()
        assertFalse(nav.transitionForward)
    }

    @Test fun bringToTopAndLeaveKeepTheStackConsistent() {
        val nav = NavStack()
        nav.push(Route.Settings)
        nav.push(Route.Memory)
        nav.push(Route.Browser)
        nav.bringToTop(Route.Settings)
        assertEquals(listOf(Route.Chat, Route.Settings), nav.stack)
        assertFalse(nav.transitionForward)
        nav.push(Route.Files)
        nav.leave(Route.Settings)
        assertEquals(listOf(Route.Chat, Route.Files), nav.stack)
        assertTrue(nav.canGoBack)
        nav.leave(Route.Files)
        assertEquals(listOf(Route.Chat), nav.stack)
        assertFalse(nav.canGoBack)
    }


}
