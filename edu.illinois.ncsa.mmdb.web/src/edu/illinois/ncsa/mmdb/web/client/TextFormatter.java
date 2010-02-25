/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

/**
 * Static methods to format text.
 * 
 * @author Luigi Marini
 * 
 */
public class TextFormatter {

	/**
	 * Format bytes.
	 * 
	 * @param x
	 *            number of bytes
	 * @return formatted string
	 */
	public static String humanBytes(long x) {
		if (x == Integer.MAX_VALUE) {
			return "No limit";
		}
		if (x < 1e3) {
			return x + " bytes";
		} else if (x < 1e6) {
			return (int) (x / 1e3 * 100) / 100.0 + " KB";
		} else if (x < 1e9) {
			return (int) (x / 1e6 * 100) / 100.0 + " MB";
		} else if (x < 1e12) {
			return (int) (x / 1e9 * 100) / 100.0 + " GB";
		} else if (x < 1e15) {
			return (int) (x / 1e12 * 100) / 100.0 + " TB";
		} else {
			return x + " bytes";
		}
	}

}
