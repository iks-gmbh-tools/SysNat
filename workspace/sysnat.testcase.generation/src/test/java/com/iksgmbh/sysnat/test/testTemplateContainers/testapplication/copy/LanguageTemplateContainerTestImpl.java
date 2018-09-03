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
package com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.copy;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;

@LanguageTemplateContainer
public class LanguageTemplateContainerTestImpl 
{
	@LanguageTemplate(value = "Natural language instruction without parameter.")
	@LanguageTemplate(value = "Natural language instruction without parameter. - Duplicate")
	@LanguageTemplate(value = "Natural language instruction without parameter. - Second Duplicate")
	public void methodWithoutParameter() {
		// do nothing
	}
	
	@LanguageTemplate(value = " Natural language instruction with ^^ parameter. ")
	public void methodWithOneParameter(String s) {
		// do nothing
	}

	@LanguageTemplate(value = "Natural language instruction with ^^ parameter.")
	public void anotherMethodWithOneParameter(int i) {
		// do nothing
	}

	@LanguageTemplate(value = "Natural ^^ language ^^ instruction ^^ with ^^ parameters.")
	public void methodWithFourParameters(String s1, String s2, String s3, String s4) {
		// do nothing
	}

	@LanguageTemplate(value = "^^ = ^^")
	public void methodWithTwoParameters(String s1, String s2) {
		// do nothing
	}

	@LanguageTemplate(value = "Create <> with ^^.")
	public Integer methodWithReturnValue(String s1) {
		// do nothing
		return null;
	}

	@LanguageTemplate(value = "Create <>.")
	public Integer methodOnlyWithReturnValue() {
		// do nothing
		return null;
	}
	
	@LanguageTemplate(value = "Create with ^^.")
	public void methodWithReturnValueButNoReferenceInLanguageTemplate(String s) {
		// do nothing
	}
	
	@LanguageTemplate(value = "Active: ^^")
	public void methodWithStageInstruction(String s) {
		// do nothing
	}	
}