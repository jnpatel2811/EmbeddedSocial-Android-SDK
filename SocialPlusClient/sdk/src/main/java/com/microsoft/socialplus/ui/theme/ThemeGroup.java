/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.ui.theme;

import com.google.gson.annotations.SerializedName;
import com.microsoft.socialplus.sdk.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Theme group.
 */
public enum ThemeGroup {

	@SerializedName("light")
	LIGHT,

	@SerializedName("dark")
	DARK;

	static {
		LIGHT.themeResIds.put(Theme.REGULAR, R.style.SocialPlusSdkAppTheme_Light);
		LIGHT.themeResIds.put(Theme.SEARCH, R.style.SocialPlusSdkAppTheme_Light_Search);
		DARK.themeResIds.put(Theme.REGULAR, R.style.SocialPlusSdkAppTheme_Dark);
		DARK.themeResIds.put(Theme.SEARCH, R.style.SocialPlusSdkAppTheme_Dark);
	}

	private final Map<Theme, Integer> themeResIds = new HashMap<>();

	public int getThemeResId(Theme theme) {
		return themeResIds.get(theme);
	}

}
