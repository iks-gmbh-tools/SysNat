package com.iksgmbh.sysnat.dialog.helper;

import static com.iksgmbh.sysnat.dialog.SysNatDialog.buttonDistanceToLowerFrameEdge;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.firstColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.frameHeight;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.lineHeight;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.secondColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.thirdColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosFirstColumn;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosThirdColumn;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.ConfigFileParser.ConfigData;
import com.iksgmbh.sysnat.dialog.tab.GenerateTestAppPanel;
import com.iksgmbh.sysnat.domain.TestApplication;

/**
 * Parent of all panels within a tab of the SysNat Dialog.
 * 
 * @author Reik Oberrath
 */	
public abstract class BasicTabPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	public enum LinePosition { SINGLE, FirstOfTwo, SecondOfTwo, FirstInTripple, SecondInTripple, ThirdInTripple };
	
	protected static final ResourceBundle CONSTANTS_BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	protected static final ResourceBundle CONSTANTS_BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);
	protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.getDefault());
	protected static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.ENGLISH);

	protected static final int deltaY = 40;
	protected static final String NO_TOOLTIP = "NO_TOOLTIP";

	protected static final Font labelFont = new Font("Arial", Font.BOLD, 16);
	protected static final Font labelFontPlain = new Font("Arial", Font.PLAIN, 16);
	protected static final Font buttonFont = new Font("Arial", Font.BOLD, 16);
	protected static final Font labelSectionFont = new Font("Arial", Font.ITALIC, 18);
	protected static final Font fieldFont = new Font("Arial", Font.PLAIN, 18);

	protected int yPos = 10;
	protected SysNatDialog frameDialog;
	protected ExecutionRuntimeInfo executionInfo;
	protected HashMap<String, ConfigData> configDataMap;
	protected String configFileName;

	public BasicTabPanel(SysNatDialog aDialog) {
		this(aDialog, null);
	}

	public BasicTabPanel(SysNatDialog aDialog, String aConfigFileName) 
	{
		this.frameDialog = aDialog;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.configFileName = aConfigFileName;
		if (configFileName != null) {
			this.configDataMap = ConfigFileParser.doYourJob(configFileName, executionInfo.getKnownTestApplications());
		}
	}

	protected abstract void reset();
	
	protected JCheckBox initCheckbox(final JPanel parent,
	        						 final String title,
	        						 final String tooltip,
	        						 final boolean tickInitially,
	        						 final int xPos,
	        						 final int yPos) 
	{
		JCheckBox ckeckbox = new JCheckBox(title);

		if (xPos == xPosFirstColumn) {
			ckeckbox.setBounds(xPosFirstColumn, yPos, firstColumnLength, 20);
			ckeckbox.setFont(labelFont);
		} else {
			ckeckbox.setFont(labelFontPlain);
			ckeckbox.setBounds(xPosThirdColumn, yPos, thirdColumnLength, 20);
		}
		ckeckbox.setSelected(tickInitially);
		if (tooltip != null) ckeckbox.setToolTipText(tooltip);

		parent.add(ckeckbox);
		return ckeckbox;
	}


	protected void initSaveButton(JPanel parent)
	{
		final JButton saveButton = new JButton(BUNDLE.getString("SAVE_BUTTON_TEXT"));
		final SaveActionListener saveActionListener = new SaveActionListener();
		final int buttonWidth = 180;
		final int xPos = (xPosThirdColumn + thirdColumnLength - xPosFirstColumn - buttonWidth)/2;
		final int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		
		saveButton.setToolTipText(BUNDLE.getString("SAVE_BUTTON_TOOLTIP"));
		saveButton.addActionListener(saveActionListener);
		saveButton.addKeyListener(saveActionListener);
		saveButton.setBounds(xPos, yPos, buttonWidth, 30);
		saveButton.setFont(labelFont);

		parent.add(saveButton);
	}
	
	protected JButton initResetButton(JPanel parent)
	{
		final JButton resetButton = new JButton("Reset");
		final int buttonWidth = 180;
		final int xPos = (xPosThirdColumn + thirdColumnLength - xPosFirstColumn - buttonWidth)/2;
		final int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		
		ResetActionListener resetActionListener = new  ResetActionListener();
		resetButton.addActionListener(resetActionListener);
		resetButton.addKeyListener(resetActionListener);
		resetButton.setBounds(xPos, yPos, buttonWidth, 30);
		resetButton.setFont(labelFont);

		parent.add(resetButton);
		return resetButton;
	}		
	
	protected JButton initStartButton(JPanel parent, String text, String tooltip, EventListener listener)
	{
		final JButton startButton = new JButton(text);
		startButton.setToolTipText(tooltip);
		if (listener instanceof ActionListener) startButton.addActionListener((ActionListener) listener);
		if (listener instanceof KeyListener) startButton.addKeyListener((KeyListener) listener);
		
		final int yPos = frameHeight - buttonDistanceToLowerFrameEdge;
		final int buttonWidth = 200;
		final int xPos = xPosThirdColumn + thirdColumnLength - buttonWidth;

		startButton.setBounds(xPos, yPos, buttonWidth, 30);
		startButton.setFont(buttonFont);
		startButton.setBackground(Color.GREEN);
		startButton.setForeground(Color.BLACK);

		parent.add(startButton);
		return startButton;
	}

	protected void initCancelButton(JPanel parent)
	{
		final JButton cancelButton = new JButton(BUNDLE.getString("CANCEL_BUTTON_TEXT"));
		final CancelActionListener cancelActionListener = new CancelActionListener();
		int yPos = frameHeight - buttonDistanceToLowerFrameEdge;

		cancelButton.setToolTipText(BUNDLE.getString("CANCEL_BUTTON_TOOLTIP"));
		cancelButton.addActionListener(cancelActionListener);
		cancelButton.addKeyListener(new CancelActionListener());
		cancelButton.setBounds(xPosFirstColumn, yPos, 140, 30);
		cancelButton.setFont(buttonFont);
		cancelButton.setBackground(Color.ORANGE);
		cancelButton.setForeground(Color.BLACK);

		parent.add(cancelButton);
	}

	protected JTextField initTextField(JPanel parent, int yPos, String initialText, String toolTipText, String fieldId) {
		return initTextField(parent, yPos, initialText, toolTipText, LinePosition.SINGLE, fieldId);
	}
	
	protected JTextField initTextField(JPanel parent, int yPos, String initialText, String toolTipText, LinePosition linePosition, String fieldId)
	{
		final JTextField txtField = new JTextField();
		initTextField(parent, txtField, yPos, initialText, toolTipText, linePosition, fieldId);
		return txtField;
	}

	protected void initTextField(final JPanel parent,
			                     final JTextField txtField,
						         final int yPos,
						         final String initialText,
						         final String tooltip, 
						         final LinePosition linePosition,
						         final String fieldId)
	{		
		int xPos1 = getFirstColumnXPos();
		int len1 = getFirstColumnLength();
		int xPos2 = getSecondColumnXPos();
		int len2 = getSecondColumnLength();
		int xPos3 = getThirdColumnXPos();
		int len3 = getThirdColumnLength();
		switch (linePosition) {
			case SINGLE: txtField.setBounds(xPos2, yPos, len2 + len3, lineHeight); break;
			case FirstInTripple: txtField.setBounds(xPos1 + len1/2, yPos, len1/2, lineHeight); break;
			case SecondInTripple: txtField.setBounds(xPos2 + len2/2, yPos, len2/2, lineHeight); break;
			case ThirdInTripple: txtField.setBounds(xPos3 + len3/2, yPos, len3/2, lineHeight); break;
			default: throw new RuntimeException("LineLayout '" + linePosition.name() + "' not yet supported");
		}
		
		txtField.setFont(fieldFont);
		txtField.setText(initialText);
		txtField.setName(fieldId);
		if (isTooltipNotEmpty(tooltip)) txtField.setToolTipText(tooltip);

		parent.add(txtField);
	}

	protected JComboBox<String> initCombo(JPanel parent,
							              String[] optionList, 
							              int yPos, 
							              String initialSelection,
							              String tooltip, 
							              String fieldId)
	{
		return initCombo(parent, optionList, yPos, initialSelection, tooltip, false, LinePosition.SINGLE, fieldId);
	}

	protected JComboBox<String> initCombo(JPanel parent,
			                              String[] optionList, 
			                              int yPos, 
			                              String initialValue,
			                              String tooltip,
			                              boolean editable,
			                              LinePosition linePosition, 
			                              String fieldId)
	{
		final JComboBox<String> cbx = new JComboBox<String>(optionList);

		cbx.setEditable(editable);
		cbx.setFont(fieldFont);
		cbx.setBackground(Color.WHITE);
		if (Arrays.asList(optionList).contains(initialValue)) {
			cbx.setSelectedItem(initialValue);
		} else {
			if (initialValue != null && initialValue.trim().length() > 0) {
				((JTextField)cbx.getEditor().getEditorComponent()).setText(initialValue);
			}
			else if (optionList.length > 0) {
				cbx.setSelectedItem(optionList[0]);
			}
		}
		
		cbx.getSelectedItem();
		if (isTooltipNotEmpty(tooltip)) cbx.setToolTipText(tooltip);
		
		int xPos1 = getFirstColumnXPos();
		int len1 = getFirstColumnLength();
		int xPos2 = getSecondColumnXPos();
		int len2 = getSecondColumnLength();
		int xPos3 = getThirdColumnXPos();
		int len3 = getThirdColumnLength();
		switch (linePosition) {
			case SINGLE: cbx.setBounds(xPos2, yPos, len2 + len3, lineHeight); break;
			case FirstInTripple: cbx.setBounds(xPos1 + len1/2, yPos, len1/2, lineHeight); break;
			case SecondInTripple: cbx.setBounds(xPos2 + len2/2, yPos, len2/2, lineHeight); break;
			case ThirdInTripple: cbx.setBounds(xPos3 + len3/2, yPos, len3/2, lineHeight); break;
			default: throw new RuntimeException("LineLayout '" + linePosition.name() + "' not yet supported");
		}
		
		parent.add(cbx);
		return cbx;
	}


	private boolean isTooltipNotEmpty(String tooltip)
	{
		return tooltip != null
			   && tooltip.trim().length() > 0 
			   && ! tooltip.equals("<html></html>")
			   && tooltip != NO_TOOLTIP;
	}

	protected void createSectionLabel(JPanel parent, String keyInBundle, int yPos) {
		createSectionLabel(parent, yPos, BUNDLE.getString(keyInBundle));
	}
	
	protected JLabel createSectionLabel(JPanel parent, int yPos, String labelText)
	{
		final JLabel toReturn = new JLabel();

		toReturn.setText(labelText);
		toReturn.setBounds(xPosFirstColumn, yPos, firstColumnLength + secondColumnLength, lineHeight);
		toReturn.setFont(labelSectionFont);

		parent.add(toReturn);
		return toReturn;
	}
	
	protected JButton initButton(JPanel parent, String text, int yPos, String tooltip) {
		return initButton(parent, yPos, BUNDLE.getString(text), tooltip);
	}
	
	protected JButton initButton(JPanel parent, int yPos, String text, String tooltip)
	{
		final JButton btn = new JButton(text);

		btn.setBounds(xPosThirdColumn, yPos, thirdColumnLength, lineHeight);
		btn.setFont(buttonFont);
		if (isTooltipNotEmpty(tooltip)) btn.setToolTipText(tooltip);
		
		parent.add(btn);
		return btn;
	}

	protected JLabel initLabel(JPanel parent, String keyInBundle, int yPos, String tooltip) {
		return initLabel(parent, yPos, BUNDLE.getString(keyInBundle), tooltip, LinePosition.SINGLE);
	}

	protected JLabel initLabel(JPanel parent, int yPos, String labelText, String tooltip) {
		return initLabel(parent, yPos, labelText, tooltip, LinePosition.SINGLE);
	}
	
	protected JLabel initLabel(JPanel parent, int yPos, String labelText, String tooltip, LinePosition linePosition)
	{
		final JLabel lbl = new JLabel();
		parent.add(lbl);
		
		lbl.setText(labelText);
		lbl.setFont(labelFont);
		if (isTooltipNotEmpty(tooltip)) lbl.setToolTipText(tooltip);

		int xPos1 = getFirstColumnXPos();
		int len1 = getFirstColumnLength();
		int xPos2 = getSecondColumnXPos();
		int len2 = getSecondColumnLength();
		int xPos3 = getThirdColumnXPos();
		int len3 = getThirdColumnLength();
		switch (linePosition) {
			case SINGLE: lbl.setBounds(xPos1, yPos, len1, lineHeight); break;
			case FirstInTripple: lbl.setBounds(xPos1, yPos, len1, lineHeight); break;
			case SecondInTripple: lbl.setBounds(xPos2+10, yPos, len2/2-10, lineHeight); break;
			case ThirdInTripple: lbl.setBounds(xPos3+10, yPos, len3/2-10, lineHeight); break;
			default: throw new RuntimeException("LineLayout '" + linePosition.name() + "' not yet supported");
		}

		return lbl;
	}	
	

	protected String toEnvSysNatEnum(String environment, String testApplicationName)
	{
		HashMap<String, String> envMap = new HashMap<>();
		List<String> knownEnvironments = executionInfo.getKnownEnvironments(executionInfo.getTestApplicationName());
		TestApplication testApplication = new TestApplication(testApplicationName);
		knownEnvironments.forEach(env -> envMap.put(executionInfo.getDisplayName(env, testApplication), env));
		String toReturn = envMap.get(environment);
		if (toReturn == null) toReturn = environment;
		return toReturn;
	}
	
	protected String toEnvSpecificToTestApplication(String environment, String testApplicationName)
	{
		HashMap<String, String> envMap = new HashMap<>();
		List<String> knownEnvironments = executionInfo.getKnownEnvironments(executionInfo.getTestApplicationName());
		TestApplication testApplication = new TestApplication(testApplicationName);
		knownEnvironments.forEach(env -> envMap.put(env, executionInfo.getDisplayName(env, testApplication)));
		String toReturn = envMap.get(environment);
		if (toReturn == null) toReturn = environment;
		return toReturn;
	}
	
	protected int getRightSidePosition(JComponent c) {
		return c.getLocation().x + c.getSize().width;
	}
	
	protected int getLeftSidePosition(JComponent c) {
		return c.getLocation().x;
	}
	
	protected HorizontalLine createSeparationLine(JPanel parent, int yPos) {
		return new HorizontalLine(parent, Color.BLACK, xPosFirstColumn, yPos, xPosThirdColumn + thirdColumnLength - xPosFirstColumn, 2);
	}
	
	protected JPanel createSubPanel(int numberOfDataRows, int yPos, GenerateTestAppPanel parent)
	{
		JPanel toReturn = new JPanel();
		toReturn.setLayout(null);
		
		int height = numberOfDataRows * deltaY - (deltaY - lineHeight);
		toReturn.setBounds(0, yPos, SysNatDialog.frameWidth, height);
		
		yPos += height;
		parent.add(toReturn);
		return toReturn;
	}
	
	protected int getFirstColumnLength() {
		return firstColumnLength;
	}
	
	protected int getSecondColumnLength() {
		return secondColumnLength;
	}
	
	protected int getThirdColumnLength() {
		return thirdColumnLength;
	}
	
	protected int getFirstColumnXPos() {
		return xPosFirstColumn;
	}
	
	protected int getSecondColumnXPos() {
		return getFirstColumnXPos()  + getFirstColumnLength();
	}
	
	protected int getThirdColumnXPos() {
		return getSecondColumnXPos() + getSecondColumnLength();
	}	
	
	protected int getFullLength() {
		return getFirstColumnLength() + getSecondColumnLength() + getThirdColumnLength();
	}
	
	

	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################
	
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
			frameDialog.saveCurrentSettings();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			frameDialog.saveCurrentSettings();
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
			frameDialog.terminateSysNatExecution();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			frameDialog.terminateSysNatExecution();
		}
	}

	class ResetActionListener implements ActionListener, KeyListener
	{
		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) { reset(); }
		@Override public void actionPerformed(ActionEvent arg0) { reset(); }
	}

	
	
}
