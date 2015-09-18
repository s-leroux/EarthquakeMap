package project;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a common marker for cities and earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public abstract class CommonMarker extends SimplePointMarker {

    // Records whether this marker has been clicked (most recently)
    protected boolean clicked = false;
    
    public CommonMarker(Location location) {
        super(location);
    }
    
    public CommonMarker(Location location, java.util.HashMap<java.lang.String,java.lang.Object> properties) {
        super(location, properties);
    }
    
    // Getter method for clicked field
    public boolean getClicked() {
        return clicked;
    }
    
    // Setter method for clicked field
    public void setClicked(boolean state) {
        clicked = state;
    }
    
    // Common piece of drawing method for markers; 
    // Note that you should implement this by making calls 
    // drawMarker and showTitle, which are abstract methods 
    // implemented in subclasses
    public void draw(PGraphics pg, float x, float y) {
        // For starter code just drawMaker(...)
        if (!hidden) {
            drawMarker(pg, x, y);
            if (selected) {
                showTitle(pg, x, y);  // You will implement this in the subclasses
            }
        }
    }
    public abstract void drawMarker(PGraphics pg, float x, float y);
    public abstract String getTitle();
    
    public void showTitle(PGraphics pg, float x, float y) {
        String title = getTitle();
        
        pg.pushStyle();
        float tw = pg.textWidth(title);
        float th = pg.textAscent()+pg.textDescent();
        
        pg.stroke(0xFFA89E6F);
        pg.fill(0xFFEBDFAE);
        pg.rect(x-5-tw/2, y-5-pg.textAscent(), tw + 10, th+10);
        pg.fill(0,0,0);
        pg.text(title, x-tw/2, y);
        pg.popStyle();
    }
}