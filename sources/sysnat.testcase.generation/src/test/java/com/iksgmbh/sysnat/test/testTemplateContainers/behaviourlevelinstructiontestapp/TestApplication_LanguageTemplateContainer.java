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
package com.iksgmbh.sysnat.test.testTemplateContainers.behaviourlevelinstructiontestapp;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;

@LanguageTemplateContainer
public class TestApplication_LanguageTemplateContainer 
{
	@LanguageTemplate(value = "Behaviour: ^^")
	public void declareXXGroupForBehaviour(String id) {
		// do nothing
	}
	
	@LanguageTemplate(value = "A first basic condition.")
	public void prepareOnce() {
		// do nothing
	}

	@LanguageTemplate(value = "A second basic condition.")
	public void prepare() {
		// do nothing
	}

	@LanguageTemplate(value = "a first condition")
	@LanguageTemplate(value = "A first condition is true.")
	public void setFirstCondition() {
		// do nothing
	}

	@LanguageTemplate(value = "a second condition")
	@LanguageTemplate(value = "A second condition is true.")
	public void setSecondCondition() {
		// do nothing
	}

	@LanguageTemplate(value = "a third condition")
	@LanguageTemplate(value = "A third condition is true.")
	public void setThirdCondition() {
		// do nothing
	}

	@LanguageTemplate(value = "something happens")
	@LanguageTemplate(value = "Something happens.")
	public void doFirstAction() {
		// do nothing
	}

	@LanguageTemplate(value = "something else happens")
	@LanguageTemplate(value = "Something else happens.")
	public void doSecondAction() {
		// do nothing
	}

	@LanguageTemplate(value = "something strange happens")
	@LanguageTemplate(value = "Something strange happens.")
	public void doThirdAction() {
		// do nothing
	}

	@LanguageTemplate(value = "this is true.")
	@LanguageTemplate(value = "Is this true?")
	public void doFirstAssertion() {
		// do nothing
	}

	@LanguageTemplate(value = "that is true.")
	@LanguageTemplate(value = "That is true.")
	public void doSecondAssertion() {
		// do nothing
	}
	
	@LanguageTemplate(value = "an error occurred.")
	@LanguageTemplate(value = "An error occurred.")
	public void doThirdAssertion() {
		// do nothing
	}

	@LanguageTemplate(value = "A first cleanup step.")
	public void doFirstCleanupStep() {
		// do nothing
	}

	@LanguageTemplate(value = "A second cleanup step.")
	public void doSecondCleanupStep() {
		// do nothing
	}
	
	@LanguageTemplate(value = "XXID: ^^")  
	@LanguageTemplate(value = "XX: ^^")  
	public void startNewXX(String id) {
		// do nothing
	}
	
	@LanguageTemplate(value = "Test-Phase: ^^")  
	public void startNewTestPhase(String testPhaseName) {
		// do nothing
	}
	
	@LanguageTemplate(value = "TestData: ^^")  
	public void setTestData(String multiLineDataTableString) {
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