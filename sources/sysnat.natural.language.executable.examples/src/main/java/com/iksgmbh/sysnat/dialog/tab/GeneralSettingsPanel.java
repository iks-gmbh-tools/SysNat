package com.iksgmbh.sysnat.dialog.tab;

import java.util.List;

import javax.swing.JComboBox;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DialogStartTab;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ResultLaunchOption;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.BasicTabPanel;
import com.iksgmbh.sysnat.dialog.SysNatDialog;

public class GeneralSettingsPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;

	private JComboBox<String> cbxUseDialogSetting, cbxStartTabSetting, cbxResultLaunchOption;

	public GeneralSettingsPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.GENERAL_CONFIG_FILE_NAME));
		setLayout(null);
		initGeneralSettingsComponents();
	}
	
	private void initGeneralSettingsComponents()
	{
		yPos = 10 + deltaY;
		//initSectionLabel(parent, "DOCING_SECTION_1_LABEL", yPos);
		initLabel(this, "USE_DIALOG_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] options1 = {BUNDLE.getString("USE_SYSNAT_DIALOG_OPTION_NO"), 
				             BUNDLE.getString("USE_SYSNAT_DIALOG_OPTION_YES")};
		cbxUseDialogSetting = initCombo(this, options1, yPos, getUseDialogDisplayValue(), NO_TOOLTIP);
		
		yPos += deltaY;
		initLabel(this, "START_TAB_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] options2 = {BUNDLE.getString("START_TAB_OPTION_TESTING"), 
				             BUNDLE.getString("START_TAB_OPTION_DOCING"), 
				             BUNDLE.getString("START_TAB_OPTION_GENERAL")};
		cbxStartTabSetting = initCombo(this, options2, yPos, getStartTabDisplayValue(), NO_TOOLTIP);
		
		yPos += deltaY;
		initLabel(this, "LAUNCH_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] options3 = {BUNDLE.getString("LAUNCH_OPTION_NONE"), 
				             BUNDLE.getString("LAUNCH_OPTION_TEST"),
				             BUNDLE.getString("LAUNCH_OPTION_DOC"), 
				             BUNDLE.getString("LAUNCH_OPTION_BOTH")};
		cbxResultLaunchOption = initCombo(this, options3, yPos, getLaunchOptionDisplayValue(), NO_TOOLTIP);
		
		initCancelButton(this);
		initSaveButton(this);
	}
	

	public void setSettingsToExecutionInfo()
	{
		executionInfo.setUseSysNatDialog(useSysNatDialogDisplayValueToSettingsKey());
		executionInfo.setDialogStartTab(startTabDisplayValueToSettingsKey());
		executionInfo.setResultLaunchOptionName(launchOptionDisplayValueToSettingsKey());
	}

	public void saveCurrentSettings()
	{

		final List<String> oldFileContent = SysNatFileUtil.readTextFile(configFileName);
		final StringBuffer newFileContent = new StringBuffer();

		for (String line : oldFileContent) 
		{
			line = line.trim();
			if (line.startsWith(SysNatConstants.USE_SYSNAT_DIALOG_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.USE_SYSNAT_DIALOG_SETTING_KEY + " = "
				        + useSysNatDialogDisplayValueToSettingsKey());
			} else if (line.startsWith(SysNatConstants.DIALOG_START_TAB_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DIALOG_START_TAB_SETTING_KEY + " = "
				        + startTabDisplayValueToSettingsKey());
			} else if (line.startsWith(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY + " = " 
			            + launchOptionDisplayValueToSettingsKey());
			} else {
				newFileContent.append(line);
			}
			newFileContent.append(System.getProperty("line.separator"));
		}

		// System.out.println(newFileContent.toString().trim());
		SysNatFileUtil.writeFile(configFileName, newFileContent.toString().trim());
	}
	
	private String getLaunchOptionDisplayValue()
	{
		ResultLaunchOption value = executionInfo.getResultLaunchOption();
		
		switch (value)
		{
			case Testing:
				return BUNDLE.getString("LAUNCH_OPTION_TEST");
			case Docing:
				return BUNDLE.getString("LAUNCH_OPTION_DOC");
			case Both:
				return BUNDLE.getString("LAUNCH_OPTION_BOTH");
			default:
				return BUNDLE.getString("LAUNCH_OPTION_NONE");
		}
	}

	private String launchOptionDisplayValueToSettingsKey()
	{
		String value = cbxResultLaunchOption.getSelectedItem().toString();
		
		if (BUNDLE.getString("LAUNCH_OPTION_NONE").equals(value)) {
			return ResultLaunchOption.None.name();
		}

		if (BUNDLE.getString("LAUNCH_OPTION_TEST").equals(value)) {
			return ResultLaunchOption.Testing.name();
		}
		
		if (BUNDLE.getString("LAUNCH_OPTION_DOC").equals(value)) {
			return ResultLaunchOption.Docing.name();
		}
	
		return ResultLaunchOption.Both.name();
	}

	private String getStartTabDisplayValue()
	{
		DialogStartTab value = executionInfo.getDialogStartTab();
		
		switch (value)
		{
			case Testing:
				return BUNDLE.getString("START_TAB_OPTION_TESTING");
			case Docing:
				return BUNDLE.getString("START_TAB_OPTION_DOCING");
			default:
				return BUNDLE.getString("START_TAB_OPTION_GENERAL");
		}
	}
	
	
	private String startTabDisplayValueToSettingsKey()
	{
		String value = cbxStartTabSetting.getSelectedItem().toString();

		if (BUNDLE.getString("START_TAB_OPTION_GENERAL").equals(value)) {
			return DialogStartTab.General.name();
		}
		
		if (BUNDLE.getString("START_TAB_OPTION_DOCING").equals(value)) {
			return DialogStartTab.Docing.name();
		}
	
		return DialogStartTab.Testing.name();
	}

	private String getUseDialogDisplayValue()
	{
		String value = executionInfo.getUseSysNatDialog();

		if (value.equalsIgnoreCase("yes")) {
			return BUNDLE.getString("USE_SYSNAT_DIALOG_OPTION_YES");
		}

		return BUNDLE.getString("USE_SYSNAT_DIALOG_OPTION_NO");
	}

	
	private String useSysNatDialogDisplayValueToSettingsKey()
	{
		String value = cbxUseDialogSetting.getSelectedItem().toString();
		
		if (BUNDLE.getString("USE_SYSNAT_DIALOG_OPTION_YES").equals(value)) {
			return "yes";
		}
		
		return "no";
	}


	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################

}
