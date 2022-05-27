package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen;
import com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType;
import com.iksgmbh.sysnat.utils.NaturalLanguageAnalyseUtil;

public class GenerateNLInstructionPanel extends BasicTabPanel
{
	private static final String STANDARD_STORE_TEST_OBJECT_METHOD = "storeTestObject_";
	private static final long serialVersionUID = 1L;
	private static final String INVALID_JAVA_CHARS = "'\"<>()[]{}/\\?!§$%&+*:;#=";
	
	private JLabel lblParameterName, lblMethodeName, lblReportEntry;
	private JTextField txtNaturalLanguageExpression, txtMethodeName, txtParameterName, txtReportEntry;
	private JButton btnAnalyseApply;
	private JLabel lblAnalyseResult, lblPreview;
	
	private LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	private JComboBox<String> cbxTestApplication;
	private boolean readyToGenerate = false;

	
	public GenerateNLInstructionPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initComponents();
	}

	private void startAnalyse()
	{
		lblAnalyseResult.setVisible(false);
		
		if (txtReportEntry.isVisible()) 
		{
			if (txtReportEntry.getText().trim().isEmpty()) {
				lblPreview.setText("Enter a test report message!");
				return;
			}
			initForSuccess();
			return;
		}
			
		if (txtMethodeName.isVisible()) 
		{
			if (! isJavaValid(txtMethodeName.getText().trim(), "Methode Name")) {
				return;
			}
			initReportEntryFields();
			return;
		}

		if (txtParameterName.isVisible()) {
			if (! isJavaValid(txtParameterName.getText().trim(), "Parameter Name")) {
				return;
			}
		}
		
		String nl = txtNaturalLanguageExpression.getText().trim();
		String result = NaturalLanguageAnalyseUtil.analyseNL(nl, txtParameterName.getText().trim(), parameters);
		
		if (result.startsWith(NaturalLanguageAnalyseUtil.UNKOWN_VALUE_PARAMETER)) {
			String value = result.substring(NaturalLanguageAnalyseUtil.UNKOWN_VALUE_PARAMETER.length()).trim();
			initParameterNameFields("Parameter Name for value \"" + value + "\":");
			setNaturalLanguageExpressionFieldEditable(false);
			showParametersInPreviewLabel();
		} else if (result.startsWith(NaturalLanguageAnalyseUtil.UNKOWN_TESTOBJECT_PARAMETER)) {
			String value = result.substring(NaturalLanguageAnalyseUtil.UNKOWN_TESTOBJECT_PARAMETER.length()).trim();
			initParameterNameFields("Test Object Name for value '" + value + "':");
			setNaturalLanguageExpressionFieldEditable(false);
			showParametersInPreviewLabel();
		} else if (result.startsWith(NaturalLanguageAnalyseUtil.PROBLEM)) {
			initProblemLabel(result);
			lblPreview.setText("");
		} else {
			String testObjectName = extractTestObjectName(result);
			if (testObjectName.isEmpty()) {
				initTestMethodFields("");
			} else {
				initTestMethodFields(STANDARD_STORE_TEST_OBJECT_METHOD + testObjectName);
			}
			setNaturalLanguageExpressionFieldEditable(false);
			showParametersInPreviewLabel();
		}
	}
	
	private void showParametersInPreviewLabel()
	{
		if (parameters.isEmpty()) {
			lblPreview.setText("");	
		} else {			
			final StringBuffer parameterPreview = new StringBuffer();
			AtomicInteger counter = new AtomicInteger(1);
			parameters.keySet().forEach(key -> parameterPreview.append(counter.getAndIncrement()).append(". Parameter: ")
					                                           .append(getStringRepresentation(key, parameters.get(key)))
					                                           .append("<br>"));
			lblPreview.setText("<html>List of defined parameters (they become method arguments):<br>" + parameterPreview.toString() + "</html>");
		}
	}
	
	private String getStringRepresentation(String key, String value)
	{
		if (value.isEmpty()) return key;
		return key + " = " + value;
	}

	private String extractTestObjectName(String result)
	{
		int pos = result.indexOf("<");
		if (pos == -1) return "";
		String toReturn = result.substring(pos+1);
		pos = toReturn.indexOf(">");
		return toReturn.substring(0, pos);
	}

	private void setNaturalLanguageExpressionFieldEditable(boolean ok) 
	{
		if (ok) {
			txtNaturalLanguageExpression.setEditable(true);
			txtNaturalLanguageExpression.setBackground(Color.WHITE);
		} else {
			txtNaturalLanguageExpression.setEditable(false);
			txtNaturalLanguageExpression.setBackground(new Color(210, 210, 210));
		}
	}
	
	@Override
	protected  void reset()
	{
		parameters.clear();
		
		txtMethodeName.setText("");
		txtMethodeName.setVisible(false);
		txtParameterName.setText("");
		txtParameterName.setVisible(false);
		txtReportEntry.setText("");
		txtReportEntry.setVisible(false);
		
		lblMethodeName.setVisible(false);
		lblParameterName.setVisible(false);
		lblReportEntry.setVisible(false);
		
		txtNaturalLanguageExpression.setText("");
		setNaturalLanguageExpressionFieldEditable(true);
		txtNaturalLanguageExpression.requestFocus();
		
		lblPreview.setText("");
		lblAnalyseResult.setText("");
		lblAnalyseResult.setVisible(true);
		btnAnalyseApply.setVisible(true);
		btnAnalyseApply.setBounds(SysNatDialog.xPosFirstColumn, btnAnalyseApply.getLocation().y, 100, SysNatDialog.lineHeight);
		btnAnalyseApply.setText("Analyse");
	}
	
	
	private boolean isJavaValid(String expression, String expressionpName)
	{
		if (expression.isEmpty()) {
			lblPreview.setText("Enter " + expressionpName + "!");
			return false;
		}
		
		char[] charArray = INVALID_JAVA_CHARS.toCharArray();
		List<String> badChars = new ArrayList<>();
		for (char c : charArray) {
			if (expression.contains("" + c)) {
				badChars.add("" + c);
			}
		}
		if (! badChars.isEmpty()) {
			lblPreview.setText(expressionpName + " contains invalid chars: " + SysNatStringUtil.listToString(badChars, ","));
			return false;
		}
		return true;
	}

	private void initForSuccess()
	{
		lblReportEntry.setVisible(false);
		txtReportEntry.setVisible(false);
		btnAnalyseApply.setVisible(false);
		
		lblAnalyseResult.setVisible(true);
		lblAnalyseResult.setText("Ready to generate the following code into class LanguageTemplatesBasics_" + cbxTestApplication.getSelectedItem() + ": ");
		lblAnalyseResult.setLocation(SysNatDialog.xPosFirstColumn, lblAnalyseResult.getLocation().y);
		lblPreview.setText(getPreview());
		readyToGenerate = true;
	}

	private String getPreview()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = collectGenerationData();
		String toReturn = NaturalLanguageMethodDevGen.createPreview(data);
		return "<html>" + toReturn.replace(System.getProperty("line.separator"), "<br>")
		                          .replace("    ", "&nbsp;&nbsp;&nbsp;&nbsp;") +   
		       "</html>";
	}

	private LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> collectGenerationData()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		data.put(DataType.NLExpression, txtNaturalLanguageExpression.getText().replace("  ", " "));
		data.put(DataType.MethodeName, txtMethodeName.getText().replace(" ", ""));
		data.put(DataType.ReportMessage, txtReportEntry.getText().trim().replace("<", "&lt;").replace(">", "&gt;"));
		data.put(DataType.TestApplication, cbxTestApplication.getSelectedItem().toString());
		data.put(DataType.MethodParameter, toString(parameters));
		return data;
	}

	private String toString(LinkedHashMap<String, String> linkedHashMap)
	{
		if (linkedHashMap.isEmpty()) return "";
		StringBuffer sb = new StringBuffer();
		linkedHashMap.keySet().forEach(e -> sb.append(e).append(linkedHashMap.get(e)).append(NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR));
		return sb.toString().substring(0, sb.toString().length() - NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR.length());
	}

	private void initComponents()
	{
		yPos = 30;
		initComboTestApplication();
		createSectionLabel(this, yPos, "Natural Language Expression from nlxx or nls file:");
		initNaturalLanguageTextfield();
		initAnalyseButton();
		initLabelResult();
		initTextFieldParameterName();
		initMethodeNameTextfield();
		initReportEntryTextfield();
		initLabelPreview();
		
		// button line
		initCancelButton(this);
		initResetButton(this);
		initStartButton(this);
	}

	private void initComboTestApplication()
	{
		String tooltip = configDataMap.get(SysNatConstants.TEST_APPLICATION_SETTING_KEY).tooltip;
		initLabel(this, yPos, "for Test Application", tooltip);
		List<String> knownTestApps = executionInfo.getKnownTestApplications();
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		cbxTestApplication = initCombo(this, options, yPos, executionInfo.getTestApplicationName(), tooltip, "GenerateNLInstructionPanel_cbxTestApplication");
		yPos += deltaY;
	}		

	private void initLabelResult()
	{
		lblAnalyseResult = new JLabel("result");
		int xPos = getRightSidePosition(btnAnalyseApply) + 15;
		lblAnalyseResult.setBounds(xPos, yPos, getFullLength(), SysNatDialog.lineHeight);
		this.add(lblAnalyseResult);
		lblAnalyseResult.setVisible(false);
	}
	
	private void initLabelPreview()
	{
		yPos += deltaY;
		lblPreview = new JLabel();
		lblPreview.setVerticalTextPosition(SwingConstants.TOP);
		lblPreview.setFont(new Font("Courier", Font.PLAIN, 14));
		lblPreview.setBounds(SysNatDialog.xPosFirstColumn, yPos, getFullLength(), SysNatDialog.lineHeight*8);
		this.add(lblPreview);
	}

	private void initAnalyseButton()
	{
		yPos += deltaY;
		btnAnalyseApply = new JButton("Analyse");
		btnAnalyseApply.setBounds(SysNatDialog.xPosFirstColumn, yPos, 100, SysNatDialog.lineHeight);
		this.add(btnAnalyseApply);
		btnAnalyseApply.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {startAnalyse();}
		});
	}

	private void initNaturalLanguageTextfield()
	{
		yPos += deltaY;
		txtNaturalLanguageExpression = new JTextField();
		txtNaturalLanguageExpression.setBounds(SysNatDialog.xPosFirstColumn, yPos, getFullLength(), SysNatDialog.lineHeight);
		this.add(txtNaturalLanguageExpression);
	}

	private void initMethodeNameTextfield()
	{
		String tooltip = "Name of Java method that will be generated for this Natural Language Expression";
		lblMethodeName = initLabel(this, yPos, "Methode Name", tooltip);
		lblMethodeName.setBounds(SysNatDialog.xPosFirstColumn, btnAnalyseApply.getLocation().y, getLabelWidth(lblMethodeName) +20 , SysNatDialog.lineHeight);
		lblMethodeName.setVisible(false);
		
		txtMethodeName = initTextField(this, yPos, "", tooltip, "txtMethodeName");
		int xPos = getRightSidePosition(lblMethodeName) + 10;
		txtMethodeName.setBounds(xPos, yPos, 700, SysNatDialog.lineHeight);
		txtMethodeName.setVisible(false);
	}
	
	
	private void initReportEntryTextfield()
	{
		String tooltip = "Message text to add to test report when this instruction is executed";
		lblReportEntry = initLabel(this, yPos, "Test report message", tooltip);
		lblReportEntry.setBounds(SysNatDialog.xPosFirstColumn, btnAnalyseApply.getLocation().y, getLabelWidth(lblReportEntry) +20 , SysNatDialog.lineHeight);
		lblReportEntry.setVisible(false);
		
		txtReportEntry = initTextField(this, yPos, "", tooltip, "txtReportEntry");
		int xPos = getRightSidePosition(lblReportEntry) + 10;
		txtReportEntry.setBounds(xPos, yPos, 700, SysNatDialog.lineHeight);
		txtReportEntry.setVisible(false);
	}

	
	private void initTextFieldParameterName()
	{
		String tooltip = "Which key belongs to this value? It will be used as argrument in the method signature.";
		lblParameterName = initLabel(this, yPos, "", tooltip);
		txtParameterName = initTextField(this, yPos, "", tooltip, "txtParameterName");
		
		lblParameterName.setVisible(false);
		txtParameterName.setVisible(false);
	}

	protected void initStartButton(JPanel parent) 
	{
		initStartButton(parent, "Start generation...", null, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e)
			{
				if (readyToGenerate) {
					String result = NaturalLanguageMethodDevGen.doYourJob(collectGenerationData());
					if (result.startsWith("Error:")) {
						System.err.println(result);
						JOptionPane.showMessageDialog(frameDialog, result, "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						String message = "Close dialog and finish generation?" + System.getProperty("line.separator") 
						                 + System.getProperty("line.separator") + "Generation Info:" + System.getProperty("line.separator")
						                 + result + System.getProperty("line.separator") + System.getProperty("line.separator");
						String[] options = { "Yes", "No" };
						int answer = JOptionPane.showOptionDialog(frameDialog, message, "Generation successful!", 
								     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
						if (answer == JOptionPane.YES_OPTION) {
							frameDialog.finishSysNatExecution();
						} else {
							cbxTestApplication.remove(cbxTestApplication.getSelectedIndex());
							cbxTestApplication.getSelectedIndex();
						}
					}
				} else {
					startAnalyse();
				}
			}
		});
	}
	
	private void initReportEntryFields()
	{
		lblPreview.setText("");
		lblMethodeName.setVisible(false);
		txtMethodeName.setVisible(false);

		lblReportEntry.setVisible(true);
		txtReportEntry.setVisible(true);
		
		int xPos = getRightSidePosition(txtReportEntry) + 10;
		btnAnalyseApply.setBounds(xPos, btnAnalyseApply.getLocation().y, 100, SysNatDialog.lineHeight);
		btnAnalyseApply.setText("Apply");
		
		if (txtMethodeName.getText().startsWith(STANDARD_STORE_TEST_OBJECT_METHOD)) {
			String testObjectName = txtMethodeName.getText().substring(STANDARD_STORE_TEST_OBJECT_METHOD.length());
			txtReportEntry.setText("TestObject <b>" + testObjectName + "</b> has been stored.");
			
		}
		
		txtReportEntry.requestFocus();
	}

	private void initTestMethodFields(String suggestion)
	{
		lblParameterName.setVisible(false);
		txtParameterName.setVisible(false);

		lblMethodeName.setVisible(true);
		txtMethodeName.setVisible(true);
		txtMethodeName.setText(suggestion);
		
		int xPos = getRightSidePosition(txtMethodeName) + 10;
		btnAnalyseApply.setBounds(xPos, btnAnalyseApply.getLocation().y, 100, SysNatDialog.lineHeight);
		btnAnalyseApply.setText("Apply");
		
		txtMethodeName.requestFocus();
	}

	private void initParameterNameFields(String text)
	{
		lblAnalyseResult.setVisible(false);

		lblParameterName.setText(text);
		lblParameterName.setBounds(SysNatDialog.xPosFirstColumn, btnAnalyseApply.getLocation().y, getLabelWidth(lblParameterName) + 10, SysNatDialog.lineHeight);
		lblParameterName.setVisible(true);
		
		int xPos = getRightSidePosition(lblParameterName) + 10;
		txtParameterName.setBounds(xPos, btnAnalyseApply.getLocation().y, 300, SysNatDialog.lineHeight);
		txtParameterName.setVisible(true);
		txtParameterName.setText("");
		
		xPos = getRightSidePosition(txtParameterName) + 10;
		btnAnalyseApply.setBounds(xPos, btnAnalyseApply.getLocation().y, 200, SysNatDialog.lineHeight);
		btnAnalyseApply.setText("Apply and Re-Analyse");
		
		txtParameterName.requestFocus();
	}
	
	private int getLabelWidth(JLabel label) 
	{	
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		Font font = label.getFont();
		return (int)(font.getStringBounds(label.getText(), frc).getWidth());
	}

	private void initProblemLabel(String result)
	{
		lblAnalyseResult.setText(result);
		lblAnalyseResult.setVisible(true);
	}	
}
