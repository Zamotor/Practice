package com.example.ui;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;

import com.example.widget.DisplayRect;
import com.example.widget.LineHolder;
import com.example.widget.MaterialContainer;
import com.example.widget.MaterialLabel;
import com.example.widget.Test;

public class MainActivity {

	DisplayRect rect;
	public void onCreate() {
		
		long start = System.currentTimeMillis();
		Color bgColor = new Color(1, 1, 1, 0f);
		
		JFrame frame = new JFrame("This is a test!");
		JPanel frameContainer = new JPanel();
		frameContainer.setLayout(new BorderLayout());
		
		//frameContainer.setBackground(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		//frameContainer.setLocation(0, 0);
		//frameContainer.setSize(140, 140);
		
		MaterialContainer c = new MaterialContainer(260, 260);
		
		
		Container con = new Container();
		
		frame.setLocation(700, 350);
		frame.setSize(260, 260);
		
		c.setLocation(0, 0);
		c.setSize(260, 260);
		c.setVisible(true);
		//ScrollPaneLayout scrollLayout = new ScrollPaneLayout();
		
		
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		
		//frame.setLayout(manager);
		rect = new DisplayRect(10, 10, 100, 50);
		rect.setVisible(true);
		con.add(rect);
		
		DisplayRect rect2 = new DisplayRect(0, 100, 100, 50);
		rect2.setVisible(true);
		
		JButton btn = new JButton("click");
		btn.setLocation(100, 40);
		btn.setSize(100, 50);
		
		JButton btn2 = new JButton("click");
		//btn2.setLocation(50, 180);
		btn2.setSize(100, 50);
		
		
		c.setSize(260, 260);
		//c.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		btn.setBackground(bgColor);
		//frame.setBackground(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		
		//for (int i = 0; i < 5; i++) {
		MaterialLabel label = new MaterialLabel("test1");
		Font font = new Font("t", Font.PLAIN, 25);
		label.setFont(font);
		label.setSize(100, 20);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		//label.setBackground(Color.BLACK);
		
		MaterialLabel label2 = new MaterialLabel("test2");
		label2.setFont(font);
		label2.setSize(100, 20);
		label2.setLocation(0, 0);
	
		Point s = new Point(50, 60);
		Point e = new Point(50, 100);
		LineHolder line1 = new LineHolder(rect, rect2);
		line1.setHeightRatio(0.5f);
		line1.setSize(100, 100);
		line1.setVisible(true);
		rect.addLabel(label);
		autoChangeBackground(label);
		//rect.addLabel(label2);
		//c.add(label);
		
		
		//c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		//c.addComponent(rect);
		//c.addComponent(btn);
		//c.addComponent(btn2);
		//}
		//frameContainer.add(btn);
		c.addComponent(line1);
		c.addComponent(rect);
		c.addComponent(rect2);
		
		//c.add(frameContainer);
		
		//frameContainer.add(label);
		
		//frameContainer.add(label2);
		Test t = new Test();
		t.setSize(260, 180);
		t.drawRect(0, 0, 80, 80);
		
		frameContainer.add(btn2, BorderLayout.SOUTH);
		frameContainer.add(t);
		Graphics g = frameContainer.getGraphics();
		//label.setHorizontalTextPosition(SwingConstants.RIGHT);
		//c.addComponent(frameContainer);
		BorderLayout borderLayout = new BorderLayout();
		//borderLayout.layoutContainer(frameContainer);
		
		//BoxLayout layout = new BoxLayout(frameContainer, BoxLayout.Y_AXIS);
		
		//frameContainer.setLayout(layout);
		
		//c.addComponent(label);
		
		//c.addComponent(frameContainer);
		
		System.out.println(c.getComponent(1).getWidth());
		//c.addComponent(label2);
		System.out.println(c.getComponentCount());
		
		
		btn.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				Dimension dimen = frameContainer.getPreferredSize();
				System.out.println(dimen.height + " " + dimen.width);
				System.out.println(frameContainer.getWidth());
				label.setFont(new Font("f", Font.PLAIN, 27));
				//label.setText("fdlkfjds;apofjspfjsfojeoej");
				
				dimen = frameContainer.getPreferredSize();
				System.out.println(dimen.height + " " + dimen.width);
				System.out.println(frameContainer.getWidth());
				
				//dimen = frame.getPreferredSize();
				System.out.println("frame " + dimen.height + " " + dimen.width);
				//frame.setSize(dimen);
				
				System.out.println(btn.isSelected());
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
				System.out.println(btn.isFocusPainted());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				//System.out.println(btn.isFocusPainted());
			}
			
		});
		
		btn2.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				t.drawRect();
				System.out.println("draw");
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
				System.out.println(btn.isFocusPainted());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				//System.out.println(btn.isFocusPainted());
			}
			
		});
		
		
		//frameContainer.setLayout(new BoxLayout(frameContainer, BoxLayout.Y_AXIS));
		//frame.add(frameContainer);
		//JList list = new JList();
		frame.add(c);
		//frame.add(c.get());
		//frame.add(c);
		
		frame.setVisible(true);
		label.setSize(140, 30);
		//System.out.println("isFocusable " + rect.isFocusable());
		
		long end = System.currentTimeMillis();
		
		System.out.println(end - start);
	}
	
	public void autoChangeBackground(MaterialLabel label) {
		Random ran = new Random();

		
		Thread changeThread = new Thread() {
			@Override
			public void run() {
				int counter = 1;
				while (true) {
					int red = ran.nextInt(255);
					int green = ran.nextInt(255);
					int blue = ran.nextInt(255);
					int alpha = ran.nextInt(255);

					label.setText(String.valueOf(counter) + "!");
					counter++;
					if (counter == 6) {
						counter = 1;
					}
					
					//setBackground(new Color(red, green, blue, alpha));
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		changeThread.start();

	}

}
