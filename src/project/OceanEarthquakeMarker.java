package project;

import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PGraphics;

class OceanEarthquakeMarker extends EarthquakeMarker {
    public OceanEarthquakeMarker(Location location, float magnitude, float depth,
            String age) {
        super(location, magnitude, depth, age);
    }

    public void draw(PGraphics pg, float x, float y) {
        pg.pushStyle();
        pg.noStroke();
        
        pg.fill(getColor());
        pg.ellipse(x, y, getRadius(), getRadius());
        pg.fill(getColor() | 0xFFFFFF);
        pg.ellipse(x, y, getRadius()-10, getRadius()-10);
        pg.popStyle();
      }    
}