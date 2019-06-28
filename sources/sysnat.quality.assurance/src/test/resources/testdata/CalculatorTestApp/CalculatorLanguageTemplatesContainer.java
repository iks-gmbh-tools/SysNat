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
package com.iksgmbh.sysnat.test.language_container;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.test.Calculator;

@LanguageTemplateContainer
public class CalculatorLanguageTemplatesContainer
{	
	/*
	 * Note: 
	 * For the module test reasons the test application "Calculator" is 
	 * embedded here within the templeate container. 
	 * For system tests this must be done!
	 */
	private Calculator calculator = new Calculator();
	
	private ExecutableExample executableExample;
	
	public CalculatorLanguageTemplatesContainer(ExecutableExample aExecutableExample) {
		executableExample = aExecutableExample;
	}

	@LanguageTemplate(value = "Enter number ^^.")
	public void enterNumber(String number) {
		calculator.enter(Integer.parseInt(number));
		executableExample.addReportMessage("Number <b>" + number + "</b> has been entered.");
	}

	@LanguageTemplate(value = "Calculate sum.")
	public void calculateSum() {
		calculator.addAllEnteredNumbers();
		executableExample.addReportMessage("Entered numbers has been summed up.");
	}
	
	@LanguageTemplate(value = "The result is ^^.")
	public void checkResult(String expected) 
	{
		boolean ok = Integer.parseInt(expected) == calculator.getResult();
		if (ok) {
			executableExample.addReportMessage("The result equals <b>" + expected + "</b>.");
		} else {
			executableExample.addReportMessage("The result does not equals <b>" + expected + "</b>.");
		}
	}

}