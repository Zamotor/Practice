package com.example.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class MaterialContainer extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 499422248588168418L;
	
	private JPanel background;
	private Container container;
	private int width;
	private int height;
	
	
	public MaterialContainer(int width, int height) {
		
		//setSize(width, height);
		this.width = width;
		this.height = height;
		
		init();
		
	}
	
	private void init() {
		
		container = new Container();
		background = new JPanel();
		background.setSize(width, height);
		container.setSize(width, height);
	
		background.setBackground(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		
		add(background);
	}
	
	public JPanel get() {
		return background;
	}
	
	public void addComponent(Component comp) {
		//this.
		
		remove(getComponentCount() - 1);
		comp.setSize(comp.getPreferredSize());
		add(comp);
		add(background);
		
	}

}
 