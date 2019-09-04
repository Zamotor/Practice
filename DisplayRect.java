package com.example.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class DisplayRect extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3128964249270237940L;
	
	private DisplayRectViewport viewport;
	private DisplayRectBackground background;
	private int x = 0;
	private int y = 0;
	private int width;
	private int height;
	private Graphics painter;
	private MaterialLabel headLabel;
	private Font labelFont;
	private int sepLine = 1;
	private DisplayRect displayRect = this;
	private int borderThickness = 2;
	private int edgeBreadth = 1;
	private Color edgeColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	private Color backgroundColor = new Color(0.3f, 0.8f, 1.0f, 1.0f);
	private Color currentBorderColor = new Color(1.0f, 0.0f, 0.0f, 0.3f);
	private LineBorder border = new LineBorder(currentBorderColor, borderThickness);

	public DisplayRect(int width, int height) {
		this(0, 0, width, height);
	}

	public DisplayRect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		setLocation(x, y);
		setSize(width, height);
		setFocusable(true);

		addMouseListener(new MyListener());
		//addLabel();
		
		viewport = new DisplayRectViewport(borderThickness, borderThickness, width, height);
		background = new DisplayRectBackground(borderThickness, borderThickness, width, height);
		// setLayout(new GroupLayout());
		// this.
		
		add(viewport);
		add(background);
		
		autoChangeBackground();
		
	}
	

	/*@Override
	public void paint(Graphics g) {
		super.paint(g);

		// g = (Graphics2D) g;
		/*painter = g;

		// g.
		g.setColor(edgeColor);
		g.drawRect(borderThickness, borderThickness, width - 1, height - 1);
		g.setColor(backgroundColor);
		g.fillRect(borderThickness + edgeBreadth, borderThickness + edgeBreadth, (width - edgeBreadth),
				(height - edgeBreadth));
		// g.drawString("Hello World!", 26, 26);

		// drawBorder();
		// validate();

	}*/


	public void setEdgeBreadth(int breadth) {
		edgeBreadth = breadth;
	}
	

	public void drawBorder() {

		int thickness = border.getThickness();

		int newWidth = width + 2 * thickness;
		int newHeight = height + 2 * thickness;

		setSize(newWidth, newHeight);
		border.paintBorder(this, painter, 0, 0, newWidth, newHeight);

		currentBorderColor = border.getLineColor();
		Color c = new Color(1.0f, 0.0f, 0.0f, 0.3f);
		border = new LineBorder(c, border.getThickness());

		setBorder(border);
	}

	public void setBorderColor(Color c) {
		border = new LineBorder(c, border.getThickness());
		setBorder(border);
	}

	class MyListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			Color c = new Color(1.0f, 0.5f, 0.0f, 0.3f);
			setBackground(c);

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
			drawBorder();
			
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			Point mousePosition = getMousePosition();

			if (mousePosition != null) {
				if (!contains(mousePosition)) {
					
				}
			} else {
				hideBorder();
			}

		}

	}

	public void addLabel(MaterialLabel label) {
		viewport.addContentLabel(label);
	}

	public void hideBorder() {
		Color c = new Color(0.0f, 0.0f, 0.0f, 0f);
		setBorderColor(c);
	}

	public void setBorderThickness(int thickness) {
		borderThickness = thickness;
	}

	public void margin() {

	}

	public void marginLeft(int px) {
		x = px + borderThickness;
		repaint();
	}

	public void marginTop(int px) {
		y = px + borderThickness;
		repaint();
	}

	public void marginRight(int px) {
		x = this.getParent().getWidth() - (width + px + 2 * borderThickness);
		repaint();
	}

	public void marginBottom(int px) {
		y = this.getParent().getHeight() - (height + px + 2 * borderThickness);
		repaint();
	}

	@Override 
	public void setBackground(Color c) {
		backgroundColor = c;
	}

	public void addLabel() {
		headLabel = new MaterialLabel();
		headLabel.setLocation(0, 0);
		headLabel.setSize(width, height);
		headLabel.setText("test");
		headLabel.setVisible(true);

		// setLayout(new OverlayLayout(this));
		// add(headLabel, 0);
	}
	
	public void changeSize(int width, int height) {
		displayRect.setSize(width, height);
		this.width = width;
		this.height = height;
		
		background.notifyRepaint();
	}
	
	public boolean isDataChanged() {
		return true;
	}

	public MaterialLabel getText() {
		return headLabel;
	}

	public void setText(String text) {

	}

	public void setTextStyle(int style) {

	}

	public void setTextSize(int sp) {

	}
	
	public void autoChangeBackground() {
		Random ran = new Random();

		Thread changeThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					int red = ran.nextInt(255);
					int green = ran.nextInt(255);
					int blue = ran.nextInt(255);
					int alpha = ran.nextInt(255);

					backgroundColor = new Color(red, green, blue, alpha);
					repaint();
					//setBackground(new Color(red, green, blue, alpha));
					
					try {
						Thread.sleep(4800);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		changeThread.start();

	}
	
	public void drawLine() {
		background.drawLine();
	}

	private class DisplayRectBackground extends JComponent {

	
		/**
		 * 
		 */
		private static final long serialVersionUID = -6355246536391823955L;
		/**
		 * 
		 */
		private int x;
		private int y;
		private int width;
		private int height;

		public DisplayRectBackground(int width, int height) {
			this(0, 0, width, height);
		}

		public DisplayRectBackground(int x, int y, int width, int height) {
			
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;

			setLocation(x, y);
			setSize(width, height);
			// setFocusable(true);

			// addMouseListener(new MyListener());
			// addLabel();
			// setLayout(new GroupLayout());
			//this.
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			// g = (Graphics2D) g;
			painter = g;

			// g.
			g.setColor(edgeColor);
			g.drawRect(0, 0, width - 1, height - 1);
			g.setColor(backgroundColor);
			g.fillRect(edgeBreadth, edgeBreadth, width - 2, height - 2);
			// g.drawString("Hello World!", 26, 26);
			// drawBorder();
			// validate();

		}
		
		public void notifyRepaint() {
			if (displayRect.isDataChanged()) {
				width = displayRect.width;
				height = displayRect.height;
				
				setSize(width, height);
				revalidate();
				repaint();
			}
		}
		
		public void drawLine() {
			Graphics g = this.getGraphics();
			g.drawLine(0, 0, 50, 50);
		}
	
	}

	
	private class DisplayRectViewport extends JComponent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7044239285159053347L;

		private int x;
		private int y;
		private int width;
		private int height;

		private static final int DEFAULT_LABEL_HEIGHT = 18;
		private static final int DEFAULT_MARGIN_LEFT = 10;
		private static final int DEFAULT_MARGIN_BOTTOM = 5;
		private MaterialLabel headLabel;
		private MaterialLabel contentLabel = new MaterialLabel();
		private String text = "";


		private Container content = new Container();
		
		public DisplayRectViewport(int width, int height) {
			this(0, 0, width, height);
		}

		public DisplayRectViewport(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;

			setSize(width, height);
			setFocusable(true);
			setLocation(x, y);

			// addMouseListener(new MyListener());
			// addLabel();
			// setLayout(new GroupLayout());
			// this.

			init();
		}

		private void init() {
			addheadLabel();
			initContentContainer();
		}

		Color color = new Color(1.0f, 0.5f, 0.0f, 0.3f);
		private void addheadLabel() {
			headLabel = new MaterialLabel("test()");
			headLabel.setLocation(x, y);
			headLabel.setSize(width, DEFAULT_LABEL_HEIGHT);
			headLabel.setFont(new Font("head", Font.BOLD, 14));
			headLabel.setHorizontalAlignment(SwingConstants.CENTER);

			add(headLabel);
		}

		private void initContentContainer() {
			int contentHeight = height - (headLabel.getHeight() - 1);
			content.setLocation(0, (headLabel.getHeight()));
			System.out.println("init: " + width + " " + headLabel.getHeight());
			content.setSize(width, contentHeight);
			content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
			
			add(content);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			g.drawLine(0, headLabel.getHeight(), width, headLabel.getHeight());
			//System.out.println("paint: " + width + " " + headLabel.getHeight());
		}

		public void setText(MaterialLabel label, String text) {

			label.setText(text);

			notifyTextChange();
		}

		public void setContentItemText(String text, int position) {

			MaterialLabel label = (MaterialLabel) getComponent(position);
			setText(label, text);
		}

		public void setTextStyle(MaterialLabel label, int style) {

			label.setFont(label.getFont().deriveFont(style));
			notifyTextChange();
		}

		public void setTextSize(MaterialLabel label, float sp) {
			
			label.setFont(label.getFont().deriveFont(sp));
			notifyTextChange();
		}
		
		public void margin() {

		}

		public void marginLeft(int px) {
			
			//headLabel.setLocation(px, headLabel.getY());
		}

		public void marginTop(int px) {
			//headLabel.setLocation(px, headLabel.getY());
		}

		public void marginRight(int px) {
			
		}

		public void marginBottom(int px) {
			//label.
		}
		

		public void notifyTextChange() {

			if (isTextLengthChanged()) {
				resize();
			}
		}

		private void resize() {
			//Dimension contentDimen = content.getPreferredSize();
			Dimension dimen = getPreferredSize();
			width = dimen.width + 2 * DEFAULT_MARGIN_LEFT;
			height = dimen.height + 2 * DEFAULT_MARGIN_BOTTOM;
			setSize(width, height);
			
			int newLabelHeight = headLabel.getPreferredSize().height + 2 * DEFAULT_MARGIN_BOTTOM;
			headLabel.setSize(width, newLabelHeight);
			System.out.println("resize: " + width + " " + newLabelHeight);
			
			int contentHeight = width - headLabel.getHeight() - 1;
			content.setSize(width, contentHeight);
			content.setLocation(0, headLabel.getHeight() + 1);
			
			
			
			
			displayRect.changeSize(width, height);
			updateContent();
			System.out.println(width);
		}
		
		private void updateContent() {
			
			Thread updateThread = new Thread() {
				@Override
				public void run() {
					for (Component comp : content.getComponents()) {
						MaterialLabel label = (MaterialLabel) comp;
						label.setPreferredSize(new Dimension(width, label.getPreferredSize().height));
					}
				}
			};
			
			updateThread.start();
		}
		
		public void addContentLabel(MaterialLabel label) {
			Container c = getContentView();
			MaterialLabel firstLabel;
		
			if (c.getComponentCount() != 0) {
				
				firstLabel = (MaterialLabel) c.getComponent(0);
				label.setFont(firstLabel.getFont());
				label.setSize(firstLabel.getSize());
			} else {
				label.setFont(new Font("itemFont", Font.PLAIN, 16));
				label.setSize(width, DEFAULT_LABEL_HEIGHT);
				
				System.out.println("firstContentLabel width: " + width);
			}                 
			
			System.out.println(getContentView().getWidth());
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			c.add(label);
		
			//revalidate();
			//repaint();
			//resize();
			notifyTextChange();
		}
		
		

		public boolean isTextLengthChanged() {
			return true;
		}

		public Container getContentView() {
			return content;
		}
		
	}
}
