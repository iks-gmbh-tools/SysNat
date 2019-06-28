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
package com.iksgmbh.sysnat.test.testTemplateContainers.testcasegeneratortestapplication;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.test.domain.Invoice;
import com.iksgmbh.sysnat.test.domain.Order;

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
	public void setTestData(String multiLineDataTableString) {
		// do nothing
	}
	
	@LanguageTemplate(value = "Erzeuge eine Rechnung <>.")
	public Invoice createInvoice() {
		// do nothing
		return null;
	}
	
	
	@LanguageTemplate(value = "Erzeuge einen Auftrag <>.")
	public Order createOrder() {
		// do nothing
		return null;
	}

	@LanguageTemplate(value = "Füge dem Auftrag '' den Wert ^^ zu.")
	public void addToOrder(Order order, String value) {
		// do nothing
	}

	@LanguageTemplate(value = "Erzeuge Auftrag <> aus '' und '' und Wert ^^.")
	public Order toOrder(Order order1, Order order2, String value) {
		// do nothing
		return null;
	}


	public void gotoStartPage() {
	}

}