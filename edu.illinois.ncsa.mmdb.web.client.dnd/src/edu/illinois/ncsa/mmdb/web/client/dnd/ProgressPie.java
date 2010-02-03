package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;


// most of this code is ripped off from the Java developers' almanac
// http://www.exampledepot.com/egs/java.awt/Pie.html
public class ProgressPie extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7744385659503879941L;

	class PieValue {
        double value;
        Color color;
    
        public PieValue(double value, Color color) {
            this.value = value;
            this.color = color;
        }
    }
    
    public void drawPie(Graphics2D g, Rectangle area, PieValue[] slices) {
        // Get total value of all slices
        double total = 0.0D;
        for (int i=0; i<slices.length; i++) {
            total += slices[i].value;
        }
        g.setColor(background);
        g.fillRect(area.x, area.y, area.width, area.height);
        // Draw each pie slice
        double curValue = 0.0D;
        int startAngleOffset = 90;
        for (int i=0; i<slices.length; i++) {
        	System.out.println("drawing slice "+i);
            // Compute the start and stop angles
            int startAngle = (int)(curValue * 360 / total) + startAngleOffset;
            int arcAngle = (int)(slices[i].value * 360 / total) + startAngleOffset;
    
            // Ensure that rounding errors do not leave a gap between the first and last slice
            if (i == slices.length-1) {
            	arcAngle = 360 - (startAngle - startAngleOffset);
            }
    
            // Set the color and draw a filled arc
            g.setColor(slices[i].color);
            //int arcInset = 2;
            int pieInset = 6;
            //g.drawArc(area.x+arcInset, area.y+arcInset, area.width-(arcInset*2), area.height-(arcInset*2), 0, 360);
            g.fillArc(area.x+pieInset, area.y+pieInset, area.width-(pieInset*2), area.height-(pieInset*2), startAngle, arcAngle);
    
            curValue += slices[i].value;
        }
    }
    
    PieValue slices[] = null;
    Color background = Color.gray;
    
    public void paint(Graphics g) {
        // Draw the pie
    	if(slices != null) {
    		System.out.println("painting progress pie"); // FIXME debug
    		drawPie((Graphics2D)g, getBounds(), slices);
    	} else {
    		System.out.println("slices is null, not painting pie"); // FIXME debug
    	}
    }
    
    public void setProgress(int percent) {
    	slices = new PieValue[] {
    		new PieValue(percent, Color.white),
    		new PieValue(100-percent, Color.darkGray)
    	};
    	System.out.println("progress set to "+percent); // FIXME debug
    	repaint();
    }
    
    public void setBackground(Color bg) { background = bg; }
}
