package project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;

/**
 * A marker class to display earthquakes on the map.
 * 
 * @author Sylvain
 *
 */
abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker> {
    // constants for distance
    protected static final float kmPerMile = 1.6f;
    
//    private int radius = 30;
    private final float magnitude;
    private final float depth;
    private final int   age;
   
    /**
     * Mapping between magnitude and color of the marker
     */
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
    
    /**
     * Mapping between "age" of an event and its transparency level
     */
    static final Map<String, Integer> ageToNum;
    static {
        Map<String, Integer> m = new HashMap<>();
        m.put("Past Hour", 3);
        m.put("Past Day", 2);
        m.put("Past Week", 1);
        m.put("Past Month", 0);
        
        ageToNum = Collections.unmodifiableMap(m);
    }
    
    /**
     * Instantiate a new EarthquakeMarker with the given attributes.
     * 
     * @param location
     * @param magnitude
     * @param depth
     * @param age
     * @param properties Additional properties passed to the UnfoldingMap library
     */
    public EarthquakeMarker(Location location, float magnitude, float depth, String age,
                            HashMap<String,Object> properties) {
      super(location, properties);
      
      this.setRadius(Math.round(2+3*magnitude));
      this.magnitude = magnitude;
      this.depth = depth;
      this.age = ageToNum.getOrDefault(age, 0);
    }
    
    /**
     * Utility method. Calculate the color of the marker based on various attributes
     * of the earthquake.
     * 
     * @return An ARGB color used to display that earthquake
     */
    public int getColor() {
        float color = PApplet.map(magnitude, 0, 10, 0, 10);
        int opacity = (isSelected()) ? 255 : 50*(age+1);
        
        return (opacity << 24) | colors[Math.round(color)];
    }
    
    /**
     * Utility method. Calculate the color of the frame of the marker.
     * The frame color is the same as the fill color, but with full opacity.
     * 
     * @return An ARGB color whose alpha component is 0xFF
     */
    public int getFrameColor() {
        return (isSelected()) ? 0xFF000000 : getColor();
    }

    public float getRadius() {
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
    
    /**
     * For testing purposes only
     */
    int getAgeNum() {
        return this.age;
    }
    
    public float getMagnitude()
    {
        return magnitude;
    }
    
    /**
     * Return the "threat circle" radius, or distance up to 
     * which this earthquake can affect things, for this earthquake.   
     * DISCLAIMER: this is not intended to be used for safety-critical 
     *  or predictive applications.
     *  
     *  @author UC San Diego (public domain)
     */
    public double threatCircle() {  
        double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
        double km = (miles * kmPerMile);
        return km;
    }
    
    /**
     * Return the title as displayed in the infobox when hovering over that event marker.
     */
    @Override
    public String getTitle() {
        return "" + getMagnitude() + " - " + getCountry() 
                              + " (depth: " + getDepth() + ", " + getAge() + ")";
        // TODO Auto-generated method stub
        
    } 
    
    /**
     * Order EarthQuake markers. Used to sort events for display/reporting purpose
     * 
     * TODO: should refactor this to separate the "Earthquake" model object from the 
     * "EarthquakeMarker" view object.
     */
    @Override
    public int compareTo(EarthquakeMarker o) {
        if (magnitude < o.magnitude)
            return +1;
        else if (magnitude > o.magnitude)
            return -1;
        
        // else
        int c = getCountry().compareTo(o.getCountry());
        
        if (c != 0)
            return 0;
        
        // else
        if (depth > o.depth)
            return +1;
        else if (depth < o.depth)
            return -1;
        else
            return 0;
    }
}
