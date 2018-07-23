package com.iksgmbh.sysnat.test.testTemplateContainers.testcasegeneratortestapplication;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.test.domain.Invoice;
import com.iksgmbh.sysnat.test.domain.Order;

@LanguageTemplateContainer
public class ComplexInstructionTestApplication_LanguageTemplateContainer 
{
	
	@LanguageTemplate(value = "Erzeuge eine Rechnung <>.")  //
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
