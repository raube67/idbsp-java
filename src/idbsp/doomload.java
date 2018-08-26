package idbsp;

import static idbsp.cmdlib.*;
import static idbsp.doombsp.*;
import static idbsp.buildbsp.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import idbsp.doombsp.NXPoint;
import idbsp.doombsp.divline_t;
import idbsp.doombsp.sectordef_t;
import idbsp.doombsp.worldline_t;
import idbsp.doombsp.worldside_t;
import idbsp.doombsp.worldthing_t;

/**
 * doomload.m / doomload.h
 * 
 * @author
 *
 */
public class doomload {

	private static final int WORLD_SERVER_VERSION = 4;
	private static final Pattern PATTERN_VERSION = Pattern.compile("^WorldServer version (\\d+)$");
	private static final Pattern PATTERN_LINECOUNT = Pattern.compile("^lines:(\\d+)$");
	private static final Pattern PATTERN_THINGCOUNT = Pattern.compile("^things:(\\d+)$");
	
	private static final Pattern PATTERN_WORLDLINE_1 = Pattern.compile("^\\((.+),(.+)\\) to \\((.+),(.+)\\) : (.+) : (.+) : (.+)$");
	private static final Pattern PATTERN_WORLDLINE_2 = Pattern.compile("^    (.+) \\((.+) : (.+) / (.+) / (.+) \\)$");
	private static final Pattern PATTERN_WORLDLINE_3 = Pattern.compile("^    (.+) : (.+) (.+) : (.+) (.+) (.+) (.+)$");
	
	private static final Pattern PATTERN_WORLDTHING = Pattern.compile("^\\((.+),(.+), (.+)\\) :(.+), (.+)$");
		
	public static List<worldline_t> linestore_i = new ArrayList<>();
	public static List<worldthing_t> thingstore_i = new ArrayList<>();
	
	
	/*
	===================
	=
	= LoadDoomMap
	=
	===================
	*/
	public static void LoadDoomMap(String mapname) {
		
		Path path = Paths.get(mapname);
		try (Stream<String> stream = Files.lines(path, Charset.forName("iso-8859-1"))) {
			Iterator<String> iterator = stream.iterator();
			
			int version = ReadWorldServerVersion(iterator);		
			if (version == WORLD_SERVER_VERSION) {
				System.out.println(String.format("Loading version %s doom map: %s", WORLD_SERVER_VERSION, mapname));
			} else {
				Error("LoadDoomMap: not a version %s doom map", WORLD_SERVER_VERSION);
			}
			
			// 
			// read lines
			//
			
			int linecount = ReadLineCount(iterator);
			if (linecount < 0) {
				Error("LoadDoomMap: can't read linecount");
			}
			System.out.println(String.format("%d lines", linecount));
			
			for (int i = 0; i < linecount; i++) {
				worldline_t line = ReadLine(iterator, i);
				if (line == null) {
					Error("Failed ReadLine");
				}
				if (line.p1.x == line.p2.x && line.p1.y == line.p2.y) {
					System.err.println(String.format("WARNING: line %d is length 0 (removed)", i));
					continue;
				}
				if (LineOverlaid(line)) {
					System.err.println(String.format("WARNING: line %d is overlaid (removed)", i));
					continue;
				}
				linestore_i.add(line);
			}
			
			//
			// read things
			//
			int thingcount = ReadThingCount(iterator);
			if (thingcount < 0) {
				Error("LoadDoomMap: can't read thingcount");
			}
			System.out.println(String.format("%d things", thingcount));

			for (int i = 0; i < thingcount; i++) {
				worldthing_t thing = ReadThing(iterator, i);
				if (thing == null) {
					Error("Failed ReadThing");
				}
				thingstore_i.add(thing);
			}

		} catch (IOException e) {
			Error("LoadDoomMap: couldn't open %s [%s: %s]", mapname, e.getClass().getName(), e.getMessage());
		}
	}
	
	private static int ReadWorldServerVersion(Iterator<String> iterator) {
		try {
			Matcher m = PATTERN_VERSION.matcher(iterator.next());
			return (m.matches() ? Integer.parseInt(m.group(1)) : -1);
		} catch (Exception e) {
			return -1;
		}
	}

	private static int ReadLineCount(Iterator<String> iterator) {
		try {
			if (!iterator.next().trim().isEmpty()) {
				return -1;
			}
			Matcher m = PATTERN_LINECOUNT.matcher(iterator.next());
			return (m.matches() ? Integer.parseInt(m.group(1)) : -1);
		} catch (Exception e) {
			return -1;
		}
	}

	private static int ReadThingCount(Iterator<String> iterator) {
		try {
			if (!iterator.next().trim().isEmpty()) {
				return -1;
			}
			Matcher m = PATTERN_THINGCOUNT.matcher(iterator.next());
			return (m.matches() ? Integer.parseInt(m.group(1)) : -1);
		} catch (Exception e) {
			return -1;
		}
	}
	
	private static worldthing_t ReadThing(Iterator<String> iterator, int linenum) {
		try {
			worldthing_t thing = new worldthing_t();
			thing.origin = new NXPoint();
	
			Matcher m1 = PATTERN_WORLDTHING.matcher(iterator.next());
			if (!m1.matches()) {
				Error("Failed ReadThing");
			}
			int x = Integer.parseInt(m1.group(1));
			int y = Integer.parseInt(m1.group(2));
			thing.angle = Integer.parseInt(m1.group(3));
			thing.type = Integer.parseInt(m1.group(4));
			thing.options = Integer.parseInt(m1.group(5));

			thing.origin.x = x & -16;
			thing.origin.y = y & -16;
			
			return thing;
			
		} catch (Exception e) {
			return null;
		}
	}

	private static worldline_t ReadLine(Iterator<String> iterator, int linenum) {
		try {
			worldline_t line = new worldline_t();
			line.p1 = new NXPoint();
			line.p2 = new NXPoint();
			
			Matcher m1 = PATTERN_WORLDLINE_1.matcher(iterator.next());
			if (!m1.matches()) {
				Error("Failed ReadLine");
			}
			line.p1.x = Double.parseDouble(m1.group(1));
			line.p1.y = Double.parseDouble(m1.group(2));
			line.p2.x = Double.parseDouble(m1.group(3));
			line.p2.y = Double.parseDouble(m1.group(4));
			line.flags = Integer.parseInt(m1.group(5));
			line.special = Integer.parseInt(m1.group(6));
			line.tag = Integer.parseInt(m1.group(7));
			
			int sides = ((line.flags & ML_TWOSIDED) == 0 ? 1 : 2);
			line.side = new worldside_t[sides];

			for (int i = 0; i < sides; i++) {
				worldside_t s = line.side[i] = new worldside_t();
				sectordef_t e = line.side[i].sectordef = new sectordef_t();

				Matcher m2 = PATTERN_WORLDLINE_2.matcher(iterator.next());
				if (!m2.matches()) {
					Error("Failed ReadLine (side)");
				}
				s.firstrow = Integer.parseInt(m2.group(1));
				s.firstcollumn = Integer.parseInt(m2.group(2));
				s.toptexture = m2.group(3);
				s.bottomtexture = m2.group(4);
				s.midtexture = m2.group(5);
				
				Matcher m3 = PATTERN_WORLDLINE_3.matcher(iterator.next());
				if (!m3.matches()) {
					Error("Failed ReadLine (sector)");
				}
				e.floorheight = Integer.parseInt(m3.group(1));
				e.floorflat = m3.group(2);
				e.ceilingheight = Integer.parseInt(m3.group(3));
				e.ceilingflat = m3.group(4);
				e.lightlevel = Integer.parseInt(m3.group(5));
				e.special = Integer.parseInt(m3.group(6));
				e.tag = Integer.parseInt(m3.group(7));
				
				if ("-".equals(e.floorflat)) {
					System.err.println(String.format("WARNING: line %d has no sectordef", linenum));
				}
			}
			
			return line;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	/*
	==================
	=
	= LineOverlaid
	=
	= Check to see if the line is colinear and overlapping any previous lines
	==================
	*/
	
	private static class bbox_t {
		public double 		left;
		public double		right;
		public double		top;
		public double		bottom;
	}

	private static void BBoxFromPoints(bbox_t box, NXPoint p1, NXPoint p2) {
		if (p1.x < p2.x) {
			box.left = p1.x;
			box.right = p2.x;
		} else {
			box.left = p2.x;
			box.right = p1.x;
		}
		if (p1.y < p2.y) {
			box.bottom = p1.y;
			box.top = p2.y;
		} else {
			box.bottom = p2.y;
			box.top = p1.y;
		}
	}
	
	private static boolean LineOverlaid(worldline_t line) {
		divline_t wl = new divline_t();
		wl.pt = line.p1;
		wl.dx = line.p2.x - line.p1.x;
		wl.dy = line.p2.y - line.p1.y;
		bbox_t linebox = new bbox_t();
		bbox_t scanbox = new bbox_t();
		BBoxFromPoints (linebox, line.p1, line.p2);
		
		for (worldline_t scan : linestore_i) {
			if (PointOnSide(scan.p1, wl) != -1) {
				continue;
			}
			if (PointOnSide(scan.p2, wl) != -1) {
				continue;
			}
			// line is colinear, see if it overlaps
			
			BBoxFromPoints (scanbox, scan.p1, scan.p2);
			if (linebox.right  > scanbox.left && linebox.left < scanbox.right) {
				return true;
			}
			if (linebox.bottom < scanbox.top && linebox.top > scanbox.bottom) {
				return true;
			}
		}
		return false;
	}
}
