package com.iksgmbh.sysnat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to map a natural language pattern on 
 * a java code (the annotated method). That way, the nlxx-file content
 * is translated into a series of java commands.
 * The java code generated that way is injected into the TestCaseTemplate of the 
 * application under test. The result is an executable junit test case.
 * 
 * @author Reik Oberrath
 */
@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface LanguageTemplates {
	LanguageTemplate[] value();
}
