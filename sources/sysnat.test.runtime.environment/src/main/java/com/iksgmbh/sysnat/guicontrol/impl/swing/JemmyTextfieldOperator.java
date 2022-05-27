package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class JemmyTextfieldOperator extends AbstractJemmyOperator
{
	private final JTextFieldOperator textFieldOperator;

	public JemmyTextfieldOperator(final ContainerOperator cont, final String text)
	{
		this(cont, text, TIMEOUT);
	}

	public JemmyTextfieldOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyTextfieldOperator(final ContainerOperator cont, final String text, final long timeout)
	{
		super(cont, timeout);
		textFieldOperator = new JTextFieldOperator(cont, text);
	}

	public JemmyTextfieldOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		textFieldOperator = new JTextFieldOperator(cont, chos);
	}

	public JemmyTextfieldOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		textFieldOperator = new JTextFieldOperator(cont, item);
	}

	public void typeText(final String text)
	{
		String message = "Text '" + text + "' eingeben";
		type(text, message);
	}

	public void typePassword(final String text)
	{
		String message = "Text '******' eingeben";
		type(text, message);
	}

	public void setText(final String text)
	{
		textFieldOperator.requestFocus();
		textFieldOperator.setText(text);
		textFieldOperator.transferFocus();
	}
	
	private void type(final String text, String message) 
	{
		try
		{
			if (text != null)
			{
				textFieldOperator.requestFocus();
				textFieldOperator.setVerification(false);

				textFieldOperator.typeText(text);
				textFieldOperator.transferFocus();

			}
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error inserting text");
		}
	}

	public String getText()
	{
		return textFieldOperator.getText();
	}

	public boolean isEnabled()
	{
		return textFieldOperator.isEnabled();
	}

	public boolean isEditable()
	{
		return textFieldOperator.isEditable() && textFieldOperator.isEnabled();
	}

	public boolean isVisible()
	{
		return textFieldOperator.isVisible();
	}

	public void transferFocus()
	{
		textFieldOperator.transferFocus();
	}

	/**
	 * Removes all text from textfield
	 * @return this (for fluent API use)
	 */
	public JemmyTextfieldOperator clear() {
		textFieldOperator.clearText();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JTextFieldOperator getOperator() {
		return textFieldOperator;
	}
}
