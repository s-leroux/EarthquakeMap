package project;

import java.util.HashMap;

import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PGraphics;

class LandEarthquakeMarker extends EarthquakeMarker {
    public LandEarthquakeMarker(Location location, float magnitude, float depth,
            String age,
            HashMap<String,Object> properties) {
        super(location, magnitude, depth, age, properties);
    }

    @Override
    public void drawMarker(PGraphics pg, float x, float y) {
        pg.pushStyle();
        pg.noStroke();
        
        pg.fill(getColor());
        pg.ellipse(x, y, getRadius()*2, getRadius()*2);
        //pg.fill(getColor() | 0xFFFFFF);
        //pg.ellipse(x, y, getRadius()-10, getRadius()-10);
        pg.popStyle();
      }

   
}