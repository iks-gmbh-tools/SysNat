package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.assertj.core.util.Arrays;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.dialog.helper.ConfigFileParser.ConfigData;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

public class UserTestingPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private JLabel lblBrowser;
	private JLabel lblTestReportArchiveDir;
	private JTextField txtTestReportName, txtTestReportArchiveDir;
	private JComboBox<String> cbxTestApplication, cbxTestEnvironment, cbxExecSpeeds, cbxBrowsers, cbxFilters;
	private JButton startTestingButton, btnTestingReset, btnTestReportFileSelect;
	private JCheckBox chbTestReportArchiving;

	private boolean defaultTestReportNameMode = true;
	private String[] configuredExecutionFilters;

	private ApplicationNameListener applicationNameListener = new ApplicationNameListener();
	private TestReportNameListener testReportNameListener = new TestReportNameListener();
	private ShowHideBrowserComboListener showHideBrowserComboListener = new ShowHideBrowserComboListener();
	private StartTestingActionListener startTestingActionListener = new StartTestingActionListener();

	private JCheckBox chbKeep;

	private JCheckBox chbRemove;

	public UserTestingPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initTestingComponents();
		defaultTestReportNameMode = checkTestReportName();
		setEditablilityOfTestReportComponentsDueToCurrentSettings();
		showHideBrowserCombo();
	}

	private void initTestingComponents()
	{
		yPos = 10;
		createSectionLabel(this, "TESTING_SECTION_1_LABEL", yPos);
		initComboTestApplication();
		initComboTargetEnvironment();
		initComboExecutionFilter();
		initCheckboxesExecutionFilter();
		
		yPos += deltaY;
		createSeparationLine(this, yPos);

		yPos += deltaY/2;
		createSectionLabel(this, "TESTING_SECTION_2_LABEL", yPos);
		initComboExecSpeed();
		initComboBrowser();

		yPos += deltaY;
		createSeparationLine(this, yPos);

		yPos += deltaY/2;
		createSectionLabel(this, "TESTING_SECTION_3_LABEL", yPos);
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
		String testApplicationName = cbxTestApplication.getSelectedItem().toString();
		executionInfo.setTestApplicationName(testApplicationName);
		String env = toEnvSysNatEnum(cbxTestEnvironment.getSelectedItem().toString(), testApplicationName);
		executionInfo.setTestEnvironmentName(env);

		String filterString = ((JTextField)cbxFilters.getEditor().getEditorComponent()).getText();
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
				        + toEnvSysNatEnum(cbxTestEnvironment.getSelectedItem().toString(), 
				        		          cbxTestApplication.getSelectedItem().toString()));
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
		updateExecutionFilterOptions();
	}

	private void updateExecutionFilterOptions()
	{
		String selectedItem = cbxFilters.getSelectedItem().toString();
		List<String> options = null;
		boolean resetCurrentOption = false;
		
		if (chbKeep.isVisible() && chbKeep.isSelected()) {
			options = getOptions(cbxFilters, null);
			options.add(selectedItem);
			resetCurrentOption = true;
		} 
		
		if (chbRemove.isVisible() && chbRemove.isSelected()) {
			options = getOptions(cbxFilters, selectedItem);
		}
		
		// Collections.sort(options);
		if (options != null) {
			cbxFilters.removeAllItems();
			options.forEach(option -> cbxFilters.addItem(option));		
		}
		if (resetCurrentOption) cbxFilters.setSelectedItem(selectedItem);
	}

	private List<String> getOptions(JComboBox<String> comboBox, String toIgnore)
	{
		List<String> newOptions = new ArrayList<>();

		for (int i = 0; i < comboBox.getItemCount(); i++) 
		{
			String option = comboBox.getItemAt(i);
			if (! option.equals(toIgnore)) {
				newOptions.add(option);
			}
		}
		
		return newOptions;
	}

	private void initTestingStartButton(JPanel parent) {
		startTestingButton = initStartButton(parent, 
				                             BUNDLE.getString("TESTING_START_BUTTON_TEXT"), 
				                             BUNDLE.getString("TESTING_START_BUTTON_TOOLTIP"), 
				                             startTestingActionListener);
	}

	private void initTextFieldArchiveDirectory()
	{
		yPos += deltaY;
		lblTestReportArchiveDir = initLabel(this, "TESTARCHIVE_DIR_DIALOG_LABEL", yPos, NO_TOOLTIP);
		txtTestReportArchiveDir = initTextField(this, yPos, executionInfo.getTestArchiveDir(), NO_TOOLTIP, "txtTestReportArchiveDir");
		txtTestReportArchiveDir.setSize(new Dimension(600, SysNatDialog.lineHeight));
		btnTestReportFileSelect = initButton(this, "FILE_SELECT_BUTTON_TEXT", yPos, null);
		btnTestReportFileSelect.addActionListener(new FileSelectListener());
	}


	private void initChechboxArchiving()
	{
		yPos += deltaY;
		chbTestReportArchiving = initCheckbox(this, BUNDLE.getString("WITH_ARCHIVING"), null, 
				                    executionInfo.isTestReportToArchive(), SysNatDialog.xPosFirstColumn, yPos);
		chbTestReportArchiving.addActionListener(new CheckBoxTestReportArchivingActionListener());
	}


	private void initTextFieldTestReportName()
	{
		yPos += deltaY;
		initLabel(this, "REPORT_NAME_DIALOG_LABEL", yPos, NO_TOOLTIP);
		String testReportName = getTestReportName();
		txtTestReportName = initTextField(this, yPos, testReportName, NO_TOOLTIP, "txtTestReportName");
		txtTestReportName.setSize(new Dimension(600, SysNatDialog.lineHeight));
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
	

	private void initComboBrowser()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_BROWSER_SETTING_KEY);
		lblBrowser = initLabel(this, "BROWSER_DIALOG_LABEL", yPos, configData.tooltip);
		cbxBrowsers = initCombo(this, configData.getPossibleValuesArray(), yPos,
		                        executionInfo.getTestBrowserTypeName(), configData.tooltip, "cbxBrowsers");
	}

	private void initComboExecSpeed()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY);
		initLabel(this, "EXECUTION_SPEED_DIALOG_LABEL", yPos, configData.tooltip);
		cbxExecSpeeds = initCombo(this, configData.getPossibleValuesArray(), yPos,
		                          executionInfo.getTestExecutionSpeedName(), configData.tooltip, "cbxExecSpeeds");
	}

	private void initComboExecutionFilter()
	{
		yPos += deltaY;
		ConfigData configData = configDataMap.get(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY);
		initLabel(this, "EXECUTION_FILTER_DIALOG_LABEL", yPos, configData.tooltip);
		String[] options = getAvailableFilters(configData);
		cbxFilters = initCombo(this, options, yPos, executionInfo.getTestExecutionFilter(), configData.tooltip, true, LinePosition.SINGLE, "cbxFilters");
		cbxFilters.setSize(new Dimension(600, SysNatDialog.lineHeight));
		cbxFilters.getEditor().getEditorComponent().addKeyListener(testReportNameListener);
		cbxFilters.addActionListener(testReportNameListener);
		((JTextField)cbxFilters.getEditor().getEditorComponent()).addFocusListener(new FocusListener() 
		{
			@Override public void focusLost(FocusEvent e) {
				Object selection = cbxFilters.getSelectedItem();
				if (selection != null && selection.toString().equals("")) {
					cbxFilters.setSelectedItem("-");
				}
			}
			
			@Override public void focusGained(FocusEvent e) {
				JTextField txt = (JTextField)cbxFilters.getEditor().getEditorComponent();
				if (txt.getText().toString().equals("-")) {
					txt.setText("");
				}			
			}
		});
	}
	
	private void initCheckboxesExecutionFilter() 
	{
		chbKeep = initCheckbox(this, "Keep", "Keep in option list", true, SysNatDialog.xPosThirdColumn, yPos);
		chbRemove = initCheckbox(this, "Remove", "Remove from option list", false, SysNatDialog.xPosThirdColumn, yPos);
		checkOptionList();
	}
	
	public void updateFilterOptions()
	{
		String currentOption = ((JTextField)cbxFilters.getEditor().getEditorComponent()).getText();
		if (chbKeep.isVisible()) {
			if (chbKeep.isSelected()) {
				updateFilterOptionList(currentOption);
			}
		} else {
			if (chbRemove.isSelected()) {
				updateFilterOptionList(currentOption);
			}
		}
	}
	

	private void updateFilterOptionList(String currentOption)
	{
		List<String> options = new ArrayList<>();
		int num = cbxFilters.getItemCount();
		for (int i = 0; i<num; i++) {
			options.add(cbxFilters.getItemAt(i));
		}

		if (options.contains(currentOption)) {
			options.remove(currentOption);
		} else {
			options.add(currentOption);
		}
		
		updateFilterOptionsInTestingConfig(options);
	}

	private void updateFilterOptionsInTestingConfig(List<String> options)
	{
		String currentApplication = cbxTestApplication.getSelectedItem().toString();
		List<String> content = SysNatFileUtil.readTextFile(configFileName);
		List<String> newContent = new ArrayList<>();
		for (String line : content) 
		{
			if (line.startsWith("# " + currentApplication + ": ")) {
				line = "# " + currentApplication + ": " + SysNatStringUtil.listToString(options, ",");
			}
			newContent.add(line);
		}
		
		SysNatFileUtil.writeFile(new File(configFileName), newContent);
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
		knownEnvironments = createDisplayList(knownEnvironments, executionInfo.getTestApplication());
		String[] options = knownEnvironments.toArray(new String[knownEnvironments.size()]);
		String initialValue = toEnvSpecificToTestApplication(executionInfo.getTestEnvironmentName(), executionInfo.getTestApplicationName());
		cbxTestEnvironment = initCombo(this, options, yPos, initialValue, tooltip, "cbxTestEnvironment");
		cbxTestEnvironment.addActionListener(testReportNameListener);
		//applicationNameListener.updateConfiguredTargetEnvironments();  why hier ???
	}

	private List<String> createDisplayList(List<String> knownEnvironments, TestApplication testApplication)
	{
		List<Object> order = Arrays.asList(SysNatConstants.TargetEnvironment.values());
		return order.stream().map(env -> env.toString())
				    .filter(env -> knownEnvironments.contains(env))
				    .map(env -> executionInfo.getDisplayName(env, testApplication))
				    .collect(Collectors.toList());

	}

	private void initComboTestApplication()
	{
		yPos += deltaY;
		String tooltip = configDataMap.get(SysNatConstants.TEST_APPLICATION_SETTING_KEY).tooltip;
		initLabel(this, "TESTAPP_DIALOG_LABEL", yPos, tooltip);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxTestApplication = initCombo(this, options, yPos,
		                               executionInfo.getTestApplicationName(), tooltip, "TestingPanel_cbxTestApplication");
		cbxTestApplication.addActionListener(testReportNameListener);
		cbxTestApplication.addActionListener(applicationNameListener);
		cbxTestApplication.addActionListener(showHideBrowserComboListener);
	}


	public String buildDefaultTestReportName()
	{
		String s1 = "";
		if (cbxTestApplication.getSelectedItem() != null) s1 = cbxTestApplication.getSelectedItem().toString();  

		String s2 = "";
		if (cbxTestEnvironment.getSelectedItem() != null) s2 = cbxTestEnvironment.getSelectedItem().toString();  
		
		String s3 = "";
		if (cbxFilters.getSelectedItem() != null) s3 = ((JTextField)cbxFilters.getEditor().getEditorComponent()).getText();  
		
		return executionInfo.buildDefaultReportName(s1, s2, s3);
	}
	
	public JButton getStartButton() {
		return startTestingButton;
	}

	/**
	 * Checks whether report name matches the other selected settings
	 */
	private boolean checkTestReportName()
	{
		final String defaultFromGUI = buildDefaultTestReportName();
		final String defaultFromFile = getTestReportName();
		if (! defaultFromFile.equals(defaultFromGUI)) {
			executionInfo.setTestReportName(defaultFromGUI);
			txtTestReportName.setText(defaultFromGUI);
		}
		return true;
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
	
	private void showHideBrowserCombo()
	{
		String selecetedApp = (String) cbxTestApplication.getSelectedItem();
		boolean visible = new TestApplication(selecetedApp).isWebApplication();
		cbxBrowsers.setVisible(visible);
		lblBrowser.setVisible(visible);
	}

	private void checkOptionList()
	{
		String text = ((JTextField)cbxFilters.getEditor().getEditorComponent()).getText();
		if ("-".equals(text.trim()) || "".equals(text.trim())) {
			chbKeep.setVisible(false);
			chbRemove.setVisible(false);
			chbKeep.setSelected(true);			
			chbRemove.setSelected(false);
		}
		else if (containsOption(cbxFilters, text)) 
		{
			chbRemove.setVisible(true);
			chbRemove.setSelected(false);
			chbKeep.setVisible(false);
		} else {
			chbRemove.setVisible(false);
			chbKeep.setVisible(true);
			chbKeep.setSelected(false);
		}
	}
	
	private boolean containsOption(JComboBox<String> cbx, String text)
	{
		int num = cbx.getItemCount();
		for (int i = 0; i<num; i++) {
			if (cbx.getItemAt(i).toString().equals(text)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void reset() {
		// not yet used here
	}

	protected int getFirstColumnLength() {
		return 350;
	}
	
	protected int getSecondColumnLength() {
		return 350;
	}
	
	protected int getThirdColumnLength() {
		return 350;
	}

	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################

	
	class ShowHideBrowserComboListener implements ActionListener, KeyListener
	{
		@Override public void keyTyped(KeyEvent e) { }
		@Override public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e) {
			showHideBrowserCombo();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showHideBrowserCombo();
		}	
	}
	
	class StartTestingActionListener implements KeyListener, ActionListener
	{
		@Override
		public void keyTyped(KeyEvent e) { }

		@Override
		public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e) 
		{
			UserTestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !cbxTestApplication.isPopupVisible()) {
				if (e.getComponent() == startTestingButton) {
					run();
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			run();
		}

		private void run()
		{
			System.setProperty(SysNatConstants.SYSNAT_MODE, SysNatConstants.SysNatMode.Testing.name());
			frameDialog.closeAndApplySettings();
		}
	}

	class TestReportNameListener implements ActionListener, KeyListener
	{
		@Override public void keyTyped(KeyEvent e) { }
		@Override public void keyPressed(KeyEvent e) { }

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
				txtTestReportName.setText(UserTestingPanel.this.buildDefaultTestReportName());
			}
			
			checkOptionList();
			UserTestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
		}
	}

	class CheckBoxTestReportArchivingActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			UserTestingPanel.this.setEditablilityOfTestReportComponentsDueToCurrentSettings();
		}
	}

	class ApplicationNameListener implements ActionListener, KeyListener
	{
		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			updateConfiguredTargetEnvironments();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
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
			List<String> knownEnvironments = executionInfo.getTestAppEnvironmentsMap().get(selectedTestApp);
			if (knownEnvironments == null) {
				knownEnvironments = new ArrayList<>();
			}
			knownEnvironments = createDisplayList(knownEnvironments, new TestApplication(selectedTestApp));
			String[] modelData = knownEnvironments.stream().toArray(String[]::new);
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
			fileChooser.showSaveDialog(UserTestingPanel.this);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtTestReportArchiveDir.setText(fileAsString);
		}
	}

}
