package com.qusin.qusin.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.qusin.qusin.ui.theme.QusinMotion
import com.qusin.qusin.ui.theme.QusinTheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** App 里的页面。对话是根页面,其余都从它(或从设置)进去,返回时回到进来的那一页。 */
enum class Route { Chat, Personal, QuickVoice, Settings, Memory, Plans, Browser, Files, Run, Source, PhoneSetup, Models, Usage, SpeechModel, VoiceModel, ImageModel, Welcome, ModelSetup, About, License, UpdateMirrors, Screens, Teach, Calls, Report, Playback, Persona }

/**
 * 页面栈:最底下永远是对话页。[push] 进一页,[back] 回上一页(已在对话页时返回 false,交给系统退出)。
 * 返回键和页面上的返回按钮走同一个 [back],所以两者总是一致。
 */
@Stable
class NavStack(initial: List<Route> = listOf(Route.Chat), fromDrawer: Boolean = false) {
    var stack: List<Route> by mutableStateOf(normalize(initial))
        private set

    /** 这一串页面是从侧栏进去的:退回对话页时要回到打开的侧栏。 */
    private var fromDrawer = fromDrawer && stack.size > 1

    /** 刚从侧栏进去的页面退回到了对话页,侧栏该重新打开;打开后调 [drawerReopened]。 */
    var reopenDrawer: Boolean by mutableStateOf(false)
        private set

    /** 每次外部跳转或系统分享回到对话页时递增,让已展开的侧栏一并收起。 */
    var resetSerial: Int by mutableStateOf(0)
        private set

    val current: Route get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    /** 最近一次导航的方向,供页面转场判断推入还是返回。 */
    var transitionForward: Boolean by mutableStateOf(true)
        private set

    /** 进一页;已经在这一页时不重复压栈。 */
    fun push(route: Route) {
        if (route == Route.Chat) return reset()
        if (current != route) {
            transitionForward = true
            stack = stack + route
        }
    }

    /** 从侧栏进一页:之后一路退回对话页时,侧栏是打开的。 */
    fun pushFromDrawer(route: Route) {
        fromDrawer = true
        push(route)
    }

    fun back(): Boolean {
        if (!canGoBack) return false
        transitionForward = false
        stack = stack.dropLast(1)
        if (!canGoBack && fromDrawer) {
            fromDrawer = false
            reopenDrawer = true
        }
        return true
    }

    fun drawerReopened() {
        reopenDrawer = false
    }

    /** 只留对话页和 [route] 两层(首次上手的每一步用)。 */
    fun show(route: Route) {
        fromDrawer = false
        transitionForward = route != Route.Chat
        stack = normalize(listOf(route))
    }

    /** 把 [route] 提到最上层;若已在栈中,移除它上面的页面。 */
    fun bringToTop(route: Route) {
        if (route == Route.Chat) return reset()
        val at = stack.indexOf(route)
        transitionForward = at < 0 || at == stack.lastIndex
        stack = if (at < 0) stack + route else stack.take(at + 1)
    }

    /** 从栈中移除 [route],不影响它之前的页面。 */
    fun leave(route: Route) {
        if (route == Route.Chat || route !in stack) return
        if (current == route) back() else stack = stack - route
    }

    /** 回到对话页(打开某个对话时)。 */
    fun reset() {
        fromDrawer = false
        reopenDrawer = false
        transitionForward = false
        stack = listOf(Route.Chat)
        resetSerial++
    }

    companion object {
        private const val DRAWER_MARK = "fromDrawer"

        private fun normalize(list: List<Route>): List<Route> =
            listOf(Route.Chat) + list.filter { it != Route.Chat }

        val Saver: Saver<NavStack, ArrayList<String>> = Saver(
            save = { ArrayList(it.stack.map(Route::name) + listOfNotNull(DRAWER_MARK.takeIf { _ -> it.fromDrawer })) },
            restore = { names ->
                NavStack(names.mapNotNull { n -> Route.entries.firstOrNull { it.name == n } }, fromDrawer = DRAWER_MARK in names)
            },
        )
    }
}

@Composable
fun rememberNavStack(first: Route = Route.Chat): NavStack =
    rememberSaveable(saver = NavStack.Saver) { NavStack(listOf(first)) }


/**
 * 统一页面切换:新页面从右侧进入,返回时从左侧退回。
 * 系统减少动态效果时改为即时淡入淡出,不强制播放位移动画。
 */
@Composable
fun <T> NavSlide(
    targetState: T,
    forward: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedContentScope.(T) -> Unit,
) {
    val reduceMotion = QusinTheme.reduceMotion
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(animationSpec = androidx.compose.animation.core.snap()) togetherWith
                    fadeOut(animationSpec = androidx.compose.animation.core.snap())
            } else {
                val slide = tween<IntOffset>(
                    durationMillis = QusinMotion.PAGE_MS,
                    easing = QusinMotion.Decelerate,
                )
                if (forward) {
                    slideInHorizontally(animationSpec = slide) { it } togetherWith
                        slideOutHorizontally(animationSpec = slide) { -it / 4 }
                } else {
                    slideInHorizontally(animationSpec = slide) { -it / 4 } togetherWith
                        slideOutHorizontally(animationSpec = slide) { it }
                }
            }
        },
        label = "qusin-page-transition",
        content = content,
    )
}
