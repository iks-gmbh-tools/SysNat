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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.JButton;
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
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * Graphical User Interface to export or import test application specific files.
 * This is needed to exchange and synchronize test application data 
 * between different SysNat installations.
 * 
 * @author Reik Oberrath
 */
public class TestApplicationExImportDialog extends JFrame 
{
	private static final long serialVersionUID = 1L;
	private static final String EXECUTION_PROPERTIES_DIR = "../sysnat.test.runtime.environment/src/main/resources/execution_properties";
	private static final String LANGUAGE_TEMPLATE_CONTAINER_DIR = "../sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates";
	private static final String TEST_DATA_DIR = "testdata";
	private static final String EXECUTABLE_EXAMPLE_DIR = "ExecutableExamples";
	
	private static final int deltaY = 40;
	private static final String NO_TOOLTIP = "NO_TOOLTIP";

	private final Font labelFont = new Font("Arial", Font.BOLD, 16);
	private final Font buttonFont = new Font("Arial", Font.BOLD, 16);
	private final Font fieldFont = new Font("Arial", Font.PLAIN, 18);

	private final int buttonDistanceToLowerFrameEdge = 100;
	private final int firstColumnLength = 160;
	private final int secondColumnLength =400;
	private final int thirdColumnLength = 100;
	private final int xPosFirstColumn = 10;
	private final int columnDistance = 10;
	private final int xPosSecondColumn = xPosFirstColumn + firstColumnLength;
	private final int xPosThirdColumn = xPosSecondColumn + secondColumnLength + columnDistance;
	private final int frameWidth = xPosThirdColumn + thirdColumnLength + 50;
	private final int frameHeight = 200;
	
	
	private int yPos = 5;

	private JPanel parentPanel;
	private JTextField txtTargetDir;
	private JButton importButton, btnFileSelect;

	public TestApplicationExImportDialog() throws Exception
	{
        UIManager.put("ToolTip.font", new FontUIResource("SansSerif", Font.PLAIN, 16));
		UIManager.setLookAndFeel(new MyLookAndFeel());
		ToolTipManager.sharedInstance().setInitialDelay(10);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		setMinimumSize(new Dimension(frameWidth,frameHeight));
		setTitle("SysNat Data Export Import Dialog");
		setLocationRelativeTo(null);
		parentPanel = new JPanel(null);
		parentPanel.setBounds(0,0,frameWidth,frameHeight);
		getContentPane().add(parentPanel);
		initComponents(parentPanel);

		setVisible(true);
	}

	private void initComponents(final JPanel parentPanel) 
	{
		yPos += deltaY;
		initLabel("Target directory", yPos, NO_TOOLTIP);		
		String rootDir = new File("").getAbsolutePath();
		String workspaceDir = new File(rootDir).getParentFile().getAbsolutePath();
		txtTargetDir = initTextField(yPos, workspaceDir, null);
		btnFileSelect = initButton("Select", yPos, null);
		btnFileSelect.addActionListener(new FileSelectListener());
		
		initCancelButton();
		initExportButton();
		initImportButton();
	}
  
	private void initImportButton() 
	{
		importButton = new JButton("Import");
		importButton.setToolTipText("Imports SysNat data for a single test application from the selected target directory.");
		
        final ImportActionListener importActionListener = new ImportActionListener();
        importButton.addActionListener(importActionListener);
        importButton.addKeyListener(importActionListener);
		
        int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
		importButton.setBounds(570,yPos,140,30);
		importButton.setFont(buttonFont);
		importButton.setBackground(Color.ORANGE);
		importButton.setForeground(Color.BLACK);
		
		parentPanel.add(importButton);
	}

	private void initExportButton() 
    {
       final JButton exportButton = new JButton("Export");
	   exportButton.setToolTipText("Exports SysNat data of all known test applications into the selected target directory.");
       final ExportActionListener exportActionListener = new ExportActionListener();
       exportButton.addActionListener(exportActionListener);
       exportButton.addKeyListener(exportActionListener);
	   int xPos = 270; 
       int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
       exportButton.setBounds(xPos,yPos,180,30);
       exportButton.setFont(labelFont);
       exportButton.setBackground(Color.GREEN);
       exportButton.setForeground(Color.BLACK);
       
       parentPanel.add(exportButton);
    }
  
	private void initCancelButton() 
	{
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Terminates dialog without any action.");
		final CancelActionListener cancelActionListener = new CancelActionListener();
		
		cancelButton.addActionListener(cancelActionListener);
		cancelButton.addKeyListener(new CancelActionListener());
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge; 
		cancelButton.setBounds(10,yPos,150,30);
		cancelButton.setFont(buttonFont);
		
		parentPanel.add(cancelButton);
	}

	private JTextField initTextField(int yPos, String initialText, String toolTipText) {
		final JTextField txtField = new JTextField();
		initTextField(txtField, yPos, initialText, toolTipText);
		return txtField;
	}

	private void initTextField(final JTextField txtField,
			                   final int yPos, 
			                   final String initialText,
			                   final String toolTipText) 
	{
		txtField.setBounds(xPosSecondColumn,yPos,secondColumnLength,22);
		txtField.setFont(fieldFont);
		txtField.setText(initialText);
		txtField.setToolTipText(toolTipText);
		
		parentPanel.add(txtField);
	}
	

	private JButton initButton(String text, int yPos, String tooltipText) 
	{
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
			text = text + ":";
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
			fileChooser.setDialogTitle("Folder Selector");
			fileChooser.showSaveDialog(parentPanel);
			String fileAsString = fileChooser.getSelectedFile().getAbsolutePath();
			fileAsString = fileAsString.replaceAll("\\\\", "/");
			txtTargetDir.setText(fileAsString);
		}
	}

    class ImportActionListener implements ActionListener, KeyListener
    {
       @Override public void keyTyped(KeyEvent e) {}

       @Override public void keyPressed(KeyEvent e) {}

       @Override
       public void keyReleased(KeyEvent e) {
    	   importTestApplicationData();
       }

       @Override
       public void actionPerformed(ActionEvent e) {
    	   importTestApplicationData();
       }
    }
	
    class ExportActionListener implements ActionListener, KeyListener
    {
       @Override public void keyTyped(KeyEvent e) {}

       @Override public void keyPressed(KeyEvent e) {}

       @Override
       public void keyReleased(KeyEvent e) {
    	   exportTestApplicationData();
       }

       @Override
       public void actionPerformed(ActionEvent e) {
    	   exportTestApplicationData();
       }
    }
	
    public void exportTestApplicationData() 
    {
    	this.dispose();
    	final File targetDir = new File(txtTargetDir.getText());
    	if ( ! targetDir.isDirectory() ) {
    		System.err.println("Error: " + targetDir.getAbsolutePath() + " is no directory!");
    		System.exit(1);
    	}
    	
    	if ( ! targetDir.exists() ) {
    		boolean ok = targetDir.mkdirs();
    		if ( ! ok) {
        		System.err.println("Error: " + targetDir.getAbsolutePath() + " could not be created!");
        		System.exit(1);
    		}
    	}
    	
    	if ( targetDir.list().length > 0 ) {
    		System.err.println("Error: " + targetDir.getAbsolutePath() + " is not empty!");
    		System.exit(1);
    	}

    	List<String> knownTestApplications = findKnownTestApplications();
    	
    	knownTestApplications.forEach(applicationName -> copySysNatData(targetDir, applicationName));
    	
    	System.out.println("Done with export.");
    }

	private void copySysNatData(File targetRootDir, String applicationName) 
	{
		targetRootDir = new File(targetRootDir, applicationName);
		boolean ok = targetRootDir.mkdir();
		if ( ! ok) {
    		System.err.println("Error: " + targetRootDir.getAbsolutePath() + " could not be created!");
    		System.exit(1);
		}
		
    	SysNatFileUtil.copyTextFileToTargetDir(EXECUTION_PROPERTIES_DIR, applicationName + ".properties", targetRootDir.getAbsolutePath());
    	
    	File sourceDir = new File(TEST_DATA_DIR, applicationName);
    	SysNatFileUtil.copyFolder(sourceDir, new File(targetRootDir, "testdata"));
    	
    	sourceDir = new File(LANGUAGE_TEMPLATE_CONTAINER_DIR, applicationName);
    	SysNatFileUtil.copyFolder(sourceDir, new File(targetRootDir, "language_container"));
    	
    	sourceDir = new File(EXECUTABLE_EXAMPLE_DIR, applicationName);
    	SysNatFileUtil.copyFolder(sourceDir, new File(targetRootDir, "ExecutableExamples"));
	}

	private List<String> findKnownTestApplications() 
	{
		final File propertiesDir = new File(EXECUTION_PROPERTIES_DIR);
    	String[] list = propertiesDir.list();
    	List<String> knownTestApplications = new ArrayList<>();
    	for (String filename : list) {
    		if ( ! filename.equals("execution.properties") ) {
    			int pos = filename.lastIndexOf('.');
    			knownTestApplications.add(filename.substring(0, pos));
    		}
		}
		return knownTestApplications;
	}
    
    public void importTestApplicationData() 
    {
    	this.dispose();
    	final File targetDir = new File(txtTargetDir.getText());
    	
    	if ( ! targetDir.exists() ) {
    		System.err.println("Error: " + targetDir.getAbsolutePath() + " does not exist!");
    		System.exit(1);
    	}

    	if ( ! targetDir.isDirectory() ) {
    		System.err.println("Error: " + targetDir.getAbsolutePath() + " is no directory!");
    		System.exit(1);
    	}
    	
    	if ( targetDir.list().length == 0 ) {
    		System.err.println("Error: " + targetDir.getAbsolutePath() + " is empty!");
    		System.exit(1);
    	}

    	final String testApplicationToImport = targetDir.getName();
		
    	SysNatFileUtil.copyTextFileToTargetDir(targetDir.getAbsolutePath(), testApplicationToImport + ".properties", EXECUTION_PROPERTIES_DIR);
    	
    	File sourceDir = new File(targetDir, "testdata/" + testApplicationToImport);
    	SysNatFileUtil.copyFolder(sourceDir, new File(TEST_DATA_DIR));
    	
    	sourceDir = new File(targetDir, "language_container/" + testApplicationToImport.toLowerCase());
    	SysNatFileUtil.copyFolder(sourceDir, new File(LANGUAGE_TEMPLATE_CONTAINER_DIR));
    	
    	sourceDir = new File(targetDir, "ExecutableExamples/" + testApplicationToImport);
    	SysNatFileUtil.copyFolder(sourceDir, new File(EXECUTABLE_EXAMPLE_DIR));
    	
    	System.out.println("Done with import.");
    }
    
	public static void doYourJob() 
	{
		if (ExecutionRuntimeInfo.getInstance().getUseSettingsDialog())
		{			
			try {
				CompletableFuture.runAsync( TestApplicationExImportDialog::runGUI ).get();
			} catch (Exception e) {
				System.err.println("Error starting SettingsConfigDialog!");
			}
		}
	}
	
	private static void runGUI() 
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		
		try {
			new TestApplicationExImportDialog().setVisible(true);
			while ( ! executionInfo.areSettingsComplete() ) {
					Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestApplicationExImportDialog();
	}
	
}