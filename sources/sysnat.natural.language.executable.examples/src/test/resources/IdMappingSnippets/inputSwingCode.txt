package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.ArrayUtils;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElement;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElementBuilder;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

public class GeneratePageObjectPanel extends BasicTabPanel
{
	private static final String EVERYWHERE = "<EVERYWHERE>";
	private static final String TOOLTIP_PAGEOBJECT_FROM = "PageObject to leave";
	private static final String TOOLTIP_PAGEOBJECT_TO = "PageObject to go to";
	private static final String TOOLTIP_WAIT_CONDITION = "Wait condition after event (either millis to wait or id of gui element to appear)";
	private static final String TOOLTIP_EVENT_TRIGGER_TYPE = "Type of event that triggers this page change";
	private static final String TOOLTIP_EVENT_TRIGGER_NAME = "Name of GUI element that can trigger this page change";

	private static final long serialVersionUID = 1L;
	
	private PageObjectNameListener pageObjectNameListener = new PageObjectNameListener();
	private TestApplicationListener testApplicationListener = new TestApplicationListener();
	private List<String> existingPageObbjects = new ArrayList<>();
	private int xPos = SysNatDialog.xPosFirstColumn;
	private JPanel hidePanel = null;
	
	private JComboBox<String> cbxTestApplication;
	private JComboBox<String> cbxChangeEventType_forth, cbxChangeEventType_back;
	private JComboBox<String> cbxPageObjectFrom_forth, cbxPageObjectTo_back;
	private JTextField txtPageObjectName;
	private JTextField txtEventTriggerName_forth, txtEventTriggerName_back;
	private JTextField txtPageObjectTo_forth, txtPageObjectFrom_back;
	private JTextField txtPageObjectWait_forth, txtPageObjectWait_back;
	
	public GeneratePageObjectPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initComponents();
	}

	private void initComponents()
	{
		yPos = 30;
		createSectionLabel(this, yPos, "Mandatory basic information");
		initTextFieldPageObjectName();
		initComboTestApplication();
		
		yPos += deltaY + 10;
		createSeparationLine(this, yPos);
		yPos += deltaY;
		
		createSectionLabel(this, yPos, "Optional Page Change Events");
		findExistingPageObbjects();
		initPageChangeEventComponents_forth();
		initLabelRow();
		initPageChangeEventComponents_back();
		initInfoLabel();
		
		// button line
		initCancelButton(this);
		initStartButton(this);
	}

	private void findExistingPageObbjects()
	{
		existingPageObbjects.clear();
		String testApp = cbxTestApplication.getSelectedItem().toString();
		String packageName = testApp.toLowerCase();
		String path = "../sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/" + packageName;
		String filename = "LanguageTemplatesBasics_" +  testApp + ".java";
		File file = new File(path, filename);
		if (! file.exists()) return;
		
		List<String> lines = SysNatFileUtil.readTextFile(file);
		List<String> list = lines.stream().filter(e -> e.trim().startsWith("private "))
                                          .map(e -> e.trim().substring(7).trim())
                                          .filter(e -> e.contains("PageObject "))
                                          .map(e -> e.substring(0, e.indexOf(" ")).trim())
                                          .map(e -> e.substring(0, e.indexOf("Object")))
                                          .collect(Collectors.toList());
		String mainPage = list.stream().filter(e -> e.equals("MainPage")).findFirst().orElse(null);
		if (mainPage != null) {
			list.remove(mainPage);
			list.add(0, mainPage);
		}
		existingPageObbjects.addAll(list);
	}

	protected void initStartButton(JPanel parent) {
		initStartButton(parent, "Start generation...", null, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				createCode();
			}
		});
	}
	
	protected void createCode()
	{
		String testAppName = cbxTestApplication.getSelectedItem().toString();
		String pageobjectName = txtPageObjectName.getText().trim();
		String result = PageObjectDevGen.doYourJob(testAppName, pageobjectName, getPageChangeElements(), getIdMappings());
		if (result.startsWith("Error:")) {
			System.err.println(result);
			JOptionPane.showMessageDialog(frameDialog, result, "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			String message = "<html>Close dialog and finish generation?<br><br><html>";
			String[] options = { "Yes", "No" };
			int answer = JOptionPane.showOptionDialog(frameDialog, message, "Generation successful!", 
					     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (answer == JOptionPane.YES_OPTION) {
				frameDialog.finishSysNatExecution();
			} else {
				reset();
			}
		}
	}

	private HashMap<GuiType, HashMap<String, String>> getIdMappings() {
		return new HashMap<SysNatConstants.GuiType, HashMap<String,String>>();  // no mappings here
	}

	private PageChangeElement[] getPageChangeElements()
	{
		List<PageChangeElement> toReturn = new ArrayList<>();

		String triggerComponent = txtEventTriggerName_forth.getText().trim();
		if (! triggerComponent.isEmpty()) 
		{
			String fromPage = cbxPageObjectFrom_forth.getSelectedItem().toString();
			if (EVERYWHERE.equals(fromPage)) fromPage = null; 
			toReturn.add(new PageChangeElementBuilder().from(fromPage)
                                                       .via(EventType.valueOf(cbxChangeEventType_forth.getSelectedItem().toString()))
                                                       .on(triggerComponent)
                                                       .to(txtPageObjectTo_forth.getText().trim())
                                                       .waiting(txtPageObjectWait_forth.getText().trim()).build());
		}

		triggerComponent = txtEventTriggerName_back.getText().trim();
		if (! triggerComponent.isEmpty()) 
		{
			String fromPage = cbxPageObjectFrom_forth.getSelectedItem().toString();
			if (EVERYWHERE.equals(fromPage)) fromPage = null; 
			toReturn.add(new PageChangeElementBuilder().from(txtPageObjectFrom_back.getText().trim())
													   .via(EventType.valueOf(cbxChangeEventType_back.getSelectedItem().toString()))
													   .on(triggerComponent)
													   .to(cbxPageObjectTo_back.getSelectedItem().toString())
													   .waiting(txtPageObjectWait_back.getText().trim()).build());
		}
		
		return toReturn.stream().toArray(PageChangeElement[]::new);
	}

	private void initLabelRow() {
		int y = yPos - deltaY;
		JLabel label = initLabel(this, y, "PageObject", NO_TOOLTIP);
		Rectangle bounds = cbxPageObjectFrom_forth.getBounds();
		bounds.y = y;
		label.setBounds(bounds);
		
		label = initLabel(this, y, "EventType", NO_TOOLTIP);
		bounds = cbxChangeEventType_forth.getBounds();
		bounds.y = y;
		label.setBounds(bounds);		

		label = initLabel(this, y, "UI Element *", NO_TOOLTIP);
		bounds = txtEventTriggerName_forth.getBounds();
		bounds.y = y;
		label.setBounds(bounds);		
		
		label = initLabel(this, y, "PageObject", NO_TOOLTIP);
		bounds = txtPageObjectTo_forth.getBounds();
		bounds.y = y;
		label.setBounds(bounds);		

		label = initLabel(this, y, "Condition", NO_TOOLTIP);
		bounds = txtPageObjectWait_forth.getBounds();
		bounds.y = y;
		label.setBounds(bounds);		
	}
	
	private void initInfoLabel()
	{
		yPos += deltaY;
		JLabel label = initLabel(this, yPos, "* Leave 'UI Element' empty to avoid PageChangeEvent creation.", NO_TOOLTIP);
		Font font = labelFont.deriveFont(Font.PLAIN);
		label.setFont(font);
		label.setBounds(SysNatDialog.xPosFirstColumn, yPos, SysNatDialog.frameWidth -SysNatDialog.xPosFirstColumn , SysNatDialog.lineHeight);
	}

	private void initPageChangeEventComponents_forth()
	{
		yPos += deltaY * 2;
		initComboPageObjectFrom_forth();
		initComboChangeEventType_forth();
		initTextFieldEventTriggerName_forth();
		initTextFieldPageObjectTo_forth();
		initTextFieldWaitCondition_forth();
	}

	private void initPageChangeEventComponents_back()
	{
		yPos += deltaY;
		initTextFieldPageObjectFrom_back();
		initComboChangeEventType_back();
		initTextFieldEventTriggerName_back();
		initComboPageObjectTo_back();
		initTextFieldWaitCondition_back();
	}
	
	private void initComboTestApplication()
	{
		yPos += deltaY;
		String tooltip = configDataMap.get(SysNatConstants.TEST_APPLICATION_SETTING_KEY).tooltip;
		initLabel(this, yPos, "for Test Application", tooltip);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxTestApplication = initCombo(this, options, yPos,
		                               executionInfo.getTestApplicationName(), tooltip, "GeneratePageObjectPanel_cbxTestApplication");
		cbxTestApplication.addItemListener(testApplicationListener);
	}	

	private void initTextFieldPageObjectName()
	{
		yPos += deltaY;
		initLabel(this, yPos, "Name of PageObject", NO_TOOLTIP);
		txtPageObjectName = initTextField(this, yPos, "", NO_TOOLTIP, LinePosition.SINGLE, "txtPageObjectName");
		txtPageObjectName.addKeyListener(pageObjectNameListener);
	}
	
	private void initComboPageObjectFrom_forth()
	{
		String tooltip = TOOLTIP_PAGEOBJECT_FROM;
		JLabel label = initLabel(this, yPos, "from", tooltip);
		label.setBounds(xPos, yPos, 50, 26);
		String[] options = existingPageObbjects.toArray(new String[existingPageObbjects.size()]);
		options = ArrayUtils.add( options, EVERYWHERE);
		cbxPageObjectFrom_forth = initCombo(this, options, yPos, null, tooltip, "cbxPageObjectFrom_forth");
		xPos = getRightSidePosition(label);
		cbxPageObjectFrom_forth.setBounds(xPos, yPos, 200, 26);
		cbxPageObjectFrom_forth.addItemListener(new ItemListener() {
			@Override public void itemStateChanged(ItemEvent e) {
				checkEventType();
				showOrHideHidePanel();
			}
		});
	}

	private void checkEventType()
	{
		if (EVERYWHERE.equals(cbxPageObjectFrom_forth.getSelectedItem().toString())) {
			cbxChangeEventType_forth.setSelectedItem(EventType.MenuItemClick.name());
		}
		
	}

	private void showOrHideHidePanel()
	{
		if (hidePanel == null) {
			hidePanel = new JPanel();
			hidePanel.setBounds(0, cbxPageObjectTo_back.getLocation().y, SysNatDialog.frameWidth, SysNatDialog.lineHeight);
			this.add(hidePanel);
			this.setComponentZOrder(hidePanel, 0);
			this.updateUI();
		}
		if (EventType.ButtonClick.name().equals(cbxPageObjectFrom_forth.getSelectedItem().toString())) {
			hidePanel.setVisible(false);
		} else {
			hidePanel.setVisible(true);
		}
	}

	private void initComboChangeEventType_forth()
	{
		String tooltip = TOOLTIP_EVENT_TRIGGER_TYPE;
		JLabel label = initLabel(this, yPos, "via", tooltip);
		xPos = getRightSidePosition(cbxPageObjectFrom_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		List<String> options = Stream.of(EventType.values()).map(e -> e.name()).collect(Collectors.toList());
		cbxChangeEventType_forth = initCombo(this, options.toArray(new String[options.size()]), yPos, EventType.ButtonClick.name(), tooltip, "cbxChangeEventType_forth");
		xPos = getRightSidePosition(label);
		cbxChangeEventType_forth.setBounds(xPos, yPos, 150, 26);
	}
	
	private void initComboChangeEventType_back()
	{
		String tooltip = TOOLTIP_EVENT_TRIGGER_TYPE;
		JLabel label = initLabel(this, yPos, "via", tooltip);
		xPos = getRightSidePosition(cbxPageObjectFrom_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		List<String> options = Stream.of(EventType.values()).map(e -> e.name()).collect(Collectors.toList());
		cbxChangeEventType_back = initCombo(this, options.toArray(new String[options.size()]), yPos, EventType.ButtonClick.name(), tooltip, "cbxChangeEventType_back");
		xPos = getRightSidePosition(label);
		cbxChangeEventType_back.setBounds(xPos, yPos, 150, 26);
	}
	
	private void initTextFieldWaitCondition_forth()
	{
		String tooltip = TOOLTIP_WAIT_CONDITION;
		JLabel label = initLabel(this, yPos, "wait", tooltip);
		xPos = getRightSidePosition(txtPageObjectTo_forth) + 20;
		label.setBounds(xPos, yPos, 40, 26);
		xPos = getRightSidePosition(label);
		txtPageObjectWait_forth = initTextField(this, yPos, null, tooltip, "txtPageObjectWait_forth");
		txtPageObjectWait_forth.setBounds(xPos, yPos, 90, 26);
	}
	
	private void initTextFieldWaitCondition_back()
	{
		String tooltip = TOOLTIP_WAIT_CONDITION;
		JLabel label = initLabel(this, yPos, "wait", tooltip);
		xPos = getRightSidePosition(txtPageObjectTo_forth) + 20;
		label.setBounds(xPos, yPos, 40, 26);
		xPos = getRightSidePosition(label);
		txtPageObjectWait_back = initTextField(this, yPos, null, tooltip, "txtPageObjectWait_back");
		txtPageObjectWait_back.setBounds(xPos, yPos, 90, 26);
	}
	

	private void initTextFieldEventTriggerName_forth()
	{
		String tooltip = TOOLTIP_EVENT_TRIGGER_NAME;
		JLabel label = initLabel(this, yPos, "on", tooltip);
		xPos = getRightSidePosition(cbxChangeEventType_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		xPos = getRightSidePosition(label);
		txtEventTriggerName_forth = initTextField(this, yPos, null, tooltip, "txtEventTriggerName_forth");
		txtEventTriggerName_forth.setBounds(xPos, yPos, 150, 26);
	}

	private void initTextFieldEventTriggerName_back()
	{
		String tooltip = TOOLTIP_EVENT_TRIGGER_NAME;
		JLabel label = initLabel(this, yPos, "on", tooltip);
		xPos = getRightSidePosition(cbxChangeEventType_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		xPos = getRightSidePosition(label);
		txtEventTriggerName_back = initTextField(this, yPos, null, tooltip, "txtEventTriggerName_back");
		txtEventTriggerName_back.setBounds(xPos, yPos, 150, 26);
	}
	
	private void initTextFieldPageObjectTo_forth()
	{
		String tooltip = TOOLTIP_PAGEOBJECT_TO;
		JLabel label = initLabel(this, yPos, "to", tooltip);
		xPos = getRightSidePosition(txtEventTriggerName_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		txtPageObjectTo_forth = initTextField(this, yPos, null, tooltip, "txtPageObjectTo_forth");
		xPos = getRightSidePosition(label);
		txtPageObjectTo_forth.setBounds(xPos, yPos, 200, 26);
		txtPageObjectTo_forth.setEditable(false);
		txtPageObjectTo_forth.setBackground(Color.LIGHT_GRAY);
	}
	
	private void initTextFieldPageObjectFrom_back()
	{
		String tooltip = TOOLTIP_PAGEOBJECT_TO;
		JLabel label = initLabel(this, yPos, "from", tooltip);
		xPos = SysNatDialog.xPosFirstColumn;
		label.setBounds(xPos, yPos, 50, 26);
		txtPageObjectFrom_back = initTextField(this, yPos, null, tooltip, "txtPageObjectFrom_back");
		xPos = getRightSidePosition(label);
		txtPageObjectFrom_back.setBounds(xPos, yPos, 200, 26);
		txtPageObjectFrom_back.setEditable(false);
		txtPageObjectFrom_back.setBackground(Color.LIGHT_GRAY);
	}
	
	
	private void initComboPageObjectTo_back()
	{
		String tooltip = TOOLTIP_PAGEOBJECT_TO;
		JLabel label = initLabel(this, yPos, "to", tooltip);
		xPos = getRightSidePosition(txtEventTriggerName_forth) + 20;
		label.setBounds(xPos, yPos, 30, 26);
		String[] options = existingPageObbjects.toArray(new String[existingPageObbjects.size()]);
		cbxPageObjectTo_back = initCombo(this, options, yPos, null, tooltip, "cbxPageObjectTo_back");
		xPos = getRightSidePosition(label);
		cbxPageObjectTo_back.setBounds(xPos, yPos, 200, 26);
	}

	public void updateOptions(JComboBox<String> combobox, List<String> newItems)
	{
		combobox.removeAllItems();
		newItems.forEach(e -> combobox.addItem(e));
	}

	@Override
	protected void reset() {
		txtPageObjectName.setText("");
		txtEventTriggerName_forth.setText("");
		txtEventTriggerName_back.setText("");
		txtPageObjectTo_forth.setText("");
		txtPageObjectFrom_back.setText("");
		txtPageObjectWait_forth.setText("");
		txtPageObjectWait_back.setText("");
	}
	
	class PageObjectNameListener implements KeyListener
	{
		@Override public void keyTyped(KeyEvent e) {}		
		@Override public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e)
		{
			String text = txtPageObjectName.getText();
			txtPageObjectTo_forth.setText(text);
			txtPageObjectFrom_back.setText(text);
		}
	}
	
	class TestApplicationListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			findExistingPageObbjects();
			updateOptions(cbxPageObjectFrom_forth, existingPageObbjects);
			updateOptions(cbxPageObjectTo_back, existingPageObbjects);
		}

	}

}
