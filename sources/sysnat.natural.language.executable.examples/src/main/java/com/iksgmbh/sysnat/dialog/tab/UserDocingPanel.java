package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

public class UserDocingPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private boolean defaultDocNameMode = true;

	private JLabel lblDocArchiveDir;
	private JTextField txtDocName, txtDocArchiveDir;
	private JComboBox<String> cbxDocApplication, cbxDocType, cbxDocDepth, cbxDocEnvironment, cbxDocFormat;
	private JButton startDocingButton, btnDocingReset, btnDocFileSelect;
	private JCheckBox chbDocArchiving;

	private StartDocingActionListener startDocingActionListener = new StartDocingActionListener();
	private DocNameListener docingNameListener = new DocNameListener();
	private TargetEnvDocingListener targetEnvDocingListener = new TargetEnvDocingListener();

	public UserDocingPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.DOCING_CONFIG_FILE_NAME));
		setLayout(null);
		initDocingComponents();
		defaultDocNameMode = checkDocName();
		setEditablilityOfDocComponentsDueToCurrentSettings();
	}
	
	private void initDocingComponents()
	{
		yPos = 10;
		createSectionLabel(this, "DOCING_SECTION_1_LABEL", yPos);
		initComboDocApplication();
		initComboDocType();
		initComboDocDepth();
		initComboDocEnvironment();
		
		yPos += deltaY;
		createSeparationLine(this, yPos);

		yPos += deltaY/2;
		createSectionLabel(this, "DOCING_SECTION_2_LABEL", yPos);
		initComboDocFormat();
		
		yPos += deltaY;
		createSeparationLine(this, yPos);

		yPos += deltaY/2;
		createSectionLabel(this, "DOCING_SECTION_3_LABEL", yPos);		
		initTextFieldDocName();
		initDocNameResetButton();
		initCheckboxArchiving();
		initTextFieldArchiveDirectory();

		// button line
		initCancelButton(this);
		initSaveButton(this);
		initDocingStartButton(this);
	}
	
	public void saveCurrentSettings()
	{

		final List<String> oldFileContent = SysNatFileUtil.readTextFile(configFileName);
		final StringBuffer newFileContent = new StringBuffer();

		for (String line : oldFileContent) 
		{
			line = line.trim();
			if (line.startsWith(SysNatConstants.DOC_APPLICATION_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_APPLICATION_SETTING_KEY + " = "
				        + cbxDocApplication.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.DOC_TYPE_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_TYPE_SETTING_KEY + " = "
				        + docTypeDisplayValueToSettingKey());
			} else if (line.startsWith(SysNatConstants.DOC_DEPTH_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_DEPTH_SETTING_KEY + " = " 
			            + docDepthDisplayValueToSettingKey());
			} else if (line.startsWith(SysNatConstants.DOV_ENVIRONMENT_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOV_ENVIRONMENT_SETTING_KEY + " = " 
			            + toEnvSysNatEnum(cbxDocEnvironment.getSelectedItem().toString(),
			            		          cbxDocApplication.getSelectedItem().toString()));
			} else if (line.startsWith(SysNatConstants.DOC_FORMAT_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_FORMAT_SETTING_KEY + " = "
				        + cbxDocFormat.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.DOC_NAME_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_NAME_SETTING_KEY + " = " 
			            + txtDocName.getText());
			} else if (line.startsWith(SysNatConstants.DOC_ARCHIVE_DIR_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.DOC_ARCHIVE_DIR_SETTING_KEY + " = " 
			            + txtDocArchiveDir.getText());
			} else {
				newFileContent.append(line);
			}
			newFileContent.append(System.getProperty("line.separator"));
		}

		// System.out.println(newFileContent.toString().trim());
		SysNatFileUtil.writeFile(configFileName, newFileContent.toString().trim());
	}

	public void setSettingsToExecutionInfo()
	{
		// section 1 values
		String testApplicationName = cbxDocApplication.getSelectedItem().toString();
		executionInfo.setDocApplicationName(testApplicationName);
		executionInfo.setDocTypeName(docTypeDisplayValueToSettingKey());
		executionInfo.setDocDepthName(docDepthDisplayValueToSettingKey());
		String env = toEnvSysNatEnum(cbxDocEnvironment.getSelectedItem().toString(), testApplicationName);
		executionInfo.setDocEnvironmentName(env);

		// section 2 values
		executionInfo.setDocFormatName(cbxDocFormat.getSelectedItem().toString());

		// section 3 values
		executionInfo.setDocumentationName(txtDocName.getText().trim());
		executionInfo.setDocArchiveDir(txtDocArchiveDir.getText().trim());
		executionInfo.setArchiveDocumentation(chbDocArchiving.isSelected());
	}
	
	private String docDepthSettingKeyToDisplayValue()
	{
		String value = executionInfo.getDocDepthName();
		
		if (BUNDLE.getString("DOC_DEPTH_OPTION1").startsWith(value)
		    || BUNDLE_EN.getString("DOC_DEPTH_OPTION1").startsWith(value)) 
		{
			return BUNDLE.getString("DOC_DEPTH_OPTION1");
		}
		if (BUNDLE.getString("DOC_DEPTH_OPTION3").startsWith(value)
		    || BUNDLE_EN.getString("DOC_DEPTH_OPTION3").startsWith(value)) 
				{
					return BUNDLE.getString("DOC_DEPTH_OPTION3");
				}
		
		return BUNDLE.getString("DOC_DEPTH_OPTION2");
	}


	private String docDepthDisplayValueToSettingKey()
	{
		String value = cbxDocDepth.getSelectedItem().toString();
		
		if (BUNDLE.getString("DOC_DEPTH_OPTION1").equals(value)) {
			return SysNatConstants.DocumentationDepth.Minimum.name();
		}
		
		if (BUNDLE.getString("DOC_DEPTH_OPTION3").equals(value)) {
			return SysNatConstants.DocumentationDepth.Maximum.name();
		}

		return SysNatConstants.DocumentationDepth.Medium.name();
	}

	private String docTypeSettingKeyToDisplayValue()
	{
		String value = executionInfo.getDocTypeName();
		
		if (BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION").equals(value)
		 || BUNDLE_EN.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION").equals(value)) 
		{
			return BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION");
		}
		
		return BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION");
	}
	
	private String docTypeDisplayValueToSettingKey()
	{
		String value = cbxDocType.getSelectedItem().toString();
		
		if (BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION").equals(value)) {
			return SysNatConstants.DocumentationType.SystemDescription.name();
		}
		
		return SysNatConstants.DocumentationType.RequirementsReport.name();
	}
	
	private void initTextFieldArchiveDirectory()
	{
		yPos += deltaY;
		lblDocArchiveDir = initLabel(this, "DOC_ARCHIVE_DIR_DIALOG_LABEL", yPos, NO_TOOLTIP);
		txtDocArchiveDir = initTextField(this, yPos, executionInfo.getDocArchiveDir(), "", "txtDocArchiveDir");
		txtDocArchiveDir.setSize(new Dimension(600, SysNatDialog.lineHeight));
		btnDocFileSelect = initButton(this, "FILE_SELECT_BUTTON_TEXT", yPos, null);
		btnDocFileSelect.addActionListener(new FileSelectListener());
	}

	private void initDocNameResetButton()
	{
		btnDocingReset = initButton(this, "RESET_BUTTON_TEXT", yPos, BUNDLE.getString("RESET_BUTTON_TOOLTIP"));
		btnDocingReset.addActionListener(docingNameListener);
	}

	private void initCheckboxArchiving()
	{
		yPos += deltaY;
		chbDocArchiving = initCheckbox(this, BUNDLE.getString("WITH_ARCHIVING"), null, 
				                    executionInfo.isTestReportToArchive(), SysNatDialog.xPosFirstColumn, yPos);
		chbDocArchiving.addActionListener(new CheckBoxDocArchivingActionListener());
	}

	private void initTextFieldDocName()
	{
		yPos += deltaY;
		initLabel(this, "DOC_NAME_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String documentationName = getDocumentationName();
		txtDocName = initTextField(this, yPos, documentationName, "", "txtDocName");
		txtDocName.addKeyListener(docingNameListener);
		txtDocName.setSize(new Dimension(600, SysNatDialog.lineHeight));
	}

	private String getDocumentationName()
	{
		String toReturn = executionInfo.getDocumentationName();
		if (toReturn == null) {
			return null;
		}
		
		if (toReturn.isEmpty()) {
			toReturn = buildDefaultDocName();
		}

		if (toReturn.endsWith("-")) {
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}

		System.setProperty(SysNatConstants.DOC_NAME_SETTING_KEY, toReturn);
		return toReturn;
	}

	private void initComboDocFormat()
	{
		yPos += deltaY;
		initLabel(this, "DOCFORMAT_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] options = {"PDF", "DOCX", "HTML"};
		cbxDocFormat = initCombo(this, options, yPos, executionInfo.getDocFormatName(), NO_TOOLTIP, "cbxDocFormat");
		cbxDocFormat.addActionListener(docingNameListener);
	}

	private void initComboDocEnvironment()
	{
		yPos += deltaY;
		initLabel(this, "DOCENV_DIALOG_LABEL", yPos, NO_TOOLTIP);
		List<String> knownEnvironments = executionInfo.getKnownEnvironments(executionInfo.getTestApplicationName());
		String[] options = knownEnvironments.toArray(new String[knownEnvironments.size()]);
		String initialValue = toEnvSpecificToTestApplication(executionInfo.getDocEnvironmentName(), executionInfo.getTestApplicationName());
		cbxDocEnvironment = initCombo(this, options, yPos, initialValue, NO_TOOLTIP, "cbxDocEnvironment");
		cbxDocEnvironment.addActionListener(docingNameListener);
		targetEnvDocingListener.updateConfiguredTargetEnvironments();
		
		cbxDocEnvironment.setVisible(false);  // not yet relevant
	}

	private void initComboDocDepth()
	{
		yPos += deltaY;
		initLabel(this, "DOCDEPTH_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] values = {BUNDLE.getString("DOC_DEPTH_OPTION1"), 
				            BUNDLE.getString("DOC_DEPTH_OPTION2"),
				            BUNDLE.getString("DOC_DEPTH_OPTION3")};
		cbxDocDepth = initCombo(this, values, yPos, docDepthSettingKeyToDisplayValue(), NO_TOOLTIP, "cbxDocDepth");
	}

	private void initComboDocType()
	{
		yPos += deltaY;
		initLabel(this, "DOCTYPE_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String[] options = {BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION")}; //, 
				            //BUNDLE.getString("DOCTYPE_OPTION_REQUIREMENTS_DOCUMENTATION")};
		cbxDocType = initCombo(this, options, yPos, docTypeSettingKeyToDisplayValue(), NO_TOOLTIP, "cbxDocType");
		cbxDocType.addActionListener(docingNameListener);
	}

	private void initComboDocApplication()
	{
		yPos += deltaY;
		initLabel(this, "DOCAPP_DIALOG_LABEL", yPos, NO_TOOLTIP);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxDocApplication = initCombo(this, options, yPos,
		                              executionInfo.getDocApplicationName(), NO_TOOLTIP, "cbxDocApplication");
		cbxDocApplication.addActionListener(docingNameListener);
		cbxDocApplication.addActionListener(targetEnvDocingListener);
	}
	
	private void initDocingStartButton(JPanel parent) {
		startDocingButton = initStartButton(parent, BUNDLE.getString("DOCING_START_BUTTON_TEXT"), BUNDLE.getString("DOCING_START_BUTTON_TOOLTIP"), startDocingActionListener);
	}

	public String buildDefaultDocName()
	{
		String testApp = "";
		if (cbxDocApplication.getSelectedItem() != null) testApp = cbxDocApplication.getSelectedItem().toString();  

		String type = "";
		if (cbxDocType.getSelectedItem() != null) type = cbxDocType.getSelectedItem().toString();  
		
		String env = "";
		if (cbxDocEnvironment.getSelectedItem() != null) env = cbxDocEnvironment.getSelectedItem().toString();  
		
		String format = "";
		if (cbxDocFormat.getSelectedItem() != null) format = cbxDocFormat.getSelectedItem().toString();  
		
		
		
		if (type.equals(BUNDLE.getString("DOCTYPE_OPTION_SYSTEM_DESCRIPTION"))) {
			return testApp + "-" + type + "." + format;
		}
		
		return testApp + "-" + type + "-" + env + "." + format;
	}
	
	/**
	 * Checks whether report name matches the other selected settings
	 */
	private boolean checkDocName()
	{
		final String defaultFromGUI = buildDefaultDocName();
		final String defaultFromFile = getDocumentationName();
		return defaultFromFile.equals(defaultFromGUI);
	}

	private void setEditablilityOfDocComponentsDueToCurrentSettings()
	{
		boolean enableStateOfStartButton = true;

		if (chbDocArchiving.isSelected()) {
			lblDocArchiveDir.setForeground(Color.BLACK);
			txtDocArchiveDir.setEditable(true);
			btnDocFileSelect.setEnabled(true);

			enableStateOfStartButton = isDocingArchiveAvailable();
		} else {
			lblDocArchiveDir.setForeground(Color.LIGHT_GRAY);
			txtDocArchiveDir.setEditable(false);
			btnDocFileSelect.setEnabled(false);

			enableStateOfStartButton = true;
		}

		startDocingButton.setEnabled(enableStateOfStartButton);
		btnDocingReset.setEnabled(!defaultDocNameMode);
	}
	
	private boolean isDocingArchiveAvailable()
	{
		final File file = new File(txtDocArchiveDir.getText());
		return file.exists() && file.isDirectory();
	}

	@Override
	protected void reset() {
		// not yet used here
	}


	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################
	
	class StartDocingActionListener implements KeyListener, ActionListener
	{
		@Override
		public void keyTyped(KeyEvent e) { }

		@Override
		public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e) 
		{
			UserDocingPanel.this.setEditablilityOfDocComponentsDueToCurrentSettings();
			if (e.getKeyCode() == KeyEvent.VK_ENTER && ! cbxDocApplication.isPopupVisible()) {
				if (e.getComponent() == startDocingButton) {
					frameDialog.closeAndApplySettings();
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			System.setProperty(SysNatConstants.SYSNAT_MODE, SysNatConstants.SysNatMode.Docing.name());
			frameDialog.closeAndApplySettings();
		}
	}
	
	class DocNameListener implements ActionListener, KeyListener
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			updateDocNameIfNecessary(e.getComponent());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateDocNameIfNecessary(e.getSource());
		}

		private void updateDocNameIfNecessary(Object eventSource)
		{
			if (eventSource == txtDocName) {
				defaultDocNameMode = false;
			} else if (eventSource == btnDocingReset) {
				defaultDocNameMode = true;
			}

			if (defaultDocNameMode) {
				txtDocName.setText(UserDocingPanel.this.buildDefaultDocName());
			}

			UserDocingPanel.this.setEditablilityOfDocComponentsDueToCurrentSettings();
		}
	}
	
	class CheckBoxDocArchivingActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			UserDocingPanel.this.setEditablilityOfDocComponentsDueToCurrentSettings();
		}
	}

	class TargetEnvDocingListener implements ActionListener, KeyListener
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			updateConfiguredTargetEnvironments();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateConfiguredTargetEnvironments();
		}

		void updateConfiguredTargetEnvironments()
		{
			String selectedTestApp = cbxDocApplication.getSelectedItem().toString();
			List<String> configuredEnvironments = executionInfo.getTestAppEnvironmentsMap().get(selectedTestApp);
			String[] modelData = configuredEnvironments.stream().toArray(String[]::new);
			cbxDocEnvironment.setModel(new DefaultComboBoxModel<String>(modelData));
		}
	}

	class FileSelectListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fileChooser = new JFileChooser(SysNatTestRuntimeUtil.getSysNatRootDir());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle(BUNDLE.getString("FILE_SELECT_DIALOG_TITLE"));
			fileChooser.showSaveDialog(UserDocingPanel.this);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtDocArchiveDir.setText(fileAsString);
		}
	}

}
