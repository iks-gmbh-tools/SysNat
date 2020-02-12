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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DialogStartTab;
import com.iksgmbh.sysnat.dialog.tab.DocingPanel;
import com.iksgmbh.sysnat.dialog.tab.GeneralSettingsPanel;
import com.iksgmbh.sysnat.dialog.tab.HorizontalLine;
import com.iksgmbh.sysnat.dialog.tab.TestingPanel;

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
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.getDefault());

	private static final long serialVersionUID = 1L;
	private static final Font tabFont = new Font("Arial", Font.BOLD, 20);

	public static final int buttonDistanceToLowerFrameEdge = 130;
	public static final int firstColumnLength = 350;
	public static final int secondColumnLength = 350;
	public static final int thirdColumnLength = 100;
	public static final int xPosFirstColumn = 30;
	public static final int columnDistance = 10;
	public static final int xPosSecondColumn = xPosFirstColumn + firstColumnLength;
	public static final int xPosThirdColumn = xPosSecondColumn + secondColumnLength + columnDistance;
	public static final int frameWidth = xPosThirdColumn + thirdColumnLength + 50;
	public static final int frameHeight = 675;
	
	private TestingPanel testingPanel;
	private DocingPanel docingPanel;
	private GeneralSettingsPanel generalSettingsPanel;


	public SysNatDialog() throws Exception {
		init();
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
		}
	}

	public static void main(String[] args) throws Exception
	{
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
		setTitle("SysNat Start Dialog");
		setLocationRelativeTo(null);
		initWindowsCloseEventHandler();
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, frameWidth, frameHeight);
		getContentPane().add(tabbedPane);
		initComponents(tabbedPane);
		setVisible(true);
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


	private void initComponents(JTabbedPane tabbedPane)
	{
		tabbedPane.setFont(tabFont);

		testingPanel = new TestingPanel(this);
		tabbedPane.addTab(BUNDLE.getString("TAB_TESTING_SETTING"), null, testingPanel, "Start XX as tests...");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_T);

		docingPanel = new DocingPanel(this);
		tabbedPane.addTab(BUNDLE.getString("TAB_DOCING_SETTING"), null, docingPanel, "Start generation of documentation...");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_D);

		generalSettingsPanel = new GeneralSettingsPanel(this);
		tabbedPane.addTab(BUNDLE.getString("TAB_GENERAL_SETTING"), null, generalSettingsPanel, "Define general settings...");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_G);
		
		tabbedPane.setSelectedIndex(getIndexOf(ExecutionRuntimeInfo.getInstance().getDialogStartTab()));
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
		generalSettingsPanel.setSettingsToExecutionInfo();
		ExecutionRuntimeInfo.getInstance().setSettingsOk();
	}
	

	public void saveCurrentSettings()
	{
		testingPanel.saveCurrentSettings();
		docingPanel.saveCurrentSettings();
		generalSettingsPanel.saveCurrentSettings();
	}
	

	public void terminateSysNatExecution()
	{
		System.out.println("Die Ausführung von SysNat wurde abgebrochen!");
		System.exit(0);
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
	
	public void insertSeparationLine(JPanel parent, int yPos) {
        new HorizontalLine(parent, Color.BLACK, xPosFirstColumn, yPos, 
        		           xPosThirdColumn + thirdColumnLength - xPosFirstColumn, 2);
	}


}
