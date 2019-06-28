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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * Graphical User Interface to ask user for configuration settings.
 * It's designed to have three sections, each consisting of two or three 
 * rows and columns of graphical elements.
 * First column: a label
 * Second column: a input field
 * Third column: optional element (e.g. a button to reset the content of the input field)
 * 
 * @author Reik Oberrath
 */
public class SettingsConfigDialog extends JFrame 
{
	private static final ResourceBundle CONSTANTS_BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	private static final ResourceBundle CONSTANTS_BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SettingsConfigDialog", Locale.getDefault());
	private static final long serialVersionUID = 1L;
	
	private static final int deltaY = 40;
	private static final String NO_TOOLTIP = "NO_TOOLTIP";

	private final Font labelFont = new Font("Arial", Font.BOLD, 16);
	private final Font buttonFont = new Font("Arial", Font.BOLD, 16);
	private final Font labelSectionFont = new Font("Arial", Font.ITALIC, 18);
	private final Font fieldFont = new Font("Arial", Font.PLAIN, 18);

	private final int buttonDistanceToLowerFrameEdge = 100;
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
	private StartKeyActionListener startActionListener = new StartKeyActionListener();
	private ReportNameListener reportNameListener = new ReportNameListener();
	private int yPos = 10;
	private boolean defaultReportNameMode = true;
	
	private List<String> knownEnvironments = new ArrayList<>();
	private List<String> knownFilters = new ArrayList<>();
	private List<String> knownTestApplications = new ArrayList<>();
	private List<String> knownBrowsers = new ArrayList<>();
	private List<String> knownExecSpeeds = new ArrayList<>();

	private JPanel parentPanel;
	private JLabel lblArchiveDir;
	private JTextField txtReportName, txtArchiveDir;
	private JComboBox<String> cbxTestApplication, cbxEnvironments, cbxExecSpeeds, cbxBrowsers, cbxFilters;
	private JButton startButton, btnReset, btnFileSelect;
	private JCheckBox chbArchiving;
	private String executionFilterToolTipText;

	public SettingsConfigDialog() throws Exception
	{
        UIManager.put("ToolTip.font", new FontUIResource("SansSerif", Font.PLAIN, 16));
		UIManager.setLookAndFeel(new MyLookAndFeel());
		ToolTipManager.sharedInstance().setInitialDelay(10);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		executionInfo = ExecutionRuntimeInfo.getInstance();
		analyseConfigSettings();
		setMinimumSize(new Dimension(frameWidth,frameHeight));
		setTitle("SysNat Settings Dialog");
		setLocationRelativeTo(null);
		parentPanel = new JPanel(null);
		parentPanel.setBounds(0,0,frameWidth,frameHeight);
		getContentPane().add(parentPanel);
		initComponents(parentPanel);
		defaultReportNameMode = checkReportName();
		setEditablilityOfComponentsDueToCurrentSettings();

		setVisible(true);
	}

	private void analyseConfigSettings() 
	{
		final String toolTipIdentifier = "Tooltip";
		
		final List<String> contentOfConfigSettingsFile = executionInfo.getContentOfConfigSettingsFile();		
		final List<String> possibleValues = new ArrayList<>();
		final List<String> possibleValues_en = new ArrayList<>();
		StringBuffer tooltip = getNewTookTipStringBuffer();
		boolean possibleValueLineDetected = false;
		boolean possibleValueLineDetected_en = false;
		
		for (String line : contentOfConfigSettingsFile) 
		{
			if (line.contains(SysNatLocaleConstants.POSSIBLE_VALUE_IDENTIFIER)) {
				possibleValueLineDetected = true;
				continue;
			} 
			
			if (possibleValueLineDetected) {
				String valueLine = line.substring(1).trim();
				possibleValues.addAll( Arrays.asList( valueLine.split(",") ) );
				possibleValueLineDetected = false;
				continue;
			}
			
			if (line.startsWith("# " + CONSTANTS_BUNDLE_EN.getString("POSSIBLE_VALUE_IDENTIFIER"))) {
				possibleValueLineDetected_en = true;
				continue;
			} 
	
			if (possibleValueLineDetected_en) {
				String valueLine = line.substring(1).trim();
				possibleValues_en.addAll( Arrays.asList( valueLine.split(",") ) );
				possibleValueLineDetected_en = false;
				continue;
			}
			
			if (line.contains(toolTipIdentifier)) 
			{
				String toolTipLine = line.substring(toolTipIdentifier.length()).trim();
				tooltip.append(toolTipLine).append("<br>");
				continue;
			} 
			
			if (line.contains(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY))
			{
				knownEnvironments.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			} 
			else if (line.contains(SysNatLocaleConstants.TESTAPP_SETTING_KEY))
			{
				knownTestApplications.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			}
			else if (line.contains(SysNatLocaleConstants.BROWSER_SETTING_KEY))
			{
				knownBrowsers.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			}
			else if (line.startsWith(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY))
			{
				knownExecSpeeds.addAll(trim(possibleValues));
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			}
			else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY")))
			{
				knownExecSpeeds.addAll(trim(possibleValues_en));
				possibleValues_en.clear();
				tooltip = getNewTookTipStringBuffer();
			}
			else if (line.contains(SysNatLocaleConstants.EXECUTION_FILTER))
			{
				knownFilters.add("-");
				knownFilters.addAll(trim(possibleValues));
				executionFilterToolTipText = tooltip.append("</html>").toString();
				possibleValues.clear();
				tooltip = getNewTookTipStringBuffer();
			}
		}
	}
	
	private StringBuffer getNewTookTipStringBuffer() {
		return new StringBuffer("<html>");
	}

	private List<String> trim(final List<String> input) {
		return input.stream().map(s -> s.trim()).collect(Collectors.toList());
	}

	private void initComponents(final JPanel parentPanel) 
	{
		initSectionLabel("SECTION_1_LABEL", yPos);
		
		yPos += deltaY;
		initLabel("TESTAPP_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxTestApplication = initCombo(knownTestApplications.toArray(new String[knownTestApplications.size()]), 
				                       yPos,
				                       executionInfo.getTestApplicationName());
		cbxTestApplication.addActionListener(reportNameListener);
		
		yPos += deltaY;
		initLabel("ENVIRONMENT_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxEnvironments = initCombo(knownEnvironments.toArray(new String[knownEnvironments.size()]), 
				                    yPos,
				                    executionInfo.getTargetEnv().name());
		cbxEnvironments.addActionListener(reportNameListener);

		yPos += deltaY;
		initLabel("EXECUTION_FILTER", yPos, executionFilterToolTipText);
		cbxFilters = initCombo(knownFilters.toArray(new String[knownFilters.size()]),
				              yPos, 
				              executionInfo.getFiltersToExecute());
		cbxFilters.setToolTipText(executionFilterToolTipText);
		cbxFilters.addKeyListener(reportNameListener);

		yPos += deltaY;
		initSectionLabel("SECTION_2_LABEL", yPos);

		yPos += deltaY;
		initLabel("BROWSER_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxBrowsers = initCombo(knownBrowsers.toArray(new String[knownBrowsers.size()]), 
				                    yPos,
				                    executionInfo.getBrowserTypeToUse().name());

		yPos += deltaY;
		initLabel("EXECUTION_SPEED_SETTING_KEY", yPos, NO_TOOLTIP);
		cbxExecSpeeds = initCombo(knownExecSpeeds.toArray(new String[knownExecSpeeds.size()]), 
				                    yPos,
				                    executionInfo.getExecutionSpeed());

		yPos += deltaY;
		initSectionLabel("SECTION_3_LABEL", yPos);

		yPos += deltaY;
		initLabel("REPORT_NAME_SETTING_KEY", yPos, NO_TOOLTIP);
		txtReportName = initTextField(yPos, executionInfo.getReportName(), executionInfo.getReportName());
		btnReset = initButton("RESET_BUTTON_TEXT", yPos, BUNDLE.getString("RESET_BUTTON_TOOLTIP"));
		txtReportName.addKeyListener(reportNameListener);
		btnReset.addActionListener(reportNameListener);
		
		yPos += deltaY;
		chbArchiving = initCheckbox(BUNDLE.getString("WITH_ARCHIVING"), 
				                    executionInfo.areResultsToArchive(), 
				                    yPos);
		chbArchiving.addActionListener(new CheckBoxArchivingActionListener());
			
		yPos += deltaY;
		lblArchiveDir = initLabel("ARCHIVE_DIR_SETTING_KEY", yPos, NO_TOOLTIP);
		txtArchiveDir = initTextField(yPos, executionInfo.getArchiveDir(), executionInfo.getArchiveDir());
		btnFileSelect = initButton("FILE_SELECT_BUTTON_TEXT", yPos, null);
		btnFileSelect.addActionListener(new FileSelectListener());
		
		initCancelButton();
		initSaveButton();
		initStartButton();
	}
  
	private JCheckBox initCheckbox(final String title, 
			                       final boolean tickInitially, 
			                       final int yPos) 
	{
		JCheckBox ckeckbox = new JCheckBox(title);
		
		ckeckbox.setBounds(10,yPos,secondColumnLength,20);
		ckeckbox.setFont(labelFont);
		ckeckbox.setSelected(tickInitially);
		ckeckbox.addKeyListener(startActionListener);
		
		parentPanel.add(ckeckbox);
		return ckeckbox;
	}

	private void initStartButton() 
	{
		startButton = new JButton(BUNDLE.getString("START_BUTTON_TEXT"));
		
		startButton.addActionListener(startActionListener);
		startButton.addKeyListener(startActionListener);
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
		startButton.setBounds(570,yPos,140,30);
		startButton.setFont(buttonFont);
		startButton.setBackground(Color.GREEN);
		startButton.setForeground(Color.BLACK);
		
		parentPanel.add(startButton);
	}

	private void initSaveButton() 
    {
       final JButton saveButton = new JButton(BUNDLE.getString("SAVE_BUTTON_TEXT"));
       saveButton.setToolTipText(BUNDLE.getString("SAVE_BUTTON_TOOLTIP"));
       final SaveActionListener saveActionListener = new SaveActionListener();
       
       saveButton.addActionListener(saveActionListener);
       saveButton.addKeyListener(saveActionListener);
	   int xPos = 325; 
       int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
       saveButton.setBounds(xPos,yPos,180,30);
       saveButton.setFont(labelFont);
       
       parentPanel.add(saveButton);
    }
  
	private void initCancelButton() 
	{
		final JButton cancelButton = new JButton(BUNDLE.getString("CANCEL_BUTTON_TEXT"));
		final CancelActionListener cancelActionListener = new CancelActionListener();
		
		cancelButton.addActionListener(cancelActionListener);
		cancelButton.addKeyListener(new CancelActionListener());
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
		cancelButton.setBounds(10,yPos,250,30);
		cancelButton.setFont(buttonFont);
		cancelButton.setBackground(Color.ORANGE);
		cancelButton.setForeground(Color.BLACK);
		
		parentPanel.add(cancelButton);
	}

	private JTextField initTextField(int yPos, String initialText, String toolTipText) {
		final JTextField txtField = new JTextField();
		initTextField(txtField, yPos, initialText, toolTipText);
		return txtField;
	}

	@SuppressWarnings("unused")
	private JTextField initTextField(int yPos, String initialText) {
		return initTextField(yPos, initialText, "");
	}

	private void initTextField(final JTextField txtField,
			                   final int yPos, 
			                   final String initialText,
			                   final String toolTipText) 
	{
		txtField.setBounds(xPosSecondColumn,yPos,secondColumnLength,22);
		txtField.setFont(fieldFont);
		txtField.addKeyListener(startActionListener);
		txtField.setText(initialText);
		txtField.setToolTipText(toolTipText);
		
		parentPanel.add(txtField);
	}
	
	private JComboBox<String> initCombo(String[] optionList, 
			                            int yPos, 
			                            String initialSelection) 
	{
		final JComboBox<String> cbx = new JComboBox<String>(optionList);
		
		cbx.setBounds(xPosSecondColumn,yPos,secondColumnLength,26);
		cbx.setFont(fieldFont);
		cbx.addKeyListener(startActionListener);
		cbx.setEditable(true);
		cbx.setBackground(Color.WHITE);
		cbx.setSelectedItem(initialSelection);
		
		parentPanel.add(cbx);
		return cbx;
	}
	
	private void initSectionLabel(String text, int yPos) 
	{
		final JLabel lbl = new JLabel();

		lbl.setText(BUNDLE.getString(text));
		lbl.setBounds(xPosFirstColumn,yPos,firstColumnLength+secondColumnLength,20);
		lbl.setFont(labelSectionFont);
		
		parentPanel.add(lbl);
	}

	private JButton initButton(String text, int yPos, String tooltipText) 
	{
		text = BUNDLE.getString(text);
		final JButton btn = new JButton(text);

		btn.setBounds(xPosThirdColumn,yPos,thirdColumnLength,20);
		btn.setFont(buttonFont);
		if (tooltipText != NO_TOOLTIP) {
			btn.setToolTipText(tooltipText);
		}
	
		parentPanel.add(btn);
		return btn;
	}

	private JLabel initLabel(String text, int yPos, String tooltipText) 
	{
		final JLabel lbl = new JLabel();
		
		if ( ! text.endsWith(":")) {
			text = getBundleText(text) + ":";
		}

		lbl.setText(text);
		lbl.setBounds(xPosFirstColumn,yPos,firstColumnLength,20);
		lbl.setFont(labelFont);
		if (tooltipText != NO_TOOLTIP) {
			lbl.setToolTipText(tooltipText);
		}
		
		parentPanel.add(lbl);
		return lbl;
	}

	private String getBundleText(String key)
	{
		String toReturn = CONSTANTS_BUNDLE.getString(key);
		if (toReturn == null) toReturn = CONSTANTS_BUNDLE_EN.getString(key);
		return toReturn.replaceAll("_", " ");
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
		
		if (chbArchiving.isSelected())
		{
			lblArchiveDir.setForeground(Color.BLACK);
			txtArchiveDir.setEditable(true);
			btnFileSelect.setEnabled(true);
			
			enableStateOfStartButton = isArchiveDataAvailable();
		}
		else 
		{
			lblArchiveDir.setForeground(Color.LIGHT_GRAY);
			txtArchiveDir.setEditable(false);
			btnFileSelect.setEnabled(false);

			enableStateOfStartButton = true;
		}
		
		startButton.setEnabled(enableStateOfStartButton);
		btnReset.setEnabled( ! defaultReportNameMode );
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

	private boolean isArchiveDataAvailable() {
		final File file = new File(txtArchiveDir.getText());
		return file.exists() && file.isDirectory();
	}


	class CheckBoxArchivingActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsConfigDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
		}
	}

	class ReportNameListener implements ActionListener, KeyListener 
	{

		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {
			updateReportNameIfNecessary(e.getComponent());
		}
		@Override public void actionPerformed(ActionEvent e) {
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
				txtReportName.setText(SettingsConfigDialog.this.buildDefaultReportName());
			}
			
			SettingsConfigDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
		}
	}
	
	class StartKeyActionListener implements KeyListener, ActionListener
	{
		@Override public void keyTyped(KeyEvent e) {}

		@Override public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			SettingsConfigDialog.this.setEditablilityOfComponentsDueToCurrentSettings();
			closeDialogToStartTest(e);
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			SettingsConfigDialog.this.closeLoginDialogIfAlmLoginOk();
		}
		
		private void closeDialogToStartTest(KeyEvent e) 
		{
			if (e.getKeyCode()==KeyEvent.VK_ENTER
				&& 
				! cbxTestApplication.isPopupVisible()) 
			{
				if (e.getComponent() == startButton) {
					SettingsConfigDialog.this.closeLoginDialogIfAlmLoginOk();	
				}				
			}
		}
	}
	
	class CancelActionListener implements ActionListener, KeyListener
	{
		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}

		@Override public void keyReleased(KeyEvent e) {
			terminateTestEnvironment();
		}

		@Override public void actionPerformed(ActionEvent e) {
			terminateTestEnvironment();
		}
		
		private void terminateTestEnvironment() {
			System.out.println("Testausführung von KoSelNat wurde abgebrochen.");
			System.exit(0);
		}
	}

	private void closeLoginDialogIfAlmLoginOk() 
	{
		SettingsConfigDialog.this.dispose();
		SettingsConfigDialog.this.setSelectionToExecutionInfo();
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
		
		return cbxTestApplication.getSelectedItem().toString() + "-"
				+ cbxEnvironments.getSelectedItem().toString() + filters;
	}


	class MyLookAndFeel extends MetalLookAndFeel 
	{
		private static final long serialVersionUID = 1L;

		public MyLookAndFeel() {
			super();
		}
		
		protected void initSystemColorDefaults(UIDefaults table) {
			super.initSystemColorDefaults(table);
			table.put("info", new ColorUIResource(255, 255, 225));
		}
	}

	class FileSelectListener implements ActionListener
	{
		@Override public void actionPerformed(ActionEvent e) 
		{
			JFileChooser fileChooser = new JFileChooser(SysNatTestRuntimeUtil.getSysNatRootDir());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle(BUNDLE.getString("FILE_SELECT_DIALOG_TITLE"));
			fileChooser.showSaveDialog(parentPanel);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtArchiveDir.setText(fileAsString);
		}
	}
	
    class SaveActionListener implements ActionListener, KeyListener
    {
       @Override public void keyTyped(KeyEvent e) {}

       @Override public void keyPressed(KeyEvent e) {}

       @Override
       public void keyReleased(KeyEvent e) {
    	   saveCurrentSettings();
       }

       @Override
       public void actionPerformed(ActionEvent e) {
           saveCurrentSettings();
       }
    }
	
    public void saveCurrentSettings() 
    {
       final List<String> oldFileContent = executionInfo.getContentOfConfigSettingsFile();
       final StringBuffer newFileContent = new StringBuffer();
       
       for (String line : oldFileContent) 
       {
         line = line.trim();
         if (line.startsWith(SysNatLocaleConstants.TESTAPP_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.TESTAPP_SETTING_KEY + " = " + cbxTestApplication.getSelectedItem().toString());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("TESTAPP_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("TESTAPP_SETTING_KEY") + " = " + cbxTestApplication.getSelectedItem().toString());
         } 
         else if (line.startsWith(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.ENVIRONMENT_SETTING_KEY + " = " + cbxEnvironments.getSelectedItem().toString());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY") + " = " + cbxEnvironments.getSelectedItem().toString());
         } 
         else if (line.startsWith(SysNatLocaleConstants.BROWSER_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.BROWSER_SETTING_KEY + " = " + cbxBrowsers.getSelectedItem().toString());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("BROWSER_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("BROWSER_SETTING_KEY") + " = " + cbxBrowsers.getSelectedItem().toString());             
         } 
         else if (line.startsWith(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.EXECUTION_SPEED_SETTING_KEY + " = " + cbxExecSpeeds.getSelectedItem().toString());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY") + " = " + cbxExecSpeeds.getSelectedItem().toString());             
         } 
         else if (line.startsWith(SysNatLocaleConstants.EXECUTION_FILTER)) {
             newFileContent.append(SysNatLocaleConstants.EXECUTION_FILTER + " = " + cbxFilters.getSelectedItem().toString());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("EXECUTION_FILTER"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("EXECUTION_FILTER") + " = " + cbxFilters.getSelectedItem().toString());             
         } 
         else if (line.startsWith(SysNatLocaleConstants.REPORT_NAME_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.REPORT_NAME_SETTING_KEY + " = " + txtReportName.getText());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY") + " = " + txtReportName.getText());             
         } 
         else if (line.startsWith(SysNatLocaleConstants.ARCHIVE_DIR_SETTING_KEY)) {
             newFileContent.append(SysNatLocaleConstants.ARCHIVE_DIR_SETTING_KEY + " = " + txtArchiveDir.getText());
         } else if (line.startsWith(CONSTANTS_BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY"))) {
             newFileContent.append(CONSTANTS_BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY") + " = " + txtArchiveDir.getText());             
         } 
         else {
             newFileContent.append(line);
         }
         newFileContent.append(System.getProperty("line.separator"));
       }

       //System.out.println(newFileContent.toString().trim());
       SysNatFileUtil.writeFile(ExecutionRuntimeInfo.CONFIG_FILE_NAME, newFileContent.toString().trim());
    }
    
    
	public static void doYourJob() 
	{
		if (ExecutionRuntimeInfo.getInstance().getUseSettingsDialog())
		{			
			try {
				CompletableFuture.runAsync( SettingsConfigDialog::runGUI ).get();
			} catch (Exception e) {
				System.err.println("Error starting SettingsConfigDialog!");
			}
		}
	}
	
	private static void runGUI() 
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		
		try {
			new SettingsConfigDialog().setVisible(true);
			while ( ! executionInfo.areSettingsComplete() ) {
					Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new SettingsConfigDialog();
	}
	
}