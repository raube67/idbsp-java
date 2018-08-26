package idbsp;

/**
 * cmdlib.c / cmdlib.h
 * 
 * @author
 *
 */
public class cmdlib {

	/*
	====================
	=
	= Error
	=
	= For abnormal program terminations
	=
	====================
	 */
	public static void Error(String error, Object ... args) {
		System.err.println(String.format(error, args));
		System.exit(1);
	}
}
