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
package com.iksgmbh.sysnat.test.testTemplateContainers.scenariobasedapplication;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;

@LanguageTemplateContainer
public class ComplexInstructionTestApplication_LanguageTemplateContainer 
{
	@LanguageTemplate(value = "Behaviour: ^^")
	public void declareXXGroupForBehaviour(String id) {
		// do nothing
	}

	@LanguageTemplate(value = "XXID: ^^")  
	public void startNewXX(String id) {
		// do nothing
	}
	
	
	@LanguageTemplate(value = "TestData: ^^")  
	public void setDatasetObject(String multiLineDataTableString) {
		// do nothing
	}

	@LanguageTemplate(value = "Something exists.")
	@LanguageTemplate(value = "Something is done.")
	@LanguageTemplate(value = "Something changed.")
	@LanguageTemplate(value = "Do something else.")
	@LanguageTemplate(value = "Do something.")
	@LanguageTemplate(value = "in reverse order")
	@LanguageTemplate(value = "those values are added")
	@LanguageTemplate(value = "the result is correct")
	public void doSomething() {
		// do nothing
	}

	@LanguageTemplate(value = "a value ^^")
	@LanguageTemplate(value = "the result is ^^")
	public void doSomething(String value) {
		// do nothing
	}

	public void gotoStartPage() {
	}

	@LanguageTemplate(value = "Set BDD-Keyword ^^.")
	public void setBddKeyword(String aKeyword) {

}

}