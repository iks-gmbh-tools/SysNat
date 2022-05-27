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
package com.iksgmbh.sysnat.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DialogStartTab;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.tab.UserCommonSettingsPanel;
import com.iksgmbh.sysnat.dialog.tab.UserDocingPanel;
import com.iksgmbh.sysnat.dialog.tab.GenerateNLInstructionPanel;
import com.iksgmbh.sysnat.dialog.tab.GeneratePageObjectPanel;
import com.iksgmbh.sysnat.dialog.tab.GenerateTestAppPanel;
import com.iksgmbh.sysnat.dialog.tab.GenerationDeleteTestAppPanel;
import com.iksgmbh.sysnat.dialog.tab.UserTestingPanel;

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
public class SysNatDialog extends JFrame 
{
	private static final String SYS_NAT_START_DIALOG = "SysNat-Start-Dialog";

	public enum DialogMode { User,  Developer};
	
	private static final String START_TAB = "StartTab";	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.getDefault());
	private static final long serialVersionUID = 1L;

	public static final Font tabFont = new Font("Arial", Font.BOLD, 20);

	public static final int buttonDistanceToLowerFrameEdge = 130;
	public static final int firstColumnLength = 350;
	public static final int secondColumnLength = 600;
	public static final int thirdColumnLength = 100;
	public static final int columnDistance = 10;
	public static final int xPosFirstColumn = 30;
	public static final int xPosSecondColumn = xPosFirstColumn + firstColumnLength;
	public static final int xPosThirdColumn = xPosSecondColumn + secondColumnLength + columnDistance;
	public static final int frameWidth = xPosThirdColumn + thirdColumnLength + 50;
	public static final int frameHeight = 685;
	public static final int lineHeight = 26;
	
	private static DialogMode mode = DialogMode.User;

	private JTabbedPane tabbedPane;
	private UserTestingPanel testingPanel;
	private UserDocingPanel docingPanel;
	private UserCommonSettingsPanel commonSettingsPanel;
	private GeneratePageObjectPanel pageObjectCreationPanel;
	private GenerateTestAppPanel testApplicationCreationPanel;
	private GenerateNLInstructionPanel languageInstructionCreationPanel;
	private GenerationDeleteTestAppPanel testAppDeletePanel;

	private File devgenConfigFile = new File("./devgen.config");

	public SysNatDialog() throws Exception 
	{
		init();
		if (testingPanel != null) testingPanel.getStartButton().requestFocus();
	}

	public static void doYourJob()
	{
		if (ExecutionRuntimeInfo.getInstance().useSettingsDialog()) {
			try {
				CompletableFuture.runAsync(SysNatDialog::runGUI).get();
			} catch (Exception e) {
				System.err.println("Error starting SettingsConfigDialog!");
			}
		}
	}

	private static void runGUI()
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();

		try {
			new SysNatDialog().setVisible(true);
			while (!executionInfo.areSettingsComplete()) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception
	{
		try {
			DialogMode aMode = DialogMode.valueOf(args[0]);
			mode = aMode;
		} catch (Exception e) {
			// igmore
		}
		ExecutionRuntimeInfo.getInstance();
		new SysNatDialog();
	}
	
	
	protected void init() throws Exception
	{
		UIManager.put("ToolTip.font", new FontUIResource("SansSerif", Font.PLAIN, 16));
		UIManager.setLookAndFeel(new MyLookAndFeel());
		ToolTipManager.sharedInstance().setInitialDelay(10);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		
		setMinimumSize(new Dimension(frameWidth, frameHeight));
		setTitle(SYS_NAT_START_DIALOG);
		setLocationRelativeTo(null);
		initWindowsCloseEventHandler();
		this.tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, frameWidth, frameHeight);
		getContentPane().add(tabbedPane);
		initComponents(tabbedPane);
		setVisible(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
	    public boolean dispatchKeyEvent(KeyEvent e) {
	    	return checkModeChange(e);
	    }});	
		initShutDownHook();
	}

	protected boolean checkModeChange(KeyEvent e)
	{
		if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown() && e.isAltDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_M) 
		{
			tabbedPane.removeAll();
			if (mode == DialogMode.User) {
				mode = DialogMode.Developer;
				setTitle(SYS_NAT_START_DIALOG + "  Developer Tools");
			} else {
				mode = DialogMode.User;
				setTitle(SYS_NAT_START_DIALOG);
			}
			initComponents(tabbedPane);
			tabbedPane.update(tabbedPane.getGraphics());
			tabbedPane.updateUI();
			return true;
		}
		return false;
	}

	private void initWindowsCloseEventHandler()
	{
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				SysNatDialog.this.terminateSysNatExecution();
			}
		});
	}


	protected void initComponents(JTabbedPane tabbedPane)
	{
		tabbedPane.setFont(tabFont);
		
		if (mode == DialogMode.User) 
		{
			testingPanel = new UserTestingPanel(this);
			tabbedPane.addTab(BUNDLE.getString("TAB_TESTING_SETTING"), null, testingPanel, "Start XX as tests...");
			tabbedPane.setMnemonicAt(0, KeyEvent.VK_T);
	
			docingPanel = new UserDocingPanel(this);
			tabbedPane.addTab(BUNDLE.getString("TAB_DOCING_SETTING"), null, docingPanel, "Start generation of documentation...");
			tabbedPane.setMnemonicAt(1, KeyEvent.VK_D);
	
			commonSettingsPanel = new UserCommonSettingsPanel(this);
			tabbedPane.addTab(BUNDLE.getString("TAB_GENERAL_SETTING"), null, commonSettingsPanel, "Define general settings...");
			tabbedPane.setMnemonicAt(2, KeyEvent.VK_G);
			
			tabbedPane.setSelectedIndex(getIndexOf(ExecutionRuntimeInfo.getInstance().getDialogStartTab()));
			
		} else {
			
			testApplicationCreationPanel = new GenerateTestAppPanel(this);
			tabbedPane.addTab("New TestApp", null, testApplicationCreationPanel, "Create a new TestApplications...");
			tabbedPane.setMnemonicAt(0, KeyEvent.VK_T);
			
			pageObjectCreationPanel = new GeneratePageObjectPanel(this);
			tabbedPane.addTab("New PageObject", null, pageObjectCreationPanel, "Create a new PageObject for an existing TestApplications...");
			tabbedPane.setMnemonicAt(1, KeyEvent.VK_P);
			
			languageInstructionCreationPanel = new GenerateNLInstructionPanel(this);
			tabbedPane.addTab("New LanguageTemplate", null, languageInstructionCreationPanel, "Create a new Natural Language instruction for an existing TestApplications...");
			tabbedPane.setMnemonicAt(2, KeyEvent.VK_L);
			
			testAppDeletePanel = new GenerationDeleteTestAppPanel(this);
			tabbedPane.addTab("Del TestApp", null, testAppDeletePanel, "Delete an existing TestApplications...");
			tabbedPane.setMnemonicAt(3, KeyEvent.VK_D);
			
			tabbedPane.setSelectedIndex(setActiveDevGenTabIndex());
		}
	}

	private int setActiveDevGenTabIndex()
	{
		if (! devgenConfigFile.exists()) {
			SysNatFileUtil.writeFile(devgenConfigFile, START_TAB + "=0");
			return 0;
		} else {
			Integer i;
			String content = SysNatFileUtil.readTextFileToString(devgenConfigFile);
			String[] splitResult = content.split("=");
			if (splitResult.length == 2 && splitResult[0].equals(START_TAB)) {
				try {
					i = Integer.valueOf(splitResult[1]);
				} catch (Exception e) {
					i = 1;
				}
			} else {
				i = 1;
			}
			return i;
		}		
	}
	
	private int getIndexOf(DialogStartTab dialogStartTab)
	{

		DialogStartTab value = ExecutionRuntimeInfo.getInstance().getDialogStartTab();
		
		switch (value)
		{
			case Testing:
				return 0;
			case Docing:
				return 1;
			default:
				return 2;
		}	
	}

	public void closeAndApplySettings()
	{
		SysNatDialog.this.dispose();
		testingPanel.setSettingsToExecutionInfo();
		docingPanel.setSettingsToExecutionInfo();
		commonSettingsPanel.setSettingsToExecutionInfo();
		ExecutionRuntimeInfo.getInstance().setSettingsOk();
	}
	

	public void saveCurrentSettings()
	{
		testingPanel.updateFilterOptions();
		testingPanel.saveCurrentSettings();
		docingPanel.saveCurrentSettings();
		commonSettingsPanel.saveCurrentSettings();
	}
	

	public void terminateSysNatExecution()
	{
		System.out.println("Die Ausführung von SysNat wurde abgebrochen!");
		System.exit(0);
	}
	
	public void finishSysNatExecution()
	{
		System.exit(0);
	}

	protected void initShutDownHook() 
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				SysNatFileUtil.writeFile(devgenConfigFile, START_TAB + "=" + tabbedPane.getSelectedIndex());
			}
		});
	}

	
	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################

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
	


}
