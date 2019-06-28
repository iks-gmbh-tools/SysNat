/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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