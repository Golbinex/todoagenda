package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * See https://github.com/andstatus/todoagenda/issues/47
 * @author yvolk@yurivolkov.com
 */
enum class TextColorSource(
    val value: String,
    @field:StringRes val titleResId: Int,
    @field:StringRes val summaryResId: Int
) {
    AUTO("auto", R.string.text_color_source_auto, R.string.text_color_source_auto_desc),
    SHADING("shading", R.string.text_color_source_shading, R.string.text_color_source_shading_desc),
    COLORS("colors", R.string.text_color_source_color, R.string.text_color_source_color_desc);

    fun setTitle(activity: Activity?): TextColorSource {
        activity?.setTitle(titleResId)
        return this
    }

    companion object {
        val defaultValue = AUTO
        fun fromValue(value: String?): TextColorSource {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
