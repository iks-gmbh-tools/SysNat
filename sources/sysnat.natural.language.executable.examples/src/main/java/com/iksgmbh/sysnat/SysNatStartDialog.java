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
package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.NO_FILTER;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
* Graphical User Interface to ask user for configuration settings.
* It's designed to have two tabs: one for testing and one for documentation.
* 
 * The testing tab contains three sections, each consisting of two or three 
 * rows and columns of graphical elements.
* First column: a label
* Second column: a input field
* Third column: optional element (e.g. a button to reset the content of the input field)
* 
 * @author Reik Oberrath
*/
public class SysNatStartDialog extends JFrame 
{
	private static final ResourceBundle CONSTANTS_BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	private static final ResourceBundle CONSTANTS_BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SysNatStartDialog", Locale.getDefault());
	private static final long serialVersionUID = 1L;

	private static final int deltaY = 40;
	private static final String NO_TOOLTIP = "NO_TOOLTIP";

	private final Font tabFont = new Font("Arial", Font.BOLD, 20);
	private final Font labelFont = new Font("Arial", Font.BOLD, 16);
	private final Font buttonFont = new Font("Arial", Font.BOLD, 16);
	private final Font labelSectionFont = new Font("Arial", Font.ITALIC, 18);
	private final Font fieldFont = new Font("Arial", Font.PLAIN, 18);

	private final int buttonDistanceToLowerFrameEdge = 130;
	private final int firstColumnLength = 350;
	private final int secondColumnLength = 350;
	private final int thirdColumnLength = 100;
	private final int xPosFirstColumn = 10;
	private final int columnDistance = 10;
	private final int xPosSecondColumn = xPosFirstColumn + firstColumnLength;
	private final int xPosThirdColumn = xPosSecondColumn + secondColumnLength + columnDistance;
	private final int frameWidth = xPosThirdColumn + thirdColumnLength + 50;
	private final int frameHeight = 600;

	private ExecutionRuntimeInfo executionInfo;
	private StartTestingActionListener startTestingActionListener = new StartTestingActionListener();
	private StartDocingActionListener startDocingActionListener = new StartDocingActionListener();
	private ReportNameListener reportNameListener = new ReportNameListener();
	private TargetEnvListener targetEnvListener = new TargetEnvListener();
	private int yPos = 10;
	private boolean defaultReportNameMode = true;

	private List<String> knownEnvironments = new ArrayList<>();
	private List<String> knownFilters = new ArrayList<>();
	private List<String> knownTestApplications = new ArrayList<>();
	private List<String> knownBrowsers = new ArrayList<>();
	private List<String> knownExecSpeeds = new ArrayList<>();
	private HashMap<String, List<TargetEnv>> testAppEnvironmentsMap;

	private JPanel testingParentPanel;
	private JPanel docingParentPanel;
	private JLabel lblArchiveDir;
	private JTextField txtReportName, txtArchiveDir;
	private JComboBox<String> cbxTestApplication, cbxEnvironments, cbxExecSpeeds, cbxBrowsers, cbxFilters;
	private JComboBox<String> cbxDocApplication;
	private JButton startButton, btnReset, btnFileSelect;
	private JCheckBox chbArchiving, chbSystemDescription, chbRequirementStatusInformation;
	private String executionFilterToolTipText;

	public SysNatStartDialog() throws Exception
	{
		init();
	}

	protected void init() throws Exception
	{
		UIManager.put("ToolTip.font", new FontUIResource("SansSerif", Font.PLAIN, 16));
		UIManager.setLookAndFeel(new MyLookAndFeel());
		ToolTipManager.sharedInstance().setInitialDelay(10);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		executionInfo = ExecutionRuntimeInfo.getInstance();
		analyseConfigSettings();
		testAppEnvironmentsMap = readConfiguredEnvironments();
		setMinimumSize(new Dimension(frameWidth, frameHeight));
		setTitle("SysNat Start Dialog");
		setLocationRelativeTo(null);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, frameWidth, frameHeight);
		getContentPane().add(tabbedPane);
		initComponents(tabbedPane);
		defaultReportNameMode = checkReportName();
		setEditablilityOfComponentsDueToCurrentSettings();
		setVisible(true);
	}

	private HashMap<String, List<TargetEnv>> readConfiguredEnvironments()
	{
		final HashMap<String, List<TargetEnv>> toReturn = new HashMap<>();

		knownTestApplications.forEach(testApp -> addConfiguredEnvironments(testApp, toReturn));

		return toReturn;
	}

	void addConfiguredEnvironments(String testApp, HashMap<String, List<TargetEnv>> environmentMap)
	{
		final File propertiesFile = new File(System.getProperty("sysnat.properties.path"), testApp + ".properties");
		final List<TargetEnv> configuredEnvironments = new ArrayList<>();
		final List<String> lines = SysNatFileUtil.readTextFile(propertiesFile);

		lines.forEach(line -> addIfLineConfiguresAnEnvironment(line.trim(), configuredEnvironments, testApp));
		environmentMap.put(testApp, configuredEnvironments);
	}

	private void addIfLineConfiguresAnEnvironment(String line, List<TargetEnv> configuredEnvironments, String testApp)
	{
		if (line.startsWith(testApp.toLowerCase())) {
			int pos = line.indexOf('.');
			String environment = line.substring(pos + 1);
			pos = environment.indexOf('.');
			environment = environment.substring(0, pos);
			List<String> knownEnvironments = Arrays.asList(TargetEnv.values()).stream()
			        .map(enumValue -> enumValue.name()).collect(Collectors.toList());

			if (knownEnvironments.contains(environment.toUpperCase())) {
				TargetEnv env = TargetEnv.valueOf(environment.toUpperCase());
				if (!configuredEnvironments.contains(env)) {
					configuredEnvironments.add(env);
				}
			} else {
				System.err.println("WARNING: For '" + testApp + "' the unknown environment '" + environment
				        + "' has been configured." + " Known environments are "
				        + SysNatStringUtil.listToString(knownEnvironments, ","));
			} ;
		}
	}

	private void analyseConfigSettings()
	{
		List<String> possibleValues = new ArrayList<>();
		List<String> possibleValues_en = new ArrayList<>();
		StringBuffer tooltip = getNewTookTipStringBuffer();

		final String toolTipIdentifier = "Tooltip";
		final List<String> contentOfConfigSettingsFile = executionInfo.getContentOfConfigSettingsFile();

		boolean possibleValueLineDetected = false;
		boolean possibleValueLineDetected_en = false;

		for (String line : contentOfConfigSettingsFile) {
			if (line.contains(SysNatLocaleConstants.POSSIBLE_VALUE_IDENTIFIER)) {
				possibleValueLineDetected = true;
				continue;
			}

			if (possibleValueLineDetected) {
				String valueLine = line.substring(1).trim();
				possibleValues.addAll(Arrays.asList(valueLine.split(",")));
				possibleValueLineDetected = false;
				continue;
			}

			if (line.startsWith("# " + CONSTANTS_BUNDLE_EN.getString("POSSIBLE_VALUE_IDENTIFIER"))) {
				possibleValueLineDetected_en = true;
				continue;
			}

			if (possibleValueLineDetected_en) {
				String valueLine = line.substring(1).trim();
				possibleValues_en.addAll(Arrays.asList(valueLine.split(",")));
				possibleValueLineDetected_en = false;
				continue;
			}

			if (line.contains(toolTipIdentifier)) {
				String toolTipLine = line.substring(toolTipIdentifier.length()).trim();
				tooltip.append(toolTipLine).append("<br>");
				continue;
			}

			if (isEnvironmentSetting(line)) {
				knownEnvironments.addAll(trim(possibleValues));
				possibleValues = new ArrayList<>();
				tooltip = getNewTookTipStringBuffer();
			} else if (isTestAppSetting(line)) {
				knownTestApplications.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			} else if (isBrowserSetting(line)) {
				knownBrowsers.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			} else if (isExecutionSpeed(line)) {
				knownExecSpeeds.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"))) {
				knownExecSpeeds.addAll(trim(possibleValues_en));
				possibleValues_en.clear();
				tooltip = getNewTookTipStringBuffer();
			} else if (line.startsWith(SysNatLocaleConstants.EXECUTION_FILTER)) {
				knownFilters.add("-");
				knownFilters.addAll(trim(possibleValues));
				executionFilterToolTipText = tooltip.append("</html>").toString();
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			}
		}
	}

	private boolean isExecutionSpeed(String line)
	{
		return line.startsWith(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY)
		        || line.startsWith(CONSTANTS_BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY"))
		        || line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"));
	}

	private boolean isBrowserSetting(String line)
	{
		return line.startsWith(SysNatLocaleConstants.BROWSER_SETTING_KEY)
		        || line.startsWith(CONSTANTS_BUNDLE.getString("BROWSER_SETTING_KEY"))
		        || line.startsWith(CONSTANTS_BUNDLE_EN.getString("BROWSER_SETTING_KEY"));
	}

	private boolean isEnvironmentSetting(String line)
	{
		return line.startsWith(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY)
		        || line.startsWith(CONSTANTS_BUNDLE.getString("ENVIRONMENT_SETTING_KEY"))
		        || line.startsWith(CONSTANTS_BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY"));
	}

	private boolean isTestAppSetting(String line)
	{
		return line.startsWith(SysNatLocaleConstants.TESTAPP_SETTING_KEY)
		        || line.startsWith(CONSTANTS_BUNDLE.getString("TESTAPP_SETTING_KEY"))
		        || line.startsWith(CONSTANTS_BUNDLE_EN.getString("TESTAPP_SETTING_KEY"));
	}

	private StringBuffer getNewTookTipStringBuffer()
	{
		return new StringBuffer("<html>");
	}

	private List<String> trim(final List<String> input)
	{
		return input.stream().map(s -> s.trim()).collect(Collectors.toList());
	}

	private void initComponents(JTabbedPane tabbedPane)
	{
		tabbedPane.setFont(tabFont);

		testingParentPanel = new JPanel(null);
		tabbedPane.addTab("Testing", null, testingParentPanel, "Start XX as tests...");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_T);
		initTestingComponents(testingParentPanel);

		docingParentPanel = new JPanel(null);
		tabbedPane.addTab("Docing", null, docingParentPanel, "Start generation of documentation...");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_D);
		initDocingComponents(docingParentPanel);
	}

	private void initDocingComponents(JPanel parent)
	{
		int xPos = xPosSecondColumn - 150;
		yPos = 10;
		initSectionLabel(parent, "DOCING_SECTION_1_LABEL", yPos);

		yPos += deltaY;
		initLabel(parent, "DOCAPP_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxDocApplication = initCombo(parent, knownTestApplications.toArray(new String[knownTestApplications.size()]),
		        yPos, executionInfo.getTestApplicationName());
		yPos += deltaY;
		initLabel(parent, "DOCTYPE_SETTING_KEY", yPos, NO_TOOLTIP);
		chbSystemDescription = initCheckbox(parent, BUNDLE.getString("DOCTYPE_SYSTEM_DESCRIPTION"), false, xPos, yPos);

		yPos += deltaY - 15;
		chbRequirementStatusInformation = initCheckbox(parent, BUNDLE.getString("REQ_STATUS_INFO"), false, xPos, yPos);

		initCancelButton(parent);
		initSaveButton(parent);
		initDocingStartButton(parent);
	}

	private void initTestingComponents(JPanel parent)
	{
		yPos = 10;
		initSectionLabel(parent, "TESTING_SECTION_1_LABEL", yPos);

		yPos += deltaY;
		initLabel(parent, "TESTAPP_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxTestApplication = initCombo(parent, knownTestApplications.toArray(new String[knownTestApplications.size()]),
		        yPos, executionInfo.getTestApplicationName());
		cbxTestApplication.addActionListener(reportNameListener);
		cbxTestApplication.addActionListener(targetEnvListener);

		yPos += deltaY;
		initLabel(parent, "ENVIRONMENT_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxEnvironments = initCombo(parent, knownEnvironments.toArray(new String[knownEnvironments.size()]), yPos,
		        executionInfo.getTargetEnv().name());
		cbxEnvironments.addActionListener(reportNameListener);
		targetEnvListener.updateConfiguredTargetEnvironments();

		yPos += deltaY;
		initLabel(parent, "EXECUTION_FILTER", yPos, executionFilterToolTipText);
		cbxFilters = initCombo(parent, knownFilters.toArray(new String[knownFilters.size()]), yPos,
		        executionInfo.getFiltersToExecute());
		cbxFilters.setToolTipText(executionFilterToolTipText);
		cbxFilters.addKeyListener(reportNameListener);

		yPos += deltaY;
		initSectionLabel(parent, "TESTING_SECTION_2_LABEL", yPos);

		yPos += deltaY;
		initLabel(parent, "BROWSER_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxBrowsers = initCombo(parent, knownBrowsers.toArray(new String[knownBrowsers.size()]), yPos,
		        executionInfo.getBrowserTypeToUse().name());

		yPos += deltaY;
		initLabel(parent, "EXECUTION_SPEED_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxExecSpeeds = initCombo(parent, knownExecSpeeds.toArray(new String[knownExecSpeeds.size()]), yPos,
		        executionInfo.getExecutionSpeed());

		yPos += deltaY;
		initSectionLabel(parent, "TESTING_SECTION_3_LABEL", yPos);

		yPos += deltaY;
		initLabel(parent, "REPORT_NAME_SETTING_KEY", yPos, NO_TOOLTIP);
		txtReportName = initTextField(yPos, executionInfo.getReportName(), executionInfo.getReportName());
		btnReset = initButton("RESET_BUTTON_TEXT", yPos, BUNDLE.getString("RESET_BUTTON_TOOLTIP"));
		txtReportName.addKeyListener(reportNameListener);
		btnReset.addActionListener(reportNameListener);

		yPos += deltaY;
		chbArchiving = initCheckbox(parent, BUNDLE.getString("WITH_ARCHIVING"), executionInfo.areResultsToArchive(), 10,
		        yPos);
		chbArchiving.addActionListener(new CheckBoxArchivingActionListener());

		yPos += deltaY;
		lblArchiveDir = initLabel(parent, "ARCHIVE_DIR_SETTING_KEY", yPos, NO_TOOLTIP);
		txtArchiveDir = initTextField(yPos, executionInfo.getArchiveDir(), executionInfo.getArchiveDir());
		btnFileSelect = initButton("FILE_SELECT_BUTTON_TEXT", yPos, null);
		btnFileSelect.addActionListener(new FileSelectListener());

		initCancelButton(parent);
		initSaveButton(parent);
		initTestingStartButton(parent);
	}

	private JCheckBox initCheckbox(final JPanel parent,
	        final String title,
	        final boolean tickInitially,
	        final int xPos,
	        final int yPos)
	{
		JCheckBox ckeckbox = new JCheckBox(title);

		ckeckbox.setBounds(xPos, yPos, secondColumnLength, 20);
		ckeckbox.setFont(labelFont);
		ckeckbox.setSelected(tickInitially);

		parent.add(ckeckbox);
		return ckeckbox;
	}

	private void initDocingStartButton(JPanel parent)
	{
		startButton = new JButton(BUNDLE.getString("DOCING_START_BUTTON_TEXT"));

		startButton.addActionListener(startDocingActionListener);
		startButton.addKeyListener(startDocingActionListener);
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		int xPos = 510;
		startButton.setBounds(xPos, yPos, 200, 30);
		startButton.setFont(buttonFont);
		startButton.setBackground(Color.GREEN);
		startButton.setForeground(Color.BLACK);

		parent.add(startButton);
	}

	private void initTestingStartButton(JPanel parent)
	{
		startButton = new JButton(BUNDLE.getString("TESTING_START_BUTTON_TEXT"));

		startButton.addActionListener(startTestingActionListener);
		startButton.addKeyListener(startTestingActionListener);
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		int xPos = 510;
		startButton.setBounds(xPos, yPos, 200, 30);
		startButton.setFont(buttonFont);
		startButton.setBackground(Color.GREEN);
		startButton.setForeground(Color.BLACK);

		parent.add(startButton);
	}

	private void initSaveButton(JPanel parent)
	{
		final JButton saveButton = new JButton(BUNDLE.getString("SAVE_BUTTON_TEXT"));
		saveButton.setToolTipText(BUNDLE.getString("SAVE_BUTTON_TOOLTIP"));
		final SaveActionListener saveActionListener = new SaveActionListener();

		saveButton.addActionListener(saveActionListener);
		saveButton.addKeyListener(saveActionListener);
		int xPos = 245;
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		saveButton.setBounds(xPos, yPos, 180, 30);
		saveButton.setFont(labelFont);

		parent.add(saveButton);
	}

	private void initCancelButton(JPanel parent)
	{
		final JButton cancelButton = new JButton(BUNDLE.getString("CANCEL_BUTTON_TEXT"));
		final CancelActionListener cancelActionListener = new CancelActionListener();

		cancelButton.addActionListener(cancelActionListener);
		cancelButton.addKeyListener(new CancelActionListener());
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		cancelButton.setBounds(10, yPos, 140, 30);
		cancelButton.setFont(buttonFont);
		cancelButton.setBackground(Color.ORANGE);
		cancelButton.setForeground(Color.BLACK);

		parent.add(cancelButton);
	}

	private JTextField initTextField(int yPos, String initialText, String toolTipText)
	{
		final JTextField txtField = new JTextField();
		initTextField(txtField, yPos, initialText, toolTipText);
		return txtField;
	}

	@SuppressWarnings("unused")
	private JTextField initTextField(int yPos, String initialText)
	{
		return initTextField(yPos, initialText, "");
	}

	private void initTextField(final JTextField txtField,
	        final int yPos,
	        final String initialText,
	        final String toolTipText)
	{
		txtField.setBounds(xPosSecondColumn, yPos, secondColumnLength, 22);
		txtField.setFont(fieldFont);
		txtField.setText(initialText);
		txtField.setToolTipText(toolTipText);

		testingParentPanel.add(txtField);
	}

	private JComboBox<String> initCombo(JPanel parent, String[] optionList, int yPos, String initialSelection)
	{
		final JComboBox<String> cbx = new JComboBox<String>(optionList);

		cbx.setBounds(xPosSecondColumn, yPos, secondColumnLength, 26);
		cbx.setFont(fieldFont);
		cbx.setEditable(true);
		cbx.setBackground(Color.WHITE);
		cbx.setSelectedItem(initialSelection);

		parent.add(cbx);
		return cbx;
	}

	private void initSectionLabel(JPanel parent, String text, int yPos)
	{
		final JLabel lbl = new JLabel();

		lbl.setText(BUNDLE.getString(text));
		lbl.setBounds(xPosFirstColumn, yPos, firstColumnLength + secondColumnLength, 20);
		lbl.setFont(labelSectionFont);

		parent.add(lbl);
	}

	private JButton initButton(String text, int yPos, String tooltipText)
	{
		text = BUNDLE.getString(text);
		final JButton btn = new JButton(text);

		btn.setBounds(xPosThirdColumn, yPos, thirdColumnLength, 20);
		btn.setFont(buttonFont);
		if (tooltipText != NO_TOOLTIP) {
			btn.setToolTipText(tooltipText);
		}

		testingParentPanel.add(btn);
		return btn;
	}

	private JLabel initLabel(JPanel parent, String text, int yPos, String tooltipText)
	{
		final JLabel lbl = new JLabel();

		if (!text.endsWith(":")) {
			text = getBundleText(text) + ":";
		}

		lbl.setText(text);
		lbl.setBounds(xPosFirstColumn, yPos, firstColumnLength, 20);
		lbl.setFont(labelFont);
		if (tooltipText != NO_TOOLTIP) {
			lbl.setToolTipText(tooltipText);
		}

		parent.add(lbl);
		return lbl;
	}

	private String getBundleText(String key)
	{
		try {
			return CONSTANTS_BUNDLE.getString(key).replaceAll("_", " ");
		} catch (Exception e) {
			// ignore
		}

		try {
			return CONSTANTS_BUNDLE_EN.getString(key).replaceAll("_", " ");
		} catch (Exception e) {
			// ignore
		}

		try {
			return BUNDLE.getString(key).replaceAll("_", " ");
		} catch (Exception e) {
			// ignore
		}

		return null;
	}

	private void setSelectionToExecutionInfo()
	{
		// section 1 values
		executionInfo.setTestApplicationName(cbxTestApplication.getSelectedItem().toString());
		executionInfo.setTargetEnv(cbxEnvironments.getSelectedItem().toString());

		String filterString = cbxFilters.getSelectedItem().toString();
		executionInfo.setExecutionFilterList(SysNatStringUtil.getExecutionFilterAsList(filterString, ""));
		executionInfo.setExecutionFilterString(filterString);

		// section 2 values
		executionInfo.setBrowserTypeToUse(cbxBrowsers.getSelectedItem().toString());
		executionInfo.setExecSpeed(cbxExecSpeeds.getSelectedItem().toString());

		// section 3 values
		executionInfo.setReportName(txtReportName.getText().trim());
		executionInfo.setArchiveDir(txtArchiveDir.getText().trim());
		executionInfo.setResultsToArchive(chbArchiving.isSelected());
		executionInfo.setSettingsOk();
	}

	private void setEditablilityOfComponentsDueToCurrentSettings()
	{
		boolean enableStateOfStartButton = true;

		if (chbArchiving.isSelected()) {
			lblArchiveDir.setForeground(Color.BLACK);
			txtArchiveDir.setEditable(true);
			btnFileSelect.setEnabled(true);

			enableStateOfStartButton = isArchiveDataAvailable();
		} else {
			lblArchiveDir.setForeground(Color.LIGHT_GRAY);
			txtArchiveDir.setEditable(false);
			btnFileSelect.setEnabled(false);

			enableStateOfStartButton = true;
		}

		startButton.setEnabled(enableStateOfStartButton);
		btnReset.setEnabled(!defaultReportNameMode);
	}

	/**
	 * Checks whether report name matches the other selected settings
	 */
	private boolean checkReportName()
	{
		final String defaultFromGUI = buildDefaultReportName();
		final String defaultFromFile = executionInfo.getReportName();
		return defaultFromFile.equals(defaultFromGUI);
	}

	private boolean isArchiveDataAvailable()
	{
		final File file = new File(txtArchiveDir.getText());
		return file.exists() && file.isDirectory();
	}

	class CheckBoxArchivingActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			SysNatStartDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
		}
	}

	class TargetEnvListener implements ActionListener, KeyListener
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
			String selectedTestApp = cbxTestApplication.getSelectedItem().toString();
			List<TargetEnv> configuredEnvironments = testAppEnvironmentsMap.get(selectedTestApp);
			String[] modelData = configuredEnvironments.stream().map(env -> env.name()).toArray(String[]::new);
			cbxEnvironments.setModel(new DefaultComboBoxModel<String>(modelData));
		}
	}

	class ReportNameListener implements ActionListener, KeyListener
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
			if (eventSource == txtReportName) {
				defaultReportNameMode = false;
			} else if (eventSource == btnReset) {
				defaultReportNameMode = true;
			}

			if (defaultReportNameMode) {
				txtReportName.setText(SysNatStartDialog.this.buildDefaultReportName());
			}

			SysNatStartDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
		}
	}

	class StartDocingActionListener implements KeyListener, ActionListener
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
			SysNatStartDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
			closeDialogToStartGeneration(e);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			SysNatStartDialog.this.closeLoginDialogIfAlmLoginOk();
		}

		private void closeDialogToStartGeneration(KeyEvent e)
		{
			// TODO
		}
	}

	class StartTestingActionListener implements KeyListener, ActionListener
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
			SysNatStartDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
			closeDialogToStartTest(e);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			SysNatStartDialog.this.closeLoginDialogIfAlmLoginOk();
		}

		private void closeDialogToStartTest(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !cbxTestApplication.isPopupVisible()) {
				if (e.getComponent() == startButton) {
					SysNatStartDialog.this.closeLoginDialogIfAlmLoginOk();
				}
			}
		}
	}

	class CancelActionListener implements ActionListener, KeyListener
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
			terminateTestEnvironment();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			terminateTestEnvironment();
		}

		private void terminateTestEnvironment()
		{
			System.out.println("Testausführung von KoSelNat wurde abgebrochen.");
			System.exit(0);
		}
	}

	private void closeLoginDialogIfAlmLoginOk()
	{
		SysNatStartDialog.this.dispose();
		SysNatStartDialog.this.setSelectionToExecutionInfo();
	}

	public String buildDefaultReportName()
	{
		String filters = cbxFilters.getSelectedItem().toString();
		if (filters.equals(NO_FILTER)) {
			filters = CONSTANTS_BUNDLE.getString("All");
		}

		if (filters.length() != 0) {
			filters = "-" + filters;
		}

		return cbxTestApplication.getSelectedItem().toString() + "-" + cbxEnvironments.getSelectedItem().toString()
		        + filters;
	}

	class MyLookAndFeel extends MetalLookAndFeel
	{
		private static final long serialVersionUID = 1L;

		public MyLookAndFeel()
		{
			super();
		}

		protected void initSystemColorDefaults(UIDefaults table)
		{
			super.initSystemColorDefaults(table);
			table.put("info", new ColorUIResource(255, 255, 225));
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
			fileChooser.showSaveDialog(testingParentPanel);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtArchiveDir.setText(fileAsString);
		}
	}

	class SaveActionListener implements ActionListener, KeyListener
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
			saveCurrentSettings();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			saveCurrentSettings();
		}
	}

	public void saveCurrentSettings()
	{
		final List<String> oldFileContent = executionInfo.getContentOfConfigSettingsFile();
		final StringBuffer newFileContent = new StringBuffer();

		for (String line : oldFileContent) {
			line = line.trim();
			if (line.startsWith(SysNatLocaleConstants.TESTAPP_SETTING_KEY)) {
				newFileContent.append(SysNatLocaleConstants.TESTAPP_SETTING_KEY + " = "
				        + cbxTestApplication.getSelectedItem().toString());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("TESTAPP_SETTING_KEY"))) {
				newFileContent.append(CONSTANTS_BUNDLE_EN.getString("TESTAPP_SETTING_KEY") + " = "
				        + cbxTestApplication.getSelectedItem().toString());
			} else if (line.startsWith(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY)) {
				newFileContent.append(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY + " = "
				        + cbxEnvironments.getSelectedItem().toString());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY"))) {
				newFileContent.append(CONSTANTS_BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY") + " = "
				        + cbxEnvironments.getSelectedItem().toString());
			} else if (line.startsWith(SysNatLocaleConstants.BROWSER_SETTING_KEY)) {
				newFileContent.append(
				        SysNatLocaleConstants.BROWSER_SETTING_KEY + " = " + cbxBrowsers.getSelectedItem().toString());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("BROWSER_SETTING_KEY"))) {
				newFileContent.append(CONSTANTS_BUNDLE_EN.getString("BROWSER_SETTING_KEY") + " = "
				        + cbxBrowsers.getSelectedItem().toString());
			} else if (line.startsWith(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY)) {
				newFileContent.append(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY + " = "
				        + cbxExecSpeeds.getSelectedItem().toString());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"))) {
				newFileContent.append(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY") + " = "
				        + cbxExecSpeeds.getSelectedItem().toString());
			} else if (line.startsWith(SysNatLocaleConstants.EXECUTION_FILTER)) {
				newFileContent.append(
				        SysNatLocaleConstants.EXECUTION_FILTER + " = " + cbxFilters.getSelectedItem().toString());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_FILTER"))) {
				newFileContent.append(CONSTANTS_BUNDLE_EN.getString("EXECUTION_FILTER") + " = "
				        + cbxFilters.getSelectedItem().toString());
			} else if (line.startsWith(SysNatLocaleConstants.REPORT_NAME_SETTING_KEY)) {
				newFileContent.append(SysNatLocaleConstants.REPORT_NAME_SETTING_KEY + " = " + txtReportName.getText());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY"))) {
				newFileContent.append(
				        CONSTANTS_BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY") + " = " + txtReportName.getText());
			} else if (line.startsWith(SysNatLocaleConstants.ARCHIVE_DIR_SETTING_KEY)) {
				newFileContent.append(SysNatLocaleConstants.ARCHIVE_DIR_SETTING_KEY + " = " + txtArchiveDir.getText());
			} else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY"))) {
				newFileContent.append(
				        CONSTANTS_BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY") + " = " + txtArchiveDir.getText());
			} else {
				newFileContent.append(line);
			}
			newFileContent.append(System.getProperty("line.separator"));
		}

		// System.out.println(newFileContent.toString().trim());
		SysNatFileUtil.writeFile(ExecutionRuntimeInfo.CONFIG_FILE_NAME, newFileContent.toString().trim());
	}

	public static void doYourJob()
	{
		if (ExecutionRuntimeInfo.getInstance().getUseSettingsDialog()) {
			try {
				CompletableFuture.runAsync(SysNatStartDialog::runGUI).get();
			} catch (Exception e) {
				System.err.println("Error starting SettingsConfigDialog!");
			}
		}
	}

	private static void runGUI()
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();

		try {
			new SysNatStartDialog().setVisible(true);
			while (!executionInfo.areSettingsComplete()) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception
	{
		new SysNatStartDialog();
	}

}
