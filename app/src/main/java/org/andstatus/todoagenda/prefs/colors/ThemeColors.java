package org.andstatus.todoagenda.prefs.colors;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.getColorValue;

/**
 * Colors part of settings for one theme, of one Widget
 * @author yvolk@yurivolkov.com
 */
public class ThemeColors {
    private static final String TAG = ThemeColors.class.getSimpleName();
    public static final int TRANSPARENT_BLACK = Color.TRANSPARENT;
    public static final int TRANSPARENT_WHITE = 0x00FFFFFF;
    public final static ThemeColors EMPTY = new ThemeColors(null, ColorThemeType.SINGLE);

    private final Context context;
    public final ColorThemeType colorThemeType;
    public final ConcurrentMap<BackgroundColorPref, ShadingAndColor> backgroundColors = new ConcurrentHashMap<>();
    public static final String PREF_TEXT_COLOR_SOURCE = "textColorSource";
    public TextColorSource textColorSource = TextColorSource.defaultValue;
    public final ConcurrentMap<TextColorPref, ShadingAndColor> textColors = new ConcurrentHashMap<>();

    public static ThemeColors fromJson(Context context, ColorThemeType colorThemeType, JSONObject json) {
        return new ThemeColors(context, colorThemeType).setFromJson(json);
    }

    public ThemeColors(Context context, ColorThemeType colorThemeType) {
        this.context = context;
        this.colorThemeType = colorThemeType;
    }

    public ThemeColors copy(Context context, ColorThemeType colorThemeType) {
        ThemeColors themeColors = new ThemeColors(context, colorThemeType);
        return isEmpty()
                ? themeColors
                : themeColors.setFromJson(toJson(new JSONObject()));
    }

    private ThemeColors setFromJson(JSONObject json) {
        try {
            for (BackgroundColorPref pref: BackgroundColorPref.values()) {
                int color = json.has(pref.colorPreferenceName)
                        ? json.getInt(pref.colorPreferenceName)
                        : pref.defaultColor;
                backgroundColors.put(pref, new ShadingAndColor(color));
            }

            if (json.has(PREF_TEXT_COLOR_SOURCE)) {
                textColorSource = TextColorSource.fromValue(json.getString(PREF_TEXT_COLOR_SOURCE));
            } else {
                // This was default before v.4.4
                textColorSource = TextColorSource.SHADING;
            }

            for (TextColorPref pref: TextColorPref.values()) {
                Shading shading = json.has(pref.shadingPreferenceName)
                    ? Shading.fromThemeName(json.getString(pref.shadingPreferenceName), pref.defaultShading)
                    : pref.defaultShading;
                int color = json.has(pref.colorPreferenceName)
                    ? json.getInt(pref.colorPreferenceName)
                    : pref.defaultColor;
                textColors.put(pref, new ShadingAndColor(shading, color));
            }
        } catch (JSONException e) {
            Log.w(TAG, "setFromJson failed\n" + json);
            return this;
        }
        return this;
    }

    public ThemeColors setFromApplicationPreferences() {
        for (BackgroundColorPref pref: BackgroundColorPref.values()) {
            setBackgroundColor(pref, ApplicationPreferences.getBackgroundColor(pref, context));
        }
        textColorSource = ApplicationPreferences.getTextColorSource(context);
        for (TextColorPref pref: TextColorPref.values()) {
            ShadingAndColor oldValue = textColors.get(pref);
            if (oldValue == null) oldValue = new ShadingAndColor(pref.defaultShading, pref.defaultColor);
            String themeName = ApplicationPreferences.getString(context, pref.shadingPreferenceName, "");
            Shading shading = Shading.fromThemeName(themeName, oldValue.shading);
            int color = ApplicationPreferences.getInt(context, pref.colorPreferenceName, oldValue.color);
            textColors.put(pref, new ShadingAndColor(shading, color));
        }
        return this;
    }

    public JSONObject toJson(JSONObject json) {
        try {
            for (BackgroundColorPref pref: BackgroundColorPref.values()) {
                json.put(pref.colorPreferenceName, getBackgroundColor(pref));
            }
            json.put(PREF_TEXT_COLOR_SOURCE, textColorSource.value);
            for (TextColorPref pref: TextColorPref.values()) {
                ShadingAndColor shadingAndColor = textColors.get(pref);
                if (shadingAndColor == null) shadingAndColor = new ShadingAndColor(pref.defaultShading, pref.defaultColor);
                json.put(pref.shadingPreferenceName, shadingAndColor.shading.themeName);
                json.put(pref.colorPreferenceName, shadingAndColor.color);
            }
        } catch (JSONException e) {
            throw new RuntimeException("Saving settings to JSON", e);
        }
        return json;
    }

    public Context getContext() {
        return context;
    }

    private void setBackgroundColor(BackgroundColorPref pref, Integer backgroundColor) {
        int color = backgroundColor == null ? pref.defaultColor : backgroundColor;
        backgroundColors.put(pref, new ShadingAndColor(color));
    }

    public int getBackgroundColor(BackgroundColorPref colorPref) {
        return getBackground(colorPref).color;
    }

    public ShadingAndColor getBackground(BackgroundColorPref colorPref) {
        return backgroundColors.computeIfAbsent(colorPref, pref -> new ShadingAndColor(pref.defaultColor));
    }

    public boolean isEmpty() {
        return context == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThemeColors settings = (ThemeColors) o;
        return toJson(new JSONObject()).toString().equals(settings.toJson(new JSONObject()).toString());
    }

    @Override
    public int hashCode() {
        return toJson(new JSONObject()).toString().hashCode();
    }

    public int getTextColor(TextColorPref textColorPref, int colorAttrId) {
        if (textColorSource == TextColorSource.COLORS) {
            return getTextShadingAndColor(textColorPref).color;
        } else {
            return getColorValue(getShadingContext(textColorPref), colorAttrId);
        }
    }

    public ShadingAndColor getTextShadingAndColor(TextColorPref colorPref) {
        return textColors.computeIfAbsent(colorPref, pref -> new ShadingAndColor(pref.defaultShading, pref.defaultColor));
    }

    public Shading getShading(TextColorPref pref) {
        switch (textColorSource) {
            case SHADING:
                return getTextShadingAndColor(pref).shading;
            case COLORS:
                ShadingAndColor shadingAndColor2 = getTextShadingAndColor(pref);
                // TODO:
                return ShadingAndColor.colorToShading(shadingAndColor2.color);
            default:
                return pref.getShadingForBackground(getBackground(pref.backgroundColorPref).shading);
        }
    }

    public int getEntryBackgroundColor(WidgetEntry<?> entry) {
        return getBackgroundColor(BackgroundColorPref.forTimeSection(entry.timeSection));
    }

    public ContextThemeWrapper getShadingContext(TextColorPref pref) {
        return new ContextThemeWrapper(context, getShading(pref).themeResId);
    }

}
