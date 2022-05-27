package com.iksgmbh.sysnat.dialog.tab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.dialog.helper.BasicTabPanel;
import com.iksgmbh.sysnat.dialog.helper.HorizontalLine;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen.DataType;

public class GenerateTestAppPanel extends BasicTabPanel
{
	private static final long serialVersionUID = 1L;
	
	private enum LoginMode { With_Login, Without_Login};
	
	private LoginModeListener loginModeListener = new LoginModeListener();
	private ApplicationTypeListener applicationTypeListener = new ApplicationTypeListener();
	private int yPosSub = 0;
	private HorizontalLine secondSeparationLine;
	private JLabel secondSectionLabel;

	private JPanel loginDataPanel, swingDataPanel, webDataPanel, compositeDataPanel;
	private JComboBox<String> cbxTestApplicationType, cbxInitialEnvironment, cbxLoginMode;
	private JComboBox<String> cbxCompositeApplication1, cbxCompositeApplication2;
	private JTextField txtTestApplicationName, txtInitialEnvironmentalDisplayName, txtStartURL;
	private JTextField txtInstallDir, txtJavaStartClass, txtMainFrameTitle, txtConfigFiles, txtLibDirs;
	private JTextField txtUserFieldDisplayName, txtUserFieldTechId, txtUserLoginId;
	private JTextField txtPasswordFieldDisplayName, txtPasswordFieldTechId, txtPasswordOfLoginUser;
	private JTextField txtLoginButtonDisplayName, txtLoginButtonTechId;
	
	public GenerateTestAppPanel(SysNatDialog aDialog)
	{
		super(aDialog, SysNatFileUtil.findAbsoluteFilePath(ExecutionRuntimeInfo.TESTING_CONFIG_FILE_NAME));
		setLayout(null);
		initComponents();
		checkApplicationType();
		checkLoginMode();
	}
	
	private void initComponents()
	{
		// first Component Block
		yPos = 20;
		createSectionLabel(this, yPos, "Basic settings");
		initTestApplicationFields();
		initInitialEnvironmenFields();
		initComboLoginMode();
		
		yPos += deltaY;
		createSeparationLine(this, yPos);
		yPos += deltaY/2;		
		createSectionLabel(this, yPos, "Application Type specific settings");
		yPos += deltaY;		
		
		// second Component Block
		
		swingDataPanel = createSubPanel(3, yPos, this);
		initSwingStartParamter1();
		initSwingStartParamter2();
		initSwingStartParamter3();
				
		yPosSub = 0;
		webDataPanel = createSubPanel(3, yPos, this);
		initTextFieldStartURL();
		
		yPosSub = 0;
		compositeDataPanel = createSubPanel(3, yPos, this);
		initComboCompositeTestApp1();
		initComboCompositeTestApp2();

		yPos += swingDataPanel.getSize().height;  
		yPos += deltaY/2;
		secondSeparationLine = createSeparationLine(this, yPos);
		yPos += deltaY/2;				
		secondSectionLabel = createSectionLabel(this, yPos, "Login configuration");
		yPos += deltaY;		
		
		// third Component Block
		yPosSub = 0;
		loginDataPanel = createSubPanel(3, yPos, this);
		initLoginUserFields();
		initLoginPasswordFields();
		initLoginButtonFields();
		
		// button line
		initCancelButton(this);
		initResetButton(this);
		initStartButton(this);
	}

	private void initComboCompositeTestApp1()
	{
		initLabel(compositeDataPanel, yPosSub, "First composite application", NO_TOOLTIP);
		List<String> knownTestApps = executionInfo.getKnownTestApplications(ApplicationType.Composite);
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		String defaultValue = "";
		if (options.length > 0) defaultValue = knownTestApps.get(0);
		cbxCompositeApplication1 = initCombo(compositeDataPanel, options, yPosSub, defaultValue, NO_TOOLTIP, true, LinePosition.SINGLE, "cbxCompositeApplication1");
		yPosSub += deltaY;
	}
	
	private void initComboCompositeTestApp2()
	{
		initLabel(compositeDataPanel, yPosSub, "Second composite application", NO_TOOLTIP);
		List<String> knownTestApps = executionInfo.getKnownTestApplications(ApplicationType.Composite);
		String[] options = knownTestApps.toArray(new String[knownTestApps.size()]);
		String defaultValue = "";
		if (options.length > 1) defaultValue = knownTestApps.get(1);
		cbxCompositeApplication2 = initCombo(compositeDataPanel, options, yPosSub, defaultValue, NO_TOOLTIP, true, LinePosition.SINGLE, "cbxCompositeApplication2");
		yPosSub += deltaY;
	}

	protected void initStartButton(JPanel parent) 
	{
		initStartButton(parent, "Start generation...", null, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				doCreate();
			}
		});
	}

	protected void doCreate()
	{
		String result = TestApplicationDevGen.doYourJob(collectGenerationData());
		if (result.startsWith("Error:")) {
			System.err.println(result);
			JOptionPane.showMessageDialog(frameDialog, result, "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			String testAppName = txtTestApplicationName.getText().trim();
			String message = "<html>Close dialog and finish generation?<br><br>Generation contained" 
			        + "<br>- Java code in sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/" + testAppName
			        + "<br>- Property file 'sysnat.test.runtime.environment/src/main/resources/execution_properties/" + testAppName + ".properties'" 
			        + "<br>- NL instructions in sysnat.natural.language.executable.examples/ExecutableExamples/" + testAppName 
			        + "<br>- Test data in sysnat.natural.language.executable.examples/testdata/" + testAppName
			        + "<br>- Help documentation file 'sysnat.natural.language.executable.examples/help/ExistingNLInstructions_" + testAppName + ".html'"
			        + "<br><br><html>";
			
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
	
	@Override
	protected void reset() 
	{
		txtTestApplicationName.setText("");
		cbxTestApplicationType.setSelectedItem(ApplicationType.Web.name());
		cbxInitialEnvironment.setSelectedItem(TargetEnvironment.LOCAL.name());
        txtInitialEnvironmentalDisplayName.setText("");
        cbxLoginMode.setSelectedItem("With Login");
		
		txtStartURL.setText("");  
		txtJavaStartClass.setText("");  
		txtLibDirs.setText("");  
		txtInstallDir.setText(""); 
		txtMainFrameTitle.setText(""); 
		txtConfigFiles.setText(""); 
		
		// Login settings (needed only for withLogin = true) 
		txtUserFieldDisplayName.setText("");  
		txtUserFieldTechId.setText("");
		txtUserLoginId.setText("");
		txtPasswordFieldDisplayName.setText("");
		txtPasswordFieldTechId.setText("");
		txtPasswordOfLoginUser.setText("");
		txtLoginButtonDisplayName.setText("");
		txtLoginButtonTechId.setText("");
	}

	private HashMap<DataType, String> collectGenerationData()
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		// Common Swing properties (always needed)		
		generationData.put(DataType.TestApplicationName, txtTestApplicationName.getText().trim());
		generationData.put(DataType.APP_TYPE, cbxTestApplicationType.getSelectedItem().toString());
		generationData.put(DataType.initialEnvironmentType, cbxInitialEnvironment.getSelectedItem().toString());
		generationData.put(DataType.initialEnvironmentName, txtInitialEnvironmentalDisplayName.getText().trim());
		generationData.put(DataType.withLogin, "" + cbxLoginMode.getSelectedItem().toString().equals(LoginMode.With_Login.name()));
		
		// Web properties (needed only for APP_TYPE = Web)
		generationData.put(DataType.StartURL, txtStartURL.getText().trim());  
		// Swing properties (needed only for APP_TYPE = Swing)
		generationData.put(DataType.JavaStartClass, txtJavaStartClass.getText().trim());  
		generationData.put(DataType.LibDirs, txtLibDirs.getText().trim());  
		generationData.put(DataType.InstallDir, txtInstallDir.getText().trim()); 
		generationData.put(DataType.MainFrameTitle, txtMainFrameTitle.getText().trim()); 
		generationData.put(DataType.ConfigFiles, txtConfigFiles.getText().trim()); 
		// Composite properties (needed only for APP_TYPE = Composite)
		generationData.put(DataType.Composites, cbxCompositeApplication1.getSelectedItem().toString() + ", " + cbxCompositeApplication2.getSelectedItem().toString());          
		
		// Login settings (needed only for withLogin = true) 
		generationData.put(DataType.GuiNameOfLoginUserField, txtUserFieldDisplayName.getText().trim());  
		generationData.put(DataType.TechIdOfLoginUserField, txtUserFieldTechId.getText().trim());
		generationData.put(DataType.LoginUserId, txtUserLoginId.getText().trim());
		generationData.put(DataType.GuiNameOfLoginPasswordField, txtPasswordFieldDisplayName.getText().trim());
		generationData.put(DataType.TechIdOfLoginPasswordField, txtPasswordFieldTechId.getText().trim());
		generationData.put(DataType.LoginPassword, txtPasswordOfLoginUser.getText().trim());
		generationData.put(DataType.TextOfLoginButton, txtLoginButtonDisplayName.getText().trim());
		generationData.put(DataType.TechIdOfLoginButton, txtLoginButtonTechId.getText().trim());
		
		return generationData;
	}

	private void initTestApplicationFields()
	{
		yPos += deltaY;
		initLabel(this, yPos, "New Test Application:", NO_TOOLTIP, LinePosition.FirstInTripple);
		
		initLabel(this, yPos, "Name", NO_TOOLTIP, LinePosition.SecondInTripple);
		txtTestApplicationName = initTextField(this, yPos, "", NO_TOOLTIP, LinePosition.SecondInTripple, "txtTestApplicationName");
		
		initLabel(this, yPos, "Type", NO_TOOLTIP, LinePosition.ThirdInTripple);
		List<String> availableTestAppTypes = Stream.of(ApplicationType.values()).map(e -> e.name()).collect(Collectors.toList());
		String[] options = availableTestAppTypes.toArray(new String[availableTestAppTypes.size()]);
		cbxTestApplicationType = initCombo(this, options, yPos, ApplicationType.Web.name(), NO_TOOLTIP, true, LinePosition.ThirdInTripple, "cbxTestApplicationType");
		cbxTestApplicationType.addItemListener(applicationTypeListener);
	}
	
	private void initInitialEnvironmenFields()
	{
		yPos += deltaY;
		initLabel(this, yPos, "Initial Environment:", NO_TOOLTIP, LinePosition.FirstInTripple);
		
		String tooltip = "Additional environments must be added later in the test application properties file.";
		initLabel(this, yPos, "Level", tooltip, LinePosition.SecondInTripple);
		List<String> availableTestAppTypes = Stream.of(TargetEnvironment.values()).map(e -> e.name()).collect(Collectors.toList());
		String[] options = availableTestAppTypes.toArray(new String[availableTestAppTypes.size()]);
		cbxInitialEnvironment = initCombo(this, options, yPos, TargetEnvironment.LOCAL.name(), tooltip, true, LinePosition.SecondInTripple, "cbxInitialEnvironment");
		
		tooltip = "Name of the environment used in your context";
		initLabel(this, yPos, "Name", tooltip, LinePosition.ThirdInTripple);
		txtInitialEnvironmentalDisplayName = initTextField(this, yPos, "", tooltip, LinePosition.ThirdInTripple, "txtInitialEnvironmentalDisplayName");
	}	
	
	private void initComboLoginMode()
	{
		yPos += deltaY;
		String tooltip = "Needs your test application a login?";
		initLabel(this, yPos, "Login Mode", tooltip);
		List<String> availableTestAppTypes = Stream.of(LoginMode.values()).map(e -> e.name().replace("_", " ")).collect(Collectors.toList());
		String[] options = availableTestAppTypes.toArray(new String[availableTestAppTypes.size()]);
		cbxLoginMode = initCombo(this, options, yPos, "With Login", tooltip, true, LinePosition.SINGLE, "cbxLoginMode");
		cbxLoginMode.addItemListener(loginModeListener);
	}	
	
	private void initTextFieldStartURL()
	{
		String tooltip = "Inital web address called in the web browser";
		initLabel(webDataPanel, yPosSub, "Start URL", tooltip);
		txtStartURL = initTextField(webDataPanel, yPosSub, "", tooltip, "txtStartURL");
		yPosSub += deltaY;
	}

	private void initSwingStartParamter1()
	{
		initLabel(swingDataPanel, yPosSub, "Install Directory", NO_TOOLTIP);
		txtInstallDir = initTextField(swingDataPanel, yPosSub, "", NO_TOOLTIP, "txtInstallDir");
		yPosSub += deltaY;
	}

	private void initSwingStartParamter2()
	{
		initLabel(swingDataPanel, yPosSub, "Mandatory Start Parameter:", NO_TOOLTIP, LinePosition.FirstInTripple);
		
		initLabel(swingDataPanel, yPosSub, "Java Start Class", NO_TOOLTIP, LinePosition.SecondInTripple);
		txtJavaStartClass = initTextField(swingDataPanel, yPosSub, "", NO_TOOLTIP, LinePosition.SecondInTripple, "txtJavaStartClass");
		
		initLabel(swingDataPanel, yPosSub, "Main Frame Title", NO_TOOLTIP, LinePosition.ThirdInTripple);
		txtMainFrameTitle = initTextField(swingDataPanel, yPosSub, "", NO_TOOLTIP, LinePosition.ThirdInTripple, "txtMainFrameTitle");
		
		yPosSub += deltaY;
	}

	private void initSwingStartParamter3()
	{
		initLabel(swingDataPanel, yPosSub, "Optional Start Parameter:", NO_TOOLTIP, LinePosition.FirstInTripple);
		
		initLabel(swingDataPanel, yPosSub, "Properties files", NO_TOOLTIP, LinePosition.SecondInTripple);
		txtConfigFiles = initTextField(swingDataPanel, yPosSub, "", NO_TOOLTIP, LinePosition.SecondInTripple, "txtConfigFiles");
		
		initLabel(swingDataPanel, yPosSub, "Library Directories", NO_TOOLTIP, LinePosition.ThirdInTripple);
		txtLibDirs = initTextField(swingDataPanel, yPosSub, "", NO_TOOLTIP, LinePosition.ThirdInTripple, "txtLibDirs");
		
		yPosSub += deltaY;
	}
	
	private void initLoginUserFields()
	{
		String tooltip = "Name of field 'User' in Login-Dialog";
		initLabel(loginDataPanel, yPosSub, "User Field Name", tooltip, LinePosition.FirstInTripple);
		txtUserFieldDisplayName = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.FirstInTripple, "txtUserFieldDisplayName");
		
		tooltip = "Technical identifier of field 'User' in Login-Dialog";
		initLabel(loginDataPanel, yPosSub, "Technical Id", tooltip, LinePosition.SecondInTripple);
		txtUserFieldTechId = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.SecondInTripple, "txtUserFieldTechId");
		
		tooltip = "Value for field 'User' in Login-Dialog (i.e. your Test-User)";
		initLabel(loginDataPanel, yPosSub, "Login User Name", tooltip, LinePosition.ThirdInTripple);
		txtUserLoginId = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.ThirdInTripple, "txtUserLoginId");
		
		yPosSub += deltaY;
	}

	private void initLoginPasswordFields()
	{
		String tooltip = "Name of field 'Password' in Login-Dialog";
		initLabel(loginDataPanel, yPosSub, "Passwort Field Name", tooltip, LinePosition.FirstInTripple);
		txtPasswordFieldDisplayName = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.FirstInTripple, "txtPasswordFieldDisplayName");
		
		tooltip = "Technical identifier of field 'Password' in Login-Dialog";
		initLabel(loginDataPanel, yPosSub, "Technical Id", tooltip, LinePosition.SecondInTripple);
		txtPasswordFieldTechId = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.SecondInTripple, "txtPasswordFieldTechId");
		
		tooltip = "Value for field 'Password' in Login-Dialog (i.e. password of your Test-User)";
		initLabel(loginDataPanel, yPosSub, "Login Passwort", tooltip, LinePosition.ThirdInTripple);
		txtPasswordOfLoginUser = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.ThirdInTripple, "txtPasswordOfLoginUser");
		
		yPosSub += deltaY;
	}
	
	private void initLoginButtonFields()
	{
		String tooltip = "Name of button that starts the login";
		initLabel(loginDataPanel, yPosSub, "Login Button Name", tooltip, LinePosition.FirstInTripple);
		txtLoginButtonDisplayName = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.FirstInTripple, "txtLoginButtonDisplayName");
		
		tooltip = "Technical identifier of the login button in the Login-Dialog";
		initLabel(loginDataPanel, yPosSub, "Technical Id", tooltip, LinePosition.SecondInTripple);
		txtLoginButtonTechId = initTextField(loginDataPanel, yPosSub, "", tooltip, LinePosition.SecondInTripple, "txtLoginButtonTechId");
		
		yPosSub += deltaY;
	}


	private void checkApplicationType()
	{
		boolean isWebApplication = cbxTestApplicationType.getSelectedItem().toString().equals(ApplicationType.Web.name());
		webDataPanel.setVisible(isWebApplication);
		swingDataPanel.setVisible(! isWebApplication);
		compositeDataPanel.setVisible(! isWebApplication);
		if (isWebApplication) return;
		
		boolean isSwingApplication = cbxTestApplicationType.getSelectedItem().toString().equals(ApplicationType.Swing.name());
		webDataPanel.setVisible(! isSwingApplication);
		swingDataPanel.setVisible(isSwingApplication);
		compositeDataPanel.setVisible(! isSwingApplication);
		if (isSwingApplication) return;
		
		webDataPanel.setVisible(false);
		swingDataPanel.setVisible(false);
		compositeDataPanel.setVisible(true);
	}

	private void checkLoginMode()
	{
		boolean withLogin = cbxLoginMode.getSelectedItem().toString().equals(LoginMode.With_Login.name().replace("_", " "));
		secondSeparationLine.setVisible(withLogin);
		secondSectionLabel.setVisible(withLogin);
		loginDataPanel.setVisible(withLogin);
	}
	
	protected int getFirstColumnLength() {
		return 350;
	}
	
	protected int getSecondColumnLength() {
		return 350;
	}
	
	protected int getThirdColumnLength() {
		return 350;
	}
	
	// ###########################################################################
	//                         I n n e r    C l a s s e s  
	// ###########################################################################
	
	class LoginModeListener implements ItemListener
	{
		@Override public void itemStateChanged(ItemEvent e) {
			checkLoginMode();
		}

	}
	
	class ApplicationTypeListener implements ItemListener
	{
		@Override public void itemStateChanged(ItemEvent e) { 
			checkApplicationType(); 
		}
	}
}
