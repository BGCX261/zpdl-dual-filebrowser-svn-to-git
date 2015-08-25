/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zpdl.studio.api.theme;

import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.util.TypedValue;

public class ApiTheme {
    public static final int THEME_WHITEBLUE  = 0x0001;
    public static final int THEME_BLACKGREEN = 0x0002;

    protected static int   theme = THEME_BLACKGREEN;
    protected static float scale = 1f;
    protected static float dip = 1f;

    public static void setTheme(int t) {
        ApiLog.i("setTheme = %d", t);
        theme = t;
    }

    public static int nextTheme() {
        switch(theme) {
        case THEME_WHITEBLUE :  return THEME_BLACKGREEN;
        case THEME_BLACKGREEN : return THEME_WHITEBLUE;
        default : return THEME_WHITEBLUE;
        }
    }

    public static void initDip(Context context) {
        initScale(context);
        dip = scale * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
    }

    private static void initScale(Context context) {
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;
        if(smallestScreenWidthDp >= 800) {
            scale = 1.25f;
        } else if(smallestScreenWidthDp >= 600) {
            scale = 1.5f;
        }
    }

    public static float TextSize(float size) {
        return size * scale;
    }

    public static float Dip() {
        return dip;
    }

    public static float Scale() {
        return scale;
    }

    public static int Color(Context context, int id) {
        return context.getResources().getColor(id);
    }

    public static int BackgoundId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.BACKGROUND;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.BACKGROUND;
            default : return ApiThemeWhiteBlue.BACKGROUND;
        }
    }

    public static int ForegroundColorId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.FOREGROUND;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.FOREGROUND;
            default : return ApiThemeWhiteBlue.FOREGROUND;
        }
    }

    public static int HighlightColorId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.HIGHLIGHT;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.HIGHLIGHT;
            default : return ApiThemeWhiteBlue.HIGHLIGHT;
        }
    }

    public static int SelectorListDrawableId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.SELECTOR_LIST;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.SELECTOR_LIST;
            default : return ApiThemeWhiteBlue.SELECTOR_LIST;
        }
    }

    public static int DialogDividerDrawableId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_DIVIDER;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_DIVIDER;
            default : return ApiThemeWhiteBlue.DIALOG_DIVIDER;
        }
    }

    public static int DialogTitleColorId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_TITLE;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_TITLE;
            default : return ApiThemeWhiteBlue.DIALOG_TITLE;
        }
    }

    public static int DialogButtonTextColorId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_BUTTON_TEXT;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_BUTTON_TEXT;
            default : return ApiThemeWhiteBlue.DIALOG_BUTTON_TEXT;
        }
    }

    public static int DialogButtonSelectorId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_BUTTON_SELECTOR;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_BUTTON_SELECTOR;
            default : return ApiThemeWhiteBlue.DIALOG_BUTTON_SELECTOR;
        }
    }

    public static int DialogFileEditorLayoutId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_FILE_EDITOR_LAYOUT;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_FILE_EDITOR_LAYOUT;
            default : return ApiThemeWhiteBlue.DIALOG_FILE_EDITOR_LAYOUT;
        }
    }

    public static int DialogProgressHorizontalId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_PROGRESS_HORIZONTAL;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_PROGRESS_HORIZONTAL;
            default : return ApiThemeWhiteBlue.DIALOG_PROGRESS_HORIZONTAL;
        }
    }

    public static int DialogProgressIndeterminateHorizontalId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return ApiThemeWhiteBlue.DIALOG_PROGRESS_INDETERMINATE_HORIZONTAL;
            case THEME_BLACKGREEN : return ApiThemeBlackGreen.DIALOG_PROGRESS_INDETERMINATE_HORIZONTAL;
            default : return ApiThemeWhiteBlue.DIALOG_PROGRESS_INDETERMINATE_HORIZONTAL;
        }
    }
}
