package com.example.widget;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class LineHolder extends JComponent {
	
	private Point start;
	private Point end;
	private float ratio;
	private int firstSegmentHeight;
	
	private List<String> path = new ArrayList<>();
	
	public LineHolder(JComponent comp1, JComponent comp2) {
		
		
		//setBackground(new Color(15, 89, 0, 0));
		init();
		getTargetPosition(comp1, comp2);
	}
	
	public void init() {
		start = new Point();
		end = new Point();
		
		
	}
	
	public void getTargetPosition(JComponent comp1, JComponent comp2) {
		start.x = comp1.getLocation().x + comp1.getWidth() / 2 + 12;
		
		start.y = comp1.getLocation().y + comp1.getHeight() + 12;
		System.out.print("rect " + "location: " + comp1.getLocation().x + " " + comp1.getLocation().y);
		System.out.println("size: " + comp1.getWidth() + " " + comp1.getHeight());
		System.out.println("start " + start.x + " " + start.y);
		
		end.x = comp2.getLocation().x + comp2.getWidth() / 2;
		end.y = comp2.getLocation().y;
		
		System.out.print("rect2 " + "location: " + comp2.getLocation().x + " " + comp2.getLocation().y);
		System.out.println("size: " + comp2.getWidth() + " " + comp2.getHeight());
		System.out.println("end " + end.x + " " + end.y);
		
	}
	
	public float getRatio() {
		return ratio;
	}
	
	public void setHeightRatio(float ratio) {
		this.ratio = ratio;
		firstSegmentHeight = (int) ((end.y - start.y) * ratio);
		
		//this.
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(new Color(15, 89, 0, 255));
		drawLine(g2d);
	}
	
	@Override
	public void update(Graphics g) {
		super.update(g);
	}
	
	
	public void drawLine(Graphics g) {
		if (start.x != end.x) {
			g.drawLine(start.x, start.y, start.x, start.y + firstSegmentHeight);
			//System.out.println("height: " + firstSegmentHeight);
			g.drawLine(start.x, start.y + firstSegmentHeight, end.x, start.y + firstSegmentHeight);
			g.drawLine(end.x, start.y + firstSegmentHeight, end.x, end.y);
		} else {
			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}
	
	
}
