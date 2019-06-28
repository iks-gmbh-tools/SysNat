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
package com.iksgmbh.sysnat.test.helper.faketestapp;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;

@LanguageTemplateContainer
public class FakeLanguageTemplatesContainer 
{	
	private ExecutableExample executableExample;
	private LanguageTemplatesCommon languageTemplatesCommon;

	public FakeLanguageTemplatesContainer(ExecutableExample aExecutableExample)
	{
		executableExample = aExecutableExample;
		languageTemplatesCommon = new LanguageTemplatesCommon(executableExample);
	}

	@LanguageTemplate(value = "XXID: ^^")
	public void startNewXX(String xxid) 
	{
		// do nothing here
	}

	@LanguageTemplate(value = "TestData: ^^")
	public void setTestData(String dataReference)  
	{
		languageTemplatesCommon.setTestData(dataReference);
	}
	
	@LanguageTemplate(value = "^^ = ^^") 
	public void setSingleTestDataValue(String objectAndfieldName, String value) {
		languageTemplatesCommon.setSingleTestDataValue(objectAndfieldName, value);
	}
	


}