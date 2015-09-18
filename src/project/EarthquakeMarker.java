package project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PApplet;
import processing.core.PGraphics;

abstract class EarthquakeMarker extends CommonMarker {
    // constants for distance
    protected static final float kmPerMile = 1.6f;
    
    private int radius = 30;
    private final float magnitude;
    private final float depth;
    private final int   age;
    
    static final int colors[] = {
      /*  0 */ 0x005AFF05,
      /*  1 */ 0x007AF005,
      /*  2 */ 0x00B1F005,
      /*  3 */ 0x00D0F005,
      /*  4 */ 0x00F0E005,
      /*  5 */ 0x00F09D05,
      /*  6 */ 0x00F07E05,
      /*  7 */ 0x00F06305,
      /*  8 */ 0x00F04005,
      /*  9 */ 0x00F02C05,
      /* 10 */ 0x00F00505
    };
    
    static final Map<String, Integer> ageToNum;
    static {
        Map<String, Integer> m = new HashMap<>();
        m.put("Past Hour", 3);
        m.put("Past Day", 2);
        m.put("Past Week", 1);
        m.put("Past Month", 0);
        
        ageToNum = Collections.unmodifiableMap(m);
    }
    
    public EarthquakeMarker(Location location, float magnitude, float depth, String age,
                            HashMap<String,Object> properties) {
      super(location, properties);
      
      this.setRadius(Math.round(15+3*magnitude));
      this.magnitude = magnitude;
      this.depth = depth;
      this.age = ageToNum.getOrDefault(age, 0);
    }
    
    public int getColor() {
        float color = PApplet.map(magnitude, 0, 10, 0, 10);
        int opacity = (isSelected()) ? 255 : 50*(age+1);
        
        return (opacity << 24) | colors[Math.round(color)];
    }
    
    public int getFrameColor() {
        return (isSelected()) ? 0xFF000000 : getColor();
    }

    public int getRadius() {
        return radius;
    }
    
    public String getCountry()
    {
        return getStringProperty("country");
    }
    
    public float getDepth()
    {
        return depth;
    }
    
    public String getAge()
    {
        return getStringProperty("age");
    }    
    public float getMagnitude()
    {
        return magnitude;
    }
    
    /**
     * Return the "threat circle" radius, or distance up to 
     * which this earthquake can affect things, for this earthquake.   
     * DISCLAIMER: this formula is for illustration purposes
     *  only and is not intended to be used for safety-critical 
     *  or predictive applications.
     */
    public double threatCircle() {  
        double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
        double km = (miles * kmPerMile);
        return km;
    }
    
    @Override
    public String getTitle() {
        return "" + getMagnitude() + " - " + getCountry() 
                              + " (depth: " + getDepth() + ", " + getAge() + ")";
        // TODO Auto-generated method stub
        
    } 
}