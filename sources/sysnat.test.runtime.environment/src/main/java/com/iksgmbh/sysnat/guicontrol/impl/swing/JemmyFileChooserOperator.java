package com.iksgmbh.sysnat.guicontrol.impl.swing;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.netbeans.jemmy.operators.JFileChooserOperator;

/**
 * This must not be a subclass of AbstractJemmyOperator!
 * 
 * @author Reik Oberrath
 */
public class JemmyFileChooserOperator
{
	private JFileChooserOperator operator;

	public JemmyFileChooserOperator(long timeout)
	{
		try {
			operator = CompletableFuture.supplyAsync(() -> new JFileChooserOperator()).get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			// ignore
		}
	}
	
	public JFileChooserOperator getOperator()
	{
		return operator;
	}

	public void cancel()
	{
		operator.cancel();
	}
	

	public void enter(String filePath)
	{
		operator.setSelectedFile(new File(filePath));
	}
}
