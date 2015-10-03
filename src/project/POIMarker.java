package project;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

abstract class POIMarker extends CommonMarker {
    private boolean threatened = false;  
    
    public POIMarker(Location location) {
        super(location);
    }
    
    public POIMarker(Feature city) {
        super(((PointFeature)city).getLocation(), city.getProperties());
    }
    
    public POIMarker(Location location, java.util.HashMap<java.lang.String,java.lang.Object> properties) {
        super(location, properties);
    }
    
    /* Local getters for some properties. */
    public String getName()
    {
        return getStringProperty("name");
    }
    
    public String getCountry()
    {
        return getStringProperty("country");
    }

    public void setThreatened(boolean b) {
        threatened  = b;
    }    
    
    public boolean isThreatened() {
        return threatened;
    }
}