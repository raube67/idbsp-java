package idbsp;

import static idbsp.buildbsp.BuildBSP;
import static idbsp.buildbsp.cuts;
import static idbsp.cmdlib.Error;
import static idbsp.doomload.LoadDoomMap;
import static idbsp.drawing.DrawMap;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import static idbsp.drawing.UpdateDrawing;


/**
 * doombsp.c / doombsp.h
 * 
 * @author
 *
 */
public class doombsp {

	/*
	===============================================================================

								map file types

	===============================================================================
	*/
	
	public static class NXPoint {
		public double 		x;
		public double		y;
	}

	public static class NXSize {
		public double 		width;
		public double		height;
	}

	public static class NXRect {	/* rectangle */
		public NXPoint      origin;
		public NXSize       size;
	} 
	
	public static class sectordef_t {
		public int			floorheight, ceilingheight;
		public String 		floorflat, ceilingflat;
		public int			lightlevel;
		public int			special, tag;	
	} 

	public static class worldside_t {
		public int			firstrow;	
		public int			firstcollumn;
		public String		toptexture;
		public String		bottomtexture;
		public String		midtexture;
		public sectordef_t	sectordef;			// on the viewer's side
		public int			sector;				// only used when saving doom map
	} 

	public static class worldline_t {
		public NXPoint		p1, p2;
		public int			special, tag;
		public int			flags;	
		public worldside_t[]	side;
	}
	
	public static final int ML_BLOCKMOVE	= 1;
	public static final int ML_TWOSIDED     = 4;    // backside will not be present at all if not two sided

	public static class worldthing_t {
		public NXPoint		origin;
		public int			angle;
		public int			type;
		public int			options;
		public int			area;
	} 
	
	public static class bspnode_t {
		public List<line_t>				lines_i;		// if non NULL, the node is
		public divline_t				divline;		// terminal and has no children
		public double[]					bbox;
		public bspnode_t[]				side;
	}
	
	/*
	===============================================================================

								internal types

	===============================================================================
	*/

	public static class divline_t {
		public NXPoint		pt;
		public double		dx, dy;
	};

	public static class line_t {
		public NXPoint		p1, p2;
		public int			linedef, side, offset;
		public boolean		grouped;				// internal error check
	} ;


	
	
	public static boolean draw = false;
	
	/*
	====================
	=
	= main
	=
	====================
	 */
	public static void main(String ... args) {
		
		String inmapname = null, outmapname = null;
		String basename;
		
		if (args.length == 3) {
			if (!"-draw".equals(args[0])) {
				Error("doombsp [-draw] inmap outwadpath");
			}
			inmapname = args[1];
			outmapname = args[2];
			draw = true;
		} else if (args.length == 2) {
			inmapname = args[0];
			outmapname = args[1];
			draw = false;
		} else {
			Error("doombsp [-draw] inmap outwadpath");
		}
		
		int i = inmapname.lastIndexOf('.');
		if (i == -1) {
			i = inmapname.length();
		} 
		int j = inmapname.lastIndexOf('/', i);
		if (j == -1) {
			basename = inmapname.substring(0, i);
		} else {
			basename = inmapname.substring(j + 1, i);
		}
		outmapname += '/' + basename + ".wad";
		
		System.out.println(String.format("output wadfile: %s", outmapname));
				
		LoadDoomMap(inmapname);
		DrawMap();
		BuildBSP();

		System.out.println (String.format("segment cuts: %d",cuts));

		print(buildbsp.startnode, 0);

		UpdateDrawing();
		
		//		System.exit(0);
		
		
	}
	
	private static void print(bspnode_t node, int indent) {
		System.out.println(indent(indent) + "NODE");
		if (node.lines_i != null) {
			for (line_t line : node.lines_i) {
				printLine(line, indent + 1);
			}
		}
		if (node.side != null) {
			for (bspnode_t sibling : node.side) {
				print(sibling, indent + 1);
			}
		}
	}

	private static DecimalFormat df2 = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
			
	private static void printLine(line_t line, int indent) {
		System.out.println(indent(indent) + "LINE (" + df2.format(line.p1.x) + ", " + df2.format(line.p1.y) + ") -> (" + df2.format(line.p2.x) + ", " + df2.format(line.p2.y) + ")");
	}

	private static String indent(int indent) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
}
