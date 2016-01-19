package project;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

class AirportMarker extends POIMarker {
    PImage indicator = new PImage(load("plane.png"));
    
    static Image load(String fName) {
        try {
            BufferedImage image = ImageIO.read(new File(fName));
            BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(),
                                                   BufferedImage.TYPE_INT_ARGB);
            dest.getGraphics().drawImage(image, 0, 0, null);

            return dest;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }
    
    public AirportMarker(Location location) {
        super(location);
    }
    
    
    public AirportMarker(Feature city) {
        super(((PointFeature)city).getLocation(), city.getProperties());
    }
    
    
    // HINT: pg is the graphics object on which you call the graphics
    // methods.  e.g. pg.fill(255, 0, 0) will set the color to red
    // x and y are the center of the object to draw. 
    // They will be used to calculate the coordinates to pass
    // into any shape drawing methods.  
    // e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
    // whose upper left corner is at position x, y
    /**
     * Implementation of method to draw marker on the map.
     */
    public void drawMarker(PGraphics pg, float x, float y) {
        if (isThreatened()) {
            // Save previous drawing style
            pg.pushStyle();
            
            pg.imageMode(PConstants.CENTER);
            pg.image(indicator, x, y);
            
            // Restore previous drawing style
            pg.popStyle();
        }
    }
    
    /* Local getters for some city properties.  You might not need these 
     * in module 4.      */
    public String getCity()
    {
        return getStringProperty("name");
    }
    
    public String getCountry()
    {
        return getStringProperty("country");
    }


    @Override
    public String getTitle() {
        return getCity();
    }
   
}