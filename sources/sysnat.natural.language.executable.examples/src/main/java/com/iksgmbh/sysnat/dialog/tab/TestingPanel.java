package com.iksgmbh.sysnat.dialog.tab;

import static com.iksgmbh.sysnat.dialog.SysNatDialog.buttonDistanceToLowerFrameEdge;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.frameHeight;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.thirdColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosThirdColumn;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
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
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.dialog.BasicTabPanel;
import com.iksgmbh.sysnat.dialog.ConfigFileParser.ConfigData;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

public class TestingPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private JLabel lblTestReportArchiveDir;
	private JTextField txtTestReportName, txtTestReportArchiveDir;
	private JComboBox<String> cbxTestApplication, cbxTestEnvironment, cbxExecSpeeds, cbxBrowsers, cbxFilters;
	private JButton startTestingButton, btnTestingReset, btnTestReportFileSelect;
	private JCheckBox chbTestReportArchiving;

	private boolean defaultTestReportNameMode = true;
	private String[] configuredExecutionFilters;

	private ApplicationNameListener applicationNameListener = new ApplicationNameListener();
	private TestReportNameListener testReportNameListener = new TestReportNameListener();
	private StartTestingActionListener startTestingActionListener = new StartTestingActionListener();

	
	public TestingPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initTestingComponents();
		defaultTestReportNameMode = checkTestReportName();
		setEditablilityOfTestReportComponentsDueToCurrentSettings();
	}

	private void initTestingComponents()
	{
		yPos = 10;
		initSectionLabel(this, "TESTING_SECTION_1_LABEL", yPos);
		initComboTestApplication();
		initComboTargetEnvironment();
		initComboExecutionFilter();
		
		yPos += deltaY;
		sysnatDialog.insertSeparationLine(this, yPos);

		yPos += deltaY/2;
		initSectionLabel(this, "TESTING_SECTION_2_LABEL", yPos);
		initComboBrowser();
		initComboExecSpeed();

		yPos += deltaY;
		sysnatDialog.insertSeparationLine(this, yPos);

		yPos += deltaY/2;
		initSectionLabel(this, "TESTING_SECTION_3_LABEL", yPos);
		initTextFieldTestReportName();
		initReportNameResetButton();
		initChechboxArchiving();
		initTextFieldArchiveDirectory();

		// button line
		initCancelButton(this);
		initSaveButton(this);
		initTestingStartButton(this);
	}
	
	public void setSettingsToExecutionInfo()
	{
		// section 1 values
		executionInfo.setTestApplicationName(cbxTestApplication.getSelectedItem().toString());
		executionInfo.setTestEnvironmentName(cbxTestEnvironment.getSelectedItem().toString());

		String filterString = cbxFilters.getSelectedItem().toString();
		executionInfo.setExecutionFilterList(SysNatStringUtil.getExecutionFilterAsList(filterString, ""));
		executionInfo.setTestExecutionFilter(filterString);

		// section 2 values
		executionInfo.setTestBrowserName(cbxBrowsers.getSelectedItem().toString());
		executionInfo.setTestExecutionSpeedName(cbxExecSpeeds.getSelectedItem().toString());

		// section 3 values
		executionInfo.setTestReportName(txtTestReportName.getText().trim());
		executionInfo.setTestArchiveDir(txtTestReportArchiveDir.getText().trim());
		executionInfo.setArchiveTestReport(chbTestReportArchiving.isSelected());
	}
	
	public void saveCurrentSettings()
	{
		final List<String> oldFileContent = SysNatFileUtil.readTextFile(configFileName);
		final StringBuffer newFileContent = new StringBuffer();

		for (String line : oldFileContent) 
		{
			line = line.trim();
			if (line.startsWith(SysNatConstants.TEST_APPLICATION_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_APPLICATION_SETTING_KEY + " = "
				        + cbxTestApplication.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY + " = "
				        + cbxTestEnvironment.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY + " = " 
			            + cbxFilters.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.TEST_BROWSER_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_BROWSER_SETTING_KEY + " = " 
			            + cbxBrowsers.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY + " = "
				        + cbxExecSpeeds.getSelectedItem().toString());
			} else if (line.startsWith(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY + " = " 
			            + txtTestReportName.getText());
			} else if (line.startsWith(SysNatConstants.TEST_ARCHIVE_DIR_SETTING_KEY)) {
				newFileContent.append(SysNatConstants.TEST_ARCHIVE_DIR_SETTING_KEY + " = " 
			            + txtTestReportArchiveDir.getText());
			} else {
				newFileContent.append(line);
			}
			newFileContent.append(System.getProperty("line.separator"));
		}

		// System.out.println(newFileContent.toString().trim());
		SysNatFileUtil.writeFile(configFileName, newFileContent.toString().trim());
	}

	protected void initTestingStartButton(JPanel parent)
	{
		startTestingButton = new JButton(BUNDLE.getString("TESTING_START_BUTTON_TEXT"));
		startTestingButton.setToolTipText(BUNDLE.getString("TESTING_START_BUTTON_TOOLTIP"));
		startTestingButton.addActionListener(startTestingActionListener);
		startTestingButton.addKeyListener(startTestingActionListener);
		
		final int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		final int buttonWidth = 200;
		final int xPos = xPosThirdColumn + thirdColumnLength - buttonWidth;

		startTestingButton.setBounds(xPos, yPos, buttonWidth, 30);
		startTestingButton.setFont(buttonFont);
		startTestingButton.setBackground(Color.GREEN);
		startTestingButton.setForeground(Color.BLACK);

		parent.add(startTestingButton);
	}

	private void initTextFieldArchiveDirectory()
	{
		yPos += deltaY;
		lblTestReportArchiveDir = initLabel(this, "TESTARCHIVE_DIR_DIALOG_LABEL", yPos, NO_TOOLTIP);
		txtTestReportArchiveDir = initTextField(this, yPos, executionInfo.getTestArchiveDir(), 
				                                executionInfo.getTestArchiveDir());
		btnTestReportFileSelect = initButton(this, "FILE_SELECT_BUTTON_TEXT", yPos, null);
		btnTestReportFileSelect.addActionListener(new FileSelectListener());
	}


	private void initChechboxArchiving()
	{
		yPos += deltaY;
		chbTestReportArchiving = initCheckbox(this, BUNDLE.getString("WITH_ARCHIVING"), 
				                    executionInfo.isTestReportToArchive(), yPos);
		chbTestReportArchiving.addActionListener(new CheckBoxTestReportArchivingActionListener());
	}


	private void initTextFieldTestReportName()
	{
		yPos += deltaY;
		initLabel(this, "REPORT_NAME_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String testReportName = getTestReportName();
		txtTestReportName = initTextField(this, yPos, testReportName, testReportName);
		txtTestReportName.addKeyListener(testReportNameListener);
	}


	private String getTestReportName()
	{
		String toReturn = executionInfo.getTestReportName();
		
		if (toReturn == null) {
			return null;
		}
		
		if (toReturn.isEmpty()) {
			toReturn = buildDefaultTestReportName();
		}

		if (toReturn.endsWith("-")) {
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}
		
		return toReturn;
	}


	private void initReportNameResetButton()
	{
		btnTestingReset = initButton(this, "RESET_BUTTON_TEXT", yPos, BUNDLE.getString("RESET_BUTTON_TOOLTIP"));
		btnTestingReset.addActionListener(testReportNameListener);
	}
	

	private void initComboExecSpeed()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_BROWSER_SETTING_KEY);
		initLabel(this, "BROWSER_DIALOG_LABEL", yPos, configData.tooltip);
		cbxBrowsers = initCombo(this, configData.getPossibleValuesArray(), yPos,
		                        executionInfo.getTestBrowserTypeName(), configData.tooltip);
	}

	private void initComboBrowser()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY);
		initLabel(this, "EXECUTION_SPEED_DIALOG_LABEL", yPos, configData.tooltip);
		cbxExecSpeeds = initCombo(this, configData.getPossibleValuesArray(), yPos,
		                          executionInfo.getTestExecutionSpeedName(), configData.tooltip);
	}

	private void initComboExecutionFilter()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY);
		initLabel(this, "EXECUTION_FILTER_DIALOG_LABEL", yPos, configData.tooltip);
		String[] options = getAvailableFilters(configData);
		cbxFilters = initCombo(this, options, yPos,
		                       executionInfo.getTestExecutionFilter(), configData.tooltip, true);
		cbxFilters.addKeyListener(testReportNameListener);
	}

	private String[] getAvailableFilters(ConfigData configData)
	{
		if (configuredExecutionFilters == null ) {
			configuredExecutionFilters = configData.getPossibleValuesArray();
		}
		String testApplicationName = cbxTestApplication.getSelectedItem().toString();
		
		List<String> possibleFilters = new ArrayList<>();
		for (String value : configuredExecutionFilters) 
		{
			if (value.startsWith(testApplicationName+":")) {
				int lengthOfPrefix = testApplicationName.length() + 1;
				possibleFilters.add(value.substring(lengthOfPrefix).trim());
			}
		}
		
		if ( ! possibleFilters.contains(SysNatConstants.NO_FILTER)) {
			possibleFilters.add(0, SysNatConstants.NO_FILTER);
		}

		String[] toReturn = new String[possibleFilters.size()];
		int i = 0;
		for (String filter : possibleFilters) {
			toReturn[i++] = filter;
		}
		
		return toReturn;
	}


	private void initComboTargetEnvironment()
	{
		yPos += deltaY;
		String tooltip = configDataMap.get(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY).tooltip;
		initLabel(this, "TESTENV_DIALOG_LABEL", yPos, tooltip);
		List<String> knownEnvironments = executionInfo.getKnownEnvironments(executionInfo.getTestApplicationName());
		String[] options = knownEnvironments.toArray(new String[knownEnvironments.size()]);
		cbxTestEnvironment = initCombo(this, options, yPos,
		                                   executionInfo.getTestEnvironmentName(), tooltip);
		cbxTestEnvironment.addActionListener(testReportNameListener);
		applicationNameListener.updateConfiguredTargetEnvironments();
	}


	private void initComboTestApplication()
	{
		yPos += deltaY;
		String tooltip = configDataMap.get(SysNatConstants.TEST_APPLICATION_SETTING_KEY).tooltip;
		initLabel(this, "TESTAPP_DIALOG_LABEL", yPos, tooltip);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxTestApplication = initCombo(this, options, yPos,
		                               executionInfo.getTestApplicationName(), tooltip);
		cbxTestApplication.addActionListener(testReportNameListener);
		cbxTestApplication.addActionListener(applicationNameListener);
	}


	public String buildDefaultTestReportName()
	{
		return executionInfo.buildDefaultReportName(cbxTestApplication.getSelectedItem().toString(),
				                                    cbxTestEnvironment.getSelectedItem().toString(),
				                                    cbxFilters.getSelectedItem().toString());
	}

	/**
	 * Checks whether report name matches the other selected settings
	 */
	private boolean checkTestReportName()
	{
		final String defaultFromGUI = buildDefaultTestReportName();
		final String defaultFromFile = getTestReportName();
		return defaultFromFile.equals(defaultFromGUI);
	}
	
	
	private void setEditablilityOfTestReportComponentsDueToCurrentSettings()
	{
		boolean enableStateOfStartButton = true;

		if (chbTestReportArchiving.isSelected()) {
			lblTestReportArchiveDir.setForeground(Color.BLACK);
			txtTestReportArchiveDir.setEditable(true);
			btnTestReportFileSelect.setEnabled(true);

			enableStateOfStartButton = isTestingArchiveAvailable();
		} else {
			lblTestReportArchiveDir.setForeground(Color.LIGHT_GRAY);
			txtTestReportArchiveDir.setEditable(false);
			btnTestReportFileSelect.setEnabled(false);

			enableStateOfStartButton = true;
		}

		startTestingButton.setEnabled(enableStateOfStartButton);
		btnTestingReset.setEnabled(!defaultTestReportNameMode);
	}

	private boolean isTestingArchiveAvailable()
	{
		final File file = new File(txtTestReportArchiveDir.getText());
		return file.exists() && file.isDirectory();
	}

	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################

	
	class StartTestingActionListener implements KeyListener, ActionListener
	{
		@Override
		public void keyTyped(KeyEvent e) { }

		@Override
		public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e) 
		{
			TestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !cbxTestApplication.isPopupVisible()) {
				if (e.getComponent() == startTestingButton) {
					sysnatDialog.closeAndApplySettings();
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			System.setProperty(SysNatConstants.SYSNAT_MODE, SysNatConstants.SysNatMode.Testing.name());
			sysnatDialog.closeAndApplySettings();
		}
	}

	class TestReportNameListener implements ActionListener, KeyListener
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
			updateReportNameIfNecessary(e.getComponent());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateReportNameIfNecessary(e.getSource());
		}

		private void updateReportNameIfNecessary(Object eventSource)
		{
			if (eventSource == txtTestReportName) {
				defaultTestReportNameMode = false;
			} else if (eventSource == btnTestingReset) {
				defaultTestReportNameMode = true;
			}

			if (defaultTestReportNameMode) {
				txtTestReportName.setText(TestingPanel.this.buildDefaultTestReportName());
			}

			TestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
		}
	}

	class CheckBoxTestReportArchivingActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			TestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
		}
	}

	class ApplicationNameListener implements ActionListener, KeyListener
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
			updateKnownExecutionFilters();
		}

		private void updateKnownExecutionFilters()
		{
			ConfigData configData = configDataMap.get(SysNatConstants.TEST_BROWSER_SETTING_KEY);
			String[] options = getAvailableFilters(configData);
			cbxFilters.setModel(new DefaultComboBoxModel<String>(options));
		}

		void updateConfiguredTargetEnvironments()
		{
			String selectedTestApp = cbxTestApplication.getSelectedItem().toString();
			List<String> configuredEnvironments = executionInfo.getTestAppEnvironmentsMap().get(selectedTestApp);
			String[] modelData = configuredEnvironments.stream().toArray(String[]::new);
			cbxTestEnvironment.setModel(new DefaultComboBoxModel<String>(modelData));
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
			fileChooser.showSaveDialog(TestingPanel.this);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtTestReportArchiveDir.setText(fileAsString);
		}
	}

}
