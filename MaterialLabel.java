package com.example.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JLabel;

public class MaterialLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1695539097407449042L;
	
	private Color color;
	private boolean isFocused = false;
	
	private int width;
	private int height;
	
	
	
	public MaterialLabel() {
		this("", null, LEADING);
		
	}
	
	public MaterialLabel(String text) {
		this(text, null, LEADING);
		
	}
	
	public MaterialLabel(Icon image) {
		this(null, image, CENTER);
		
	}

	public MaterialLabel(Icon image, int horizontalAlignment) {
		this(null, image, horizontalAlignment);
		
	}

	
	
	public MaterialLabel(String text, int horizontalAlignment) {
		this(text, null, horizontalAlignment);
		
	}

	public MaterialLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		init();
	}

	

	

	public void setBackgroundColor(Color color) {
		this.color = color;
	}
	
	public Color getBackgroundColor() {
		return color;
	}
	
	private void init() {
		color = new Color(0, 0, 0, 0);
		addMouseListener(new LabelMouseListener());
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(color);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		super.paint(g);
	}
	
	
	
	public void zoomOutText() {
		setFont(getFont().deriveFont(16.0f));
		repaint();
	}
	
	public void zoomInText() {
		setFont(getFont().deriveFont(14.0f));
	}
	
	
	private boolean isFocused() {
		return isFocused;
	}
	
	private void changeBackgroundColor() {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		
		if (isFocused()) {
			color = new Color(red, green, blue, 15);
		} else {
			color = new Color(red, green, blue, 0);
		}
		
		repaint();
	}
	
	
	class LabelMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
			//System.out.println(hasFocus());
			
			isFocused = true;
		
			changeBackgroundColor();
			//System.out.println("labelWidth " + getWidth());
			//System.out.println("containerWidth " + getParent().getWidth());
			isFocused = false;
			zoomOutText();
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
			changeBackgroundColor();
			zoomInText();
			repaint();
		}
		
	}
	
}
