package idbsp;

import static idbsp.cmdlib.Error;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JFrame;

import idbsp.doombsp.NXPoint;
import idbsp.doombsp.NXRect;
import idbsp.doombsp.NXSize;
import idbsp.doombsp.bspnode_t;
import idbsp.doombsp.line_t;
import idbsp.doombsp.worldline_t;
import idbsp.doombsp.worldthing_t;

public class drawing {

	
	public static NXRect		worldbounds;
	public static double 		scale = 0.2; //25; // 0.125;
	
	private static JFrame		frame;
	private static Canvas		canvas;
	
	
	static {
		worldbounds = new NXRect();
		worldbounds.origin = new NXPoint();
		worldbounds.size = new NXSize();
	}
	
//	private static final class Area extends Window {
//
//		private GraphicsDevice screen;
//		
//		public Area(GraphicsDevice screen) throws HeadlessException {
//			super(new Frame());
//			this.screen = screen;
//
//			addMouseListener(new MouseAdapter() {
//				public void mouseClicked(MouseEvent e) {
//					screen.setFullScreenWindow(null);
//				}
//			});
//		}
//
//		public void paint(Graphics g) {
////			Font font = new Font("Arial Bold", Font.PLAIN, 200);
////			g.setFont(font);
//			g.drawLine(100,  100,  600, 700);
//
//		}		
//	}
//	
	
	/*
	===========
	=
	= DrawMap
	=
	===========
	*/

	@SuppressWarnings("serial")
	public static void DrawMap () {
		BoundLineStore(doomload.linestore_i, worldbounds);

		worldbounds.origin.x -= 100;
		worldbounds.origin.y -= 100;
		worldbounds.size.width += 200;
		worldbounds.size.height += 200;
		
		if (!doombsp.draw)
			return;

		NXRect	scaled = new NXRect();
		scaled.origin = new NXPoint();
		scaled.size = new NXSize();
		scaled.origin.x = 300;
		scaled.origin.y = 80;
		scaled.size.width = worldbounds.size.width * scale;
		scaled.size.height = worldbounds.size.height * scale;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice screen = ge.getDefaultScreenDevice();
//
//			if (!screen.isFullScreenSupported()) {
//				Error("Full screen mode not supported");
//			}
//
		GraphicsConfiguration gc = screen.getDefaultConfiguration();
		frame = new JFrame(gc);
		canvas = new Canvas(gc) {

			@Override
			public void paint(Graphics g) {
				g.setColor(Color.BLUE);
				DrawCoordinates(g);
				g.setColor(Color.YELLOW);
				DrawLineStore(g);
				g.setColor(Color.RED);
				DrawThingStore(g);
				g.setColor(Color.WHITE);
				DrawBestLines(g);
//				g.setColor(Color.ORANGE);
//				DrawSectors(g);
			}

		};
		canvas.setBackground(Color.BLACK);
		frame.getContentPane().add(canvas);
		frame.getContentPane().setPreferredSize(new Dimension((int) scaled.size.width, (int) scaled.size.height));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
//			Area area = new Area(screen);
//			screen.setFullScreenWindow(area);
			
//		for (int i = 0; i < 10; i++) {
//			try {
//				Thread.sleep(1000);
//				System.out.println("repaint");
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			c.repaint();
//		}
		
	}

	
	public static void UpdateDrawing() {
		canvas.repaint();
	}
	
	/*
	===========
	=
	= BoundLineStore
	=
	===========
	*/

	private static void BoundLineStore(List<worldline_t> lines, NXRect r) {
		if (lines.isEmpty()) {
			Error("BoundLineStore: empty list");
		}

		worldline_t line_p = lines.get(0);
		IDRectFromPoints(r, line_p.p1, line_p.p2);

		for (int i = 1; i < lines.size(); i++) {
			line_p = lines.get(i);
			IDEnclosePoint(r, line_p.p1);
			IDEnclosePoint(r, line_p.p2);
		}
	}
	
	/*
	================
	=
	= IDRectFromPoints
	=
	= Makes the rectangle just touch the two points
	=
	================
	*/

	private static void IDRectFromPoints(NXRect rect, NXPoint p1, NXPoint p2) {
		// return a rectangle that encloses the two points
		if (p1.x < p2.x) {
			rect.origin.x = p1.x;
			rect.size.width = p2.x - p1.x + 1;
		} else {
			rect.origin.x = p2.x;
			rect.size.width = p1.x - p2.x + 1;
		}
		
		if (p1.y < p2.y) {
			rect.origin.y = p1.y;
			rect.size.height = p2.y - p1.y + 1;
		} else {
			rect.origin.y = p2.y;
			rect.size.height = p1.y - p2.y + 1;
		}
	}

	/*
	==================
	=
	= IDEnclosePoint
	=
	= Make the rect enclose the point if it doesn't already
	=
	==================
	*/

	private static void IDEnclosePoint(NXRect rect, NXPoint point) {
		double right = rect.origin.x + rect.size.width - 1;
		double top = rect.origin.y + rect.size.height - 1;

		if (point.x < rect.origin.x) {
			rect.origin.x = point.x;
		}
		if (point.y < rect.origin.y) {
			rect.origin.y = point.y;
		}
		if (point.x > right) {
			right = point.x;
		}
		if (point.y > top) {
			top = point.y;
		}

		rect.size.width = right - rect.origin.x + 1;
		rect.size.height = top - rect.origin.y + 1;
	}
	
	/*
	===========
	=
	= DrawLineStore
	=
	= Draws all of the lines in the given storage object
	=
	===========
	*/

	public static void DrawLineStore (Graphics g) {
		for (worldline_t line_p : doomload.linestore_i) {
			int x1 = (int) ((line_p.p1.x - worldbounds.origin.x) * scale);
			int x2 = (int) ((line_p.p2.x - worldbounds.origin.x) * scale);
			int y1 = (int) ((worldbounds.size.height - (line_p.p1.y - worldbounds.origin.y)) * scale);
			int y2 = (int) ((worldbounds.size.height - (line_p.p2.y - worldbounds.origin.y)) * scale);
			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	public static void DrawLine(Graphics g, line_t line_p) {
		int x1 = (int) ((line_p.p1.x - worldbounds.origin.x) * scale);
		int x2 = (int) ((line_p.p2.x - worldbounds.origin.x) * scale);
		int y1 = (int) ((worldbounds.size.height - (line_p.p1.y - worldbounds.origin.y)) * scale);
		int y2 = (int) ((worldbounds.size.height - (line_p.p2.y - worldbounds.origin.y)) * scale);
		g.drawLine(x1, y1, x2, y2);
	}

	public static void FillPolygon(Graphics g, List<line_t> lines) {
		int nPoints = lines.size() * 2;
		int[] xPoints = new int[nPoints];
		int[] yPoints = new int[nPoints];
		for (int i = 0; i < lines.size(); i++) {
			line_t line_p = lines.get(i);
			int x1 = (int) ((line_p.p1.x - worldbounds.origin.x) * scale);
			int x2 = (int) ((line_p.p2.x - worldbounds.origin.x) * scale);
			int y1 = (int) ((worldbounds.size.height - (line_p.p1.y - worldbounds.origin.y)) * scale);
			int y2 = (int) ((worldbounds.size.height - (line_p.p2.y - worldbounds.origin.y)) * scale);
			xPoints[i*2] = x1;
			yPoints[i*2] = y1;
			xPoints[i*2+1] = x2;
			yPoints[i*2+1] = y2;
		}
		g.fillPolygon(xPoints, yPoints, nPoints);
	}

	public static void DrawBestLines (Graphics g) {
		for (line_t line_p : buildbsp.bestlines_i) {
			int x1 = (int) ((line_p.p1.x - worldbounds.origin.x) * scale);
			int x2 = (int) ((line_p.p2.x - worldbounds.origin.x) * scale);
			int y1 = (int) ((worldbounds.size.height - (line_p.p1.y - worldbounds.origin.y)) * scale);
			int y2 = (int) ((worldbounds.size.height - (line_p.p2.y - worldbounds.origin.y)) * scale);
			g.drawLine(x1, y1, x2, y2);
		}
	}

	public static void DrawThingStore(Graphics g) {
		for (worldthing_t thing_p : doomload.thingstore_i) {
			if (thing_p == null || thing_p.origin == null) {
				System.out.println("!");
			}
			int x1 = (int) ((thing_p.origin.x - worldbounds.origin.x) * scale);
			int y1 = (int) ((worldbounds.size.height - (thing_p.origin.y - worldbounds.origin.y)) * scale);
			g.drawOval(x1, y1,  5, 5);
		}
	}
	
	public static void DrawCoordinates(Graphics g) {
		int x = (int) ((0 - worldbounds.origin.x) * scale);
		int y = (int) ((worldbounds.size.height - (0 - worldbounds.origin.y)) * scale);
		g.drawLine(x, 0, x, 10000);
		g.drawLine(0, y, 10000, y);
	}

	public static void DrawSectors(Graphics g) {
		DrawNode(buildbsp.startnode, g);
	}

	private static void DrawNode(bspnode_t node, Graphics g) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (node.lines_i != null) {
			FillPolygon(g, node.lines_i);
		}
		if (node.side != null) {
			for (bspnode_t sibling : node.side) {
				DrawNode(sibling, g);
			}
		}
	}


	
}
