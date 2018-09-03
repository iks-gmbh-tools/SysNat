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
package com.iksgmbh.sysnat.test.testTemplateContainers.badapplication;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;

public class LanguageTemplateContainerFailingTestImpl 
{

	/*
	 * This is supposed to fail, because the return value is expected
	 * to be mentioned in the NaturalLanguagePattern by ''.
	 */
	@LanguageTemplate(value = "Create with ^^.")
	public Integer methodWithReturnValue(String s) {
		// do nothing
		return null;
	}

	/*
	 * This is supposed to fail, because the return value indicated 
	 * in the NaturalLanguagePatten is actually missing.
	 */
	@LanguageTemplate(value = "Create <> with ^^.")
	public void methodWithoutReturnValue(String s) {
		// do nothing
	}

	/*
	 * This is supposed to fail, because the return value is indicated twice
	 * in the NaturalLanguagePatten.
	 */
	@LanguageTemplate(value = "Create <> with <>.")
	public String methodAnnotatedWithTwoReturnValues() {
		// do nothing
		return null;
	}
	
	/*
	 * This is supposed to fail, because the parameter count in NaturalLanguagePatten
	 * and java method mismatch.
	 */
	@LanguageTemplate(value = "Do ^^ with ^^.")
	public void methodAnnotatedWithTwoParameters(String s) {
		// do nothing
	}

}