package org.andstatus.todoagenda.widget

import android.content.Context
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.WidgetLayout
import org.andstatus.todoagenda.prefs.colors.BackgroundColorPref
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.RemoteViewsUtil

/**
 * @author yvolk@yurivolkov.com
 */
class CurrentTimeVisualizer(
    context: Context,
    widgetId: Int,
) : WidgetEntryVisualizer(EventProvider(EventProviderType.CURRENT_TIME, context, widgetId)) {
    override fun getRemoteViews(
        eventEntry: WidgetEntry,
        position: Int,
    ): RemoteViews {
        val entry = eventEntry as CurrentTimeEntry
        val rv = RemoteViews(context.packageName, WidgetLayout.CURRENT_TIME_NARROW.shadowed(settings.textShadow))
        RemoteViewsUtil.setBackgroundColor(
            rv,
            R.id.current_time_line,
            settings.colors().getBackgroundColor(BackgroundColorPref.CURRENT_TIME),
        )
        RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry, settings.colors().getEntryBackgroundColor(entry))
        RemoteViewsUtil.setCompact(settings, rv)
        return rv
    }

    override fun queryEventEntries(): List<WidgetEntry> =
        if (settings.showCurrentTimeLine) {
            listOf(CurrentTimeEntry(settings))
        } else {
            emptyList()
        }
}
