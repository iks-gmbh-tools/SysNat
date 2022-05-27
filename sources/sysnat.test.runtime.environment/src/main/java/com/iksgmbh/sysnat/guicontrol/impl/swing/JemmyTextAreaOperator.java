package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;

public class JemmyTextAreaOperator extends AbstractJemmyOperator
{
	private final JTextAreaOperator operator;


	public JemmyTextAreaOperator(final ContainerOperator cont, final ComponentChooser chooser, final long timeout)
	{
		super(cont, timeout);
		operator = new JTextAreaOperator(cont, chooser);
	}

	public JemmyTextAreaOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		operator = new JTextAreaOperator(cont, name);
	}

	public JemmyTextAreaOperator(final ContainerOperator cont, final ComponentChooser chooser)
	{
		this(cont, chooser, TIMEOUT);
	}

	public JemmyTextAreaOperator(final ContainerOperator cont, final String text)
	{
		this(cont, text, TIMEOUT);
	}

	public JemmyTextAreaOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		operator = new JTextAreaOperator(cont, item);
	}

	public String getText()
	{
		return operator.getText();
	}

	public boolean isEnabled()
	{
		return operator.isEnabled();
	}

	public boolean isEditable()
	{
		return operator.isEditable() && operator.isEnabled();
	}

	public boolean isVisible()
	{
		return operator.isVisible();
	}

	public void typeText(final String text)
	{
		try
		{
			operator.typeText(text);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Text '" + text + "' eingeben", e);
		}
	}

	public void replaceAllText(final String text)
	{
		try
		{
			operator.replaceRange(text, 0, getText().length());
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("vorhandenen Text mit '" + text + "' ersetzen", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JTextAreaOperator getOperator() {
		return operator;
	}
	
}
