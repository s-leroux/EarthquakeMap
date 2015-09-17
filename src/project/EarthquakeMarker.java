package project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PApplet;

abstract class EarthquakeMarker extends SimplePointMarker {
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
    
    public EarthquakeMarker(Location location, float magnitude, float depth, String age) {
      super(location);
      
      this.setRadius(Math.round(15+3*magnitude));
      this.magnitude = magnitude;
      this.depth = depth;
      this.age = ageToNum.getOrDefault(age, 0);
    }
    
    public int getColor() {
        float color = PApplet.map(magnitude, 0, 10, 0, 10);
        return (50*(age+1) << 24) | colors[Math.round(color)];
    }

    public int getRadius() {
        return radius;
    }
}