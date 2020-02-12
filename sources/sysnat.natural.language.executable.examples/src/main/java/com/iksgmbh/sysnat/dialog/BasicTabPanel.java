package com.iksgmbh.sysnat.dialog;

import static com.iksgmbh.sysnat.dialog.SysNatDialog.buttonDistanceToLowerFrameEdge;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.firstColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.frameHeight;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.secondColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.thirdColumnLength;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosFirstColumn;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosSecondColumn;
import static com.iksgmbh.sysnat.dialog.SysNatDialog.xPosThirdColumn;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.dialog.ConfigFileParser.ConfigData;

/**
 * Parent of all panels within a tab of the SysNat Dialog.
 * 
 * @author Reik Oberrath
 */	
public abstract class BasicTabPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceBundle CONSTANTS_BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	protected static final ResourceBundle CONSTANTS_BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);
	protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.getDefault());
	protected static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/SysNatDialog", Locale.ENGLISH);

	protected static final int deltaY = 40;
	protected static final String NO_TOOLTIP = "NO_TOOLTIP";

	protected static final Font labelFont = new Font("Arial", Font.BOLD, 16);
	protected static final Font buttonFont = new Font("Arial", Font.BOLD, 16);
	protected static final Font labelSectionFont = new Font("Arial", Font.ITALIC, 18);
	protected static final Font fieldFont = new Font("Arial", Font.PLAIN, 18);

	protected int yPos = 10;
	protected SysNatDialog sysnatDialog;
	protected ExecutionRuntimeInfo executionInfo;
	protected HashMap<String, ConfigData> configDataMap;
	protected String configFileName;

	public BasicTabPanel(SysNatDialog aDialog, String aConfigFileName) 
	{
		this.sysnatDialog = aDialog;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.configFileName = aConfigFileName;
		this.configDataMap = ConfigFileParser.doYourJob(configFileName, executionInfo.getKnownTestApplications());
	}

	protected JCheckBox initCheckbox(final JPanel parent,
	        						 final String title,
	        						 final boolean tickInitially,
	        						 final int yPos)
	{
		JCheckBox ckeckbox = new JCheckBox(title);

		ckeckbox.setBounds(xPosFirstColumn, yPos, secondColumnLength, 20);
		ckeckbox.setFont(labelFont);
		ckeckbox.setSelected(tickInitially);

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

	protected JTextField initTextField(JPanel parent, int yPos, String initialText, String toolTipText)
	{
		final JTextField txtField = new JTextField();
		initTextField(parent, txtField, yPos, initialText, toolTipText);
		return txtField;
	}

	protected JTextField initTextField(JPanel parent, int yPos, String initialText) {
		return initTextField(parent, yPos, initialText, "");
	}

	protected void initTextField(final JPanel parent,
			                   final JTextField txtField,
						       final int yPos,
						       final String initialText,
						       final String tooltip)
	{
		txtField.setBounds(xPosSecondColumn, yPos, secondColumnLength, 22);
		txtField.setFont(fieldFont);
		txtField.setText(initialText);
		if (isTooltipNotEmpty(tooltip)) txtField.setToolTipText(tooltip);

		parent.add(txtField);
	}

	protected JComboBox<String> initCombo(JPanel parent,
							              String[] optionList, 
							              int yPos, 
							              String initialSelection,
							              String tooltip)
	{
		return initCombo(parent, optionList, yPos, initialSelection, tooltip, false);
	}

	protected JComboBox<String> initCombo(JPanel parent,
			                              String[] optionList, 
			                              int yPos, 
			                              String initialSelection,
			                              String tooltip,
			                              boolean editable)
	{
		final JComboBox<String> cbx = new JComboBox<String>(optionList);
		String initialOption = null;
		for (String option : optionList) {
			if (option.equals(initialSelection)) {
				initialOption = option;
			}
		}
		if (initialOption == null) {
			initialOption = optionList[0];
		}

		cbx.setEditable(editable);
		cbx.setBounds(xPosSecondColumn, yPos, secondColumnLength, 26);
		cbx.setFont(fieldFont);
		cbx.setBackground(Color.WHITE);
		cbx.setSelectedItem(initialOption);
		if (isTooltipNotEmpty(tooltip)) cbx.setToolTipText(tooltip);
		
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

	protected void initSectionLabel(JPanel parent, String keyInBundle, int yPos)
	{
		final JLabel lbl = new JLabel();

		lbl.setText(BUNDLE.getString(keyInBundle));
		lbl.setBounds(xPosFirstColumn, yPos, firstColumnLength + secondColumnLength, 20);
		lbl.setFont(labelSectionFont);

		parent.add(lbl);
	}

	protected JButton initButton(JPanel parent, String text, int yPos, String tooltip)
	{
		text = BUNDLE.getString(text);
		final JButton btn = new JButton(text);

		btn.setBounds(xPosThirdColumn, yPos, thirdColumnLength, 20);
		btn.setFont(buttonFont);
		if (isTooltipNotEmpty(tooltip)) btn.setToolTipText(tooltip);
		
		parent.add(btn);
		return btn;
	}

	protected JLabel initLabel(JPanel parent, String keyInBundle, int yPos, String tooltip)
	{
		final JLabel lbl = new JLabel();
		parent.add(lbl);
		
		lbl.setText(BUNDLE.getString(keyInBundle));
		lbl.setBounds(xPosFirstColumn, yPos, firstColumnLength, 20);
		lbl.setFont(labelFont);
		if (isTooltipNotEmpty(tooltip)) lbl.setToolTipText(tooltip);

		return lbl;
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
			sysnatDialog.saveCurrentSettings();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			sysnatDialog.saveCurrentSettings();
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
			sysnatDialog.terminateSysNatExecution();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			sysnatDialog.terminateSysNatExecution();
		}
	}

}
