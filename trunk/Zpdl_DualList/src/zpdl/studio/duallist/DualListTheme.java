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

package zpdl.studio.duallist;

import zpdl.studio.api.theme.ApiTheme;

public class DualListTheme extends ApiTheme {
    public static int TextApperrancePathId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperrancePathId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperrancePathId;
            default : return DualListThemeWhiteBlue.TextApperrancePathId;
        }
    }

    public static int TextApperranceRowNameId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperranceRowNameId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperranceRowNameId;
            default : return DualListThemeWhiteBlue.TextApperranceRowNameId;
        }
    }

    public static int TextApperranceRowInfoId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperranceRowInfoId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperranceRowInfoId;
            default : return DualListThemeWhiteBlue.TextApperranceRowInfoId;
        }
    }

    public static int TextApperranceDialogDetailNameId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperranceDialogDetailNameId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperranceDialogDetailNameId;
            default : return DualListThemeWhiteBlue.TextApperranceDialogDetailNameId;
        }
    }

    public static int TextApperranceDialogDetailTitleId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperranceDialogDetailTitleId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperranceDialogDetailTitleId;
            default : return DualListThemeWhiteBlue.TextApperranceDialogDetailTitleId;
        }
    }

    public static int TextApperranceDialogDetailContentId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.TextApperranceDialogDetailContentId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.TextApperranceDialogDetailContentId;
            default : return DualListThemeWhiteBlue.TextApperranceDialogDetailContentId;
        }
    }

    public static int ListClickEnableId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.ListClickEnableId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.ListClickEnableId;
            default : return DualListThemeWhiteBlue.ListClickEnableId;
        }
    }

    public static int ListClickDisableId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.ListClickDisableId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.ListClickDisableId;
            default : return DualListThemeWhiteBlue.ListClickDisableId;
        }
    }

    public static int DualListBoundaryId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.DualListBoundaryId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.DualListBoundaryId;
            default : return DualListThemeWhiteBlue.DualListBoundaryId;
        }
    }

    public static int DualListPathBackgroundId() {
        switch(theme) {
            case THEME_WHITEBLUE :  return DualListThemeWhiteBlue.DualListPathBackGroundId;
            case THEME_BLACKGREEN : return DualListThemeBalckGreen.DualListPathBackGroundId;
            default : return DualListThemeWhiteBlue.DualListPathBackGroundId;
        }
    }

    private static class DualListThemeWhiteBlue {
        static final int TextApperrancePathId = R.style.TextAppearance_wb_path;
        static final int TextApperranceRowNameId = R.style.TextAppearance_wb_row_name;
        static final int TextApperranceRowInfoId = R.style.TextAppearance_wb_row_info;

        static final int TextApperranceDialogDetailNameId = R.style.TextAppearance_wb_dialog_detail_name;
        static final int TextApperranceDialogDetailTitleId = R.style.TextAppearance_wb_dialog_detail_title;
        static final int TextApperranceDialogDetailContentId = R.style.TextAppearance_wb_dialog_detail_content;

        static final int ListClickEnableId = R.drawable.duallist_wb_click_enable;
        static final int ListClickDisableId = R.drawable.duallist_wb_click_disable;

        static final int DualListBoundaryId = R.drawable.duallist_wb_boundary;
        static final int DualListPathBackGroundId = R.color.duallist_wb_path_background;
    }

    private static class DualListThemeBalckGreen {
        static final int TextApperrancePathId = R.style.TextAppearance_bg_path;
        static final int TextApperranceRowNameId = R.style.TextAppearance_bg_row_name;
        static final int TextApperranceRowInfoId = R.style.TextAppearance_bg_row_info;

        static final int TextApperranceDialogDetailNameId = R.style.TextAppearance_bg_dialog_detail_name;
        static final int TextApperranceDialogDetailTitleId = R.style.TextAppearance_bg_dialog_detail_title;
        static final int TextApperranceDialogDetailContentId = R.style.TextAppearance_bg_dialog_detail_content;

        static final int ListClickEnableId = R.drawable.duallist_bg_click_enable;
        static final int ListClickDisableId = R.drawable.duallist_bg_click_disable;

        static final int DualListBoundaryId = R.drawable.duallist_bg_boundary;
        static final int DualListPathBackGroundId = R.color.duallist_bg_path_background;
    }
}
