package com.iksgmbh.sysnat.guicontrol.impl.swing;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * This must not be a subclass of AbstractJemmyOperator!
 * 
 * @author Reik Oberrath
 */
public class JemmyDialogOperator
{
	private JDialogOperator dialogOperator;

	public JemmyDialogOperator(long timeout)
	{
		try {
			dialogOperator = CompletableFuture.supplyAsync(() -> new JDialogOperator()).get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			// ignore
		}
	}

	public JemmyDialogOperator(String title, long timeout)
	{
		try {
			dialogOperator = CompletableFuture.supplyAsync(() -> new JDialogOperator(title)).get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			// ignore
		}
	}
	
	public JDialogOperator getOperator()
	{
		return dialogOperator;
	}

	public void close()
	{
		dialogOperator.close();
	}
}
