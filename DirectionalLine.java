package com.example.widget;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class DirectionalLine {

	private Point start;
	private Point end;
	private float ratio;
	private int firstSegmentHeight = (int) ((end.y - start.y) * ratio);
	
	private List<String> path = new ArrayList<>();
	
	
	
	public Point getTargetPosition(JComponent comp) {
		return comp.getLocation();
	} 
	
	public float getRatio() {
		return ratio;
	}
	
	public void setHeightRatio(float ratio) {
		this.ratio = ratio;
	}
	
	public void drawLine(Graphics g) {
		if (start.x != end.x) {
			g.drawLine(start.x, start.y, start.x, firstSegmentHeight);
			g.drawLine(start.x, firstSegmentHeight, end.x, firstSegmentHeight);
			g.drawLine(end.x, firstSegmentHeight, end.x, end.y);
		} else {
			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}
	
}
