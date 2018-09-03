/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.common.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class SysNatLocaleConstants 
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	
	public static final String PICTURE_PROOF = BUNDLE.getString("PICTURE_PROOF");
	public static final String CATEGORY_BILDNACHWEIS = BUNDLE.getString("with") + PICTURE_PROOF;
	public static final String ERROR_KEYWORD = BUNDLE.getString("Error");	
	public static final String TECHNICAL_ERROR_TEXT = BUNDLE.getString("TECHNICAL_ERROR_TEXT");
	public static final String ASSERT_ERROR_TEXT = BUNDLE.getString("ASSERT_ERROR_TEXT");
	public static final String ARRANGE_KEYWORD = BUNDLE.getString("ARRANGE_KEYWORD");
	public static final String ACT_KEYWORD = BUNDLE.getString("ACT_KEYWORD");
	public static final String ASSERT_KEYWORD = BUNDLE.getString("ASSERT_KEYWORD");
	public static final String CLEANUP_KEYWORD = BUNDLE.getString("CLEANUP_KEYWORD");
	public static final String YES_KEYWORD = BUNDLE.getString("Yes") + ".";
	public static final String NO_KEYWORD = BUNDLE.getString("No") + "!";
	public static final String FROM_FILENAME = BUNDLE.getString("FROM_FILENAME");
	public static final String FROM_PACKAGE = BUNDLE.getString("FROM_PACKAGE");
	public static final String NON_KEYWORD = BUNDLE.getString("NON") + "-";

	public static final String FILTER_CATEGORIES_TO_EXECUTE = BUNDLE.getString("FILTER_CATEGORIES_TO_EXECUTE");
	public static final String POSSIBLE_VALUE_IDENTIFIER = BUNDLE.getString("POSSIBLE_VALUE_IDENTIFIER");
	public static final String ENVIRONMENT_SETTING_KEY = BUNDLE.getString("ENVIRONMENT_SETTING_KEY");
	public static final String TESTAPP_SETTING_KEY = BUNDLE.getString("TESTAPP_SETTING_KEY");
	public static final String EXECUTION_SPEED_SETTING_KEY = BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY");
	public static final String ARCHIVE_DIR_SETTING_KEY = BUNDLE.getString("ARCHIVE_DIR_SETTING_KEY");
	public static final String REPORT_NAME_SETTING_KEY = BUNDLE.getString("REPORT_NAME_SETTING_KEY");
	public static final String BROWSER_SETTING_KEY = BUNDLE.getString("BROWSER_SETTING_KEY");

}