package com.iksgmbh.sysnat.guicontrol;

import java.awt.Component;

import org.netbeans.jemmy.operators.JFrameOperator;

public interface SwingGuiControl extends GuiControl
{
	JFrameOperator getMainFrameHandle();
	
	// checkbox methods 
	boolean isDialogCheckBoxTicked(String dialogName, String technicalCheckboxName);
	/** @return true if checkbox was unticked  */
	boolean assureNoTickInDialogCheckBox(String dialogName, String technicalCheckboxName);
	/** @return true if checkbox was ticked  */
	boolean assureTickInDialogCheckBox(String dialogName, String technicalCheckboxName);
	
	// table methods
	int getNumberOfColumnsOfDialogTable(String dialogName, String technicalTableName);
	int getNumberOfRowsOfDialogTable(String dialogName, String tableIdentifier);	
	void clickDialogTableCell(String dialogName, String tableIdentifier, int rowNo, int columnNo);
	
	// button methods
	void clickDialogButton(String dialogName, String buttonName);
	void clickDialogButton(String dialogName, String buttonName, long timeout);
	boolean clickDialogButtonIfAvailable(String dialogName, String buttonName);
	boolean clickDialogButtonIfAvailable(String dialogName, String buttonName, long timeout);
	boolean clickDialogButtonIfAvailable(String dialogName, String buttonName, long timeout, boolean resetDialogHandle);

	Component getElement(String elementIdentifier);

}
