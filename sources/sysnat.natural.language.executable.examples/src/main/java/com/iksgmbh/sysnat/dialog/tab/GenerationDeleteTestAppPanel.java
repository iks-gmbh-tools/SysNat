package com.iksgmbh.sysnat.dialog.tab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.helper.generator.TestApplicationRemover;

public class GenerationDeleteTestAppPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> cbxTestApplication;
	
	public GenerationDeleteTestAppPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initComponents();
	}

	private void initComponents()
	{
		yPos = 100;
		initTestAppCombo();
		
		// button line
		initCancelButton(this);
		initStartButton(this);
	}

	private void initTestAppCombo()
	{
		String tooltip = configDataMap.get(SysNatConstants.TEST_APPLICATION_SETTING_KEY).tooltip;
		initLabel(this, yPos, "Test Application to delete", tooltip);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxTestApplication = initCombo(this, options, yPos, executionInfo.getTestApplicationName(), tooltip, "GenerationDeleteTestAppPanel_cbxTestApplication");
	}
	
	protected void initStartButton(JPanel parent) {
		initStartButton(parent, "Start Deletion...", null, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				startDeletion();
			}
		});
	}

	protected void startDeletion()
	{
		String testAppName = cbxTestApplication.getSelectedItem().toString();
		String message = "<html>Really delete test application '<b>" + testAppName + "</b>'?<br><br> Deletion will contain"
        + "<br>- Java code in sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/" + testAppName.toLowerCase()
        + "<br>- Property file 'sysnat.test.runtime.environment/src/main/resources/execution_properties/" + testAppName + ".properties'" 
        + "<br>- NL instructions in sysnat.natural.language.executable.examples/ExecutableExamples/" + testAppName 
        + "<br>- Test data in sysnat.natural.language.executable.examples/testdata/" + testAppName
        + "<br>- Help documentation file 'sysnat.natural.language.executable.examples/help/ExistingNLInstructions_" + testAppName + ".html'"
        + "<html>";
		String[] options = { "Yes", "No" };
		int answer = JOptionPane.showOptionDialog(frameDialog, message, "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
				
		if (answer == JOptionPane.YES_OPTION) 
		{
			message = TestApplicationRemover.doYourJob(testAppName);
			message += " Close dialog and finish generation?";
			answer = JOptionPane.showOptionDialog(frameDialog, message, "Quit Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (answer == JOptionPane.YES_OPTION) {
				frameDialog.finishSysNatExecution();
			} else {
				reset();
			}
		}
	}

	@Override
	protected void reset() {
		// not yet used here
	}
	
}
