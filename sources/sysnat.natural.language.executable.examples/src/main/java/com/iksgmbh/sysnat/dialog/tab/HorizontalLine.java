package com.iksgmbh.sysnat.dialog.tab;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class HorizontalLine extends JPanel
{
	private static final long serialVersionUID = 1L;

	public HorizontalLine(JComponent parent, Color aColor, 
			              int xpos, int ypos, int length, int thickness)
	{
		this.setLayout(null);
		this.setBackground(aColor);
		this.setBounds(xpos, ypos, length, thickness);
		parent.add(this);
	}
}
