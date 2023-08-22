package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.helper.generator.IdMappingSnippetDevGen;

public class ParseCodeForIdMappingPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> cbxCodeType;
	private JLabel lblResult;
	
	public ParseCodeForIdMappingPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initComponents();
	}

	private void initComponents()
	{
		yPos = 20;
		createSectionLabel(this, yPos, "Copy code to be parsed in clipboard!");
		initComboCodeType();
		yPos += deltaY;
		lblResult = createSectionLabel(this, yPos, "");
		
		// button line
		initCancelButton(this);
		initStartButton(this);
	}

	protected void initStartButton(JPanel parent) 
	{
		initStartButton(parent, "Start generation...", null, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				ApplicationType type = ApplicationType.valueOf(cbxCodeType.getSelectedItem().toString());
				try {
					String result = IdMappingSnippetDevGen.doYourJob(type);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
					lblResult.setText("Generated Id-Mapping is now available in clipboard!");
					askToCloseDialog();
				} catch (Exception ex) {
					ex.printStackTrace();
					lblResult.setText("Sorry, a problem occurred (see logs)!");
				}
			}
		});
	}
	private void initComboCodeType()
	{
		yPos += deltaY;
		initLabel(this, yPos, "Type of application:", NO_TOOLTIP);
		String[] options = {ApplicationType.Web.name(), ApplicationType.Swing.name()};
		cbxCodeType = initCombo(this, options, yPos,
		                              ApplicationType.Web.name(), NO_TOOLTIP, "cbxCodeType");
	}
	
	@Override
	protected void reset() {
	}

}
