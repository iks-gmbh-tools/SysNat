package com.iksgmbh.sysnat;

import com.iksgmbh.sysnat.dialog.SysNatDialog;

public class SysNatDialogDevMode extends SysNatDialog
{
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) 
	{
		SysNatDialog.mode = DialogMode.Developer;
		SysNatDialog.doYourJob();
	}

	private SysNatDialogDevMode() throws Exception {}
}
