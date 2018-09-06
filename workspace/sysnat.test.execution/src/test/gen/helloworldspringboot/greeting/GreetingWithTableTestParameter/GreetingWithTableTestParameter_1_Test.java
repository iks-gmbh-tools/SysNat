package helloworldspringboot.greeting.GreetingWithTableTestParameter;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import com.iksgmbh.sysnat.testcasejavatemplate.TestCaseTemplateParent;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.LanguageTemplatesHelloWorldSpringBootBasics;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesPrint;

/**
 * Executable Example for TestApplication 'HelloWorldSpringBoot'.
 * Autogenerated by SysNatTesting.
 */
public class GreetingWithTableTestParameter_1_Test extends TestCaseTemplateParent
{
	protected LanguageTemplatesCommon languageTemplatesCommon;
	protected LanguageTemplatesHelloWorldSpringBootBasics languageTemplatesHelloWorldSpringBootBasics;
	protected LanguageTemplatesPrint languageTemplatesPrint;
	
	@Before
	public void setUp() 
	{
		super.setUp();
		languageTemplatesCommon = new LanguageTemplatesCommon(this);
		languageTemplatesHelloWorldSpringBootBasics = new LanguageTemplatesHelloWorldSpringBootBasics(this);
		languageTemplatesPrint = new LanguageTemplatesPrint(this);
	}

	@After
	public void shutdown() 
	{
		if ( ! isSkipped() && executionInfo.isApplicationStarted()) {
			if (languageTemplatesHelloWorldSpringBootBasics != null) languageTemplatesHelloWorldSpringBootBasics.gotoStartPage();
		}
		languageTemplatesCommon = null;
		languageTemplatesHelloWorldSpringBootBasics = null;
		languageTemplatesPrint = null;
		super.shutdown();
	}

	@Test
	@Override
	public void executeTestCase() 
	{
		try {
			languageTemplatesCommon.startNewXX("GreetingWithTableTestParameter_1");
			languageTemplatesCommon.checkFilterCategory("<Pfad>");
			languageTemplatesCommon.setDatasetObject("Datasets in columns<Line Separator>|Name|Susi|<Line Separator>|GreetingResult|Hi Susi!|<Line Separator>|Greeting|Hi|");

			// arrange block
			languageTemplatesCommon.startNewTestPhase("Arrange");
			languageTemplatesCommon.executeScript("EnterGreetingData");

			// act block
			languageTemplatesCommon.startNewTestPhase("Act");
			languageTemplatesHelloWorldSpringBootBasics.clickButton("Greet");

			// assert block
			languageTemplatesCommon.startNewTestPhase("Assert");
			languageTemplatesHelloWorldSpringBootBasics.isDislayedTextCorrect("GreetingResult", ":GreetingResult");
			
			closeCurrentTestCaseWithSuccess();
		} catch (Throwable e) {
			super.handleThrowable(e);
		}
	}
}
