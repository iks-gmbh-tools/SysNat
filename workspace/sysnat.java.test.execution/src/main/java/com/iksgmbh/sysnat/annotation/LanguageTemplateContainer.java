package com.iksgmbh.sysnat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark (the type of) a field within a TestCaseTemplate 
 * to contain methods with LanguageTemplate annotations.
 * These are used to map natural language instructions in the nltc files on java commands.
 * The java code generated that way is injected into the TestCaseTemplate of the 
 * application under test. The result is an executable junit test case.
 * 
 * @author Reik Oberrath
 */
@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface LanguageTemplateContainer {}
