package com.iksgmbh.sysnat.test;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

// Executes Class Level Tests of all modules

@RunWith(ClasspathSuite.class)
@ClassnameFilters({".*ModuleLevelTest"})
public class ModuleLevelTestExecutor {
}
