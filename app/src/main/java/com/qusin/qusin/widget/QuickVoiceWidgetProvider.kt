package com.qusin.qusin.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlin.math.roundToInt
import android.widget.RemoteViews
import com.qusin.qusin.R
import com.qusin.qusin.ui.MainActivity

/** Home-screen launcher only. It never owns a microphone or a task session. */
class QuickVoiceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        update(context, manager, appWidgetId, newOptions)
    }

    private fun update(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle = manager.getAppWidgetOptions(appWidgetId),
    ) {
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight)
        val width = maxOf(minWidth, maxWidth)
        val height = maxOf(minHeight, maxHeight)
        val large = width >= 220 && height >= 90
        val wide = !large && width >= 210
        val micro = width < 150
        val views = RemoteViews(context.packageName, R.layout.widget_quick_voice)
        val horizontalPadding = if (large) 16 else if (micro) 8 else 12
        // A 4x1 widget can be only 48dp tall too: leave 40dp for its action pill.
        val verticalPadding = if (large) 16 else if (wide) 4 else 8
        views.setViewPadding(
            R.id.widget_root,
            horizontalPadding.dp(context),
            verticalPadding.dp(context),
            horizontalPadding.dp(context),
            verticalPadding.dp(context),
        )
        views.setViewVisibility(R.id.widget_compact, if (!wide && !large) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_compact_title, if (micro) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.widget_wide, if (wide) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_large, if (large) View.VISIBLE else View.GONE)

        val speak = pendingActivity(context, appWidgetId, quickVoiceIntent(context, appWidgetId))
        val type = pendingActivity(context, appWidgetId + 10_000, workIntent(context, appWidgetId))
        listOf(R.id.widget_compact, R.id.widget_wide, R.id.widget_large_speak).forEach { id ->
            views.setOnClickPendingIntent(id, speak)
        }
        views.setOnClickPendingIntent(R.id.widget_large_type, type)
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).roundToInt()

    private fun pendingActivity(context: Context, requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun quickVoiceIntent(context: Context, id: Int) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_QUICK_VOICE
            data = android.net.Uri.parse("qusin://widget/voice/$id")
            putExtra(MainActivity.EXTRA_OPEN_QUICK_VOICE, true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

    private fun workIntent(context: Context, id: Int) =
        Intent(context, MainActivity::class.java).apply {
            action = "com.qusin.qusin.action.OPEN_WORK"
            data = android.net.Uri.parse("qusin://widget/work/$id")
            putExtra(MainActivity.EXTRA_OPEN_WORK, true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
}
