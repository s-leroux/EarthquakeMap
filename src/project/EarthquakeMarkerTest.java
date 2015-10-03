package project;

import java.util.HashMap;

import org.junit.Test;

import de.fhpotsdam.unfolding.geo.Location;
import junit.framework.TestCase;
import processing.core.PGraphics;

/**
 * Test suite for EarthquakeMarker
 * 
 * @author Sylvain
 */
public class EarthquakeMarkerTest extends TestCase {
    private EarthquakeMarker buildEarthqualeMarker(Location location, float magnitude,
            float depth, String age, HashMap<String, Object> properties) {
        return new EarthquakeMarker(location, magnitude, depth, age, properties) {
            
            @Override
            public void drawMarker(PGraphics pg, float x, float y) {
                // Do nothing stub method
            }
        };
    }
    
    @Test
    public void testBasicProperties() {
        Location    loc = new Location(-37.4526, 2.5124);
        float       mag = 5.5f;
        float       depth = 8.1f;
        String      age = "Past Day";
        HashMap<String, Object> prop = new HashMap<>();
        
        EarthquakeMarker em = buildEarthqualeMarker(loc, mag, depth, age, prop);
        
        assertEquals(loc, em.getLocation());
        assertEquals(mag, em.getMagnitude());
        assertEquals(depth, em.getDepth());
    }
    
    /**
     * Specific test for the age property as the string value is mapped to an integer
     * or 0 if not recognized.
     */
    @Test
    public void testAgeProperty() {
        Location    loc = new Location(-37.4526, 2.5124);
        float       mag = 5.5f;
        float       depth = 8.1f;
        HashMap<String, Object> prop = new HashMap<>();
        
        EarthquakeMarker em;
        
        em = buildEarthqualeMarker(loc, mag, depth, "Past Month", prop);
        assertEquals(0, em.getAgeNum());
     
        em = buildEarthqualeMarker(loc, mag, depth, "Past Week", prop);
        assertEquals(1, em.getAgeNum());
        
        
        em = buildEarthqualeMarker(loc, mag, depth, "Past Day", prop);
        assertEquals(2, em.getAgeNum());
        
        
        em = buildEarthqualeMarker(loc, mag, depth, "Past Hour", prop);
        assertEquals(3, em.getAgeNum());
        
        em = buildEarthqualeMarker(loc, mag, depth, "XXX", prop);
        assertEquals(0, em.getAgeNum());
    }
    
    /**
     * Check the presence of basic data in the displayed title
     */
    @Test
    public void testTitleContent() {
        Location    loc = new Location(-37.4526, 2.5124);
        String      magAsStr = "5.4";
        String      depthAsStr = "8.1";
        String      country = "France";
        
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("country", country);
        
        EarthquakeMarker em = buildEarthqualeMarker(loc, 
                                        Float.parseFloat(magAsStr),
                                        Float.parseFloat(depthAsStr),
                                        "Past Hour",
                                        prop);
                                
        String title = em.getTitle();
        assertTrue(title.indexOf(magAsStr) >= 0);
        assertTrue(title.indexOf(depthAsStr) >= 0);
        assertTrue(title.indexOf(country) >= 0);
    }
    
    /**
     * Test ordering based on the magnitude DESCENDING
     */
    @Test
    public void testOrdering() {
        Location    loc = new Location(-37.4526, 2.5124);
        float       depth = 8.1f;
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("country", "France");
        
        EarthquakeMarker em1 = buildEarthqualeMarker(loc, 
                5.4f,
                depth,
                "Past Hour",
                prop);
        EarthquakeMarker em2 = buildEarthqualeMarker(loc, 
                8.2f,
                depth,
                "Past Hour",
                prop);
                                
        assertTrue(em1.compareTo(em2) > 0);
        assertTrue(em2.compareTo(em1) < 0);
        assertTrue(em1.compareTo(em1) == 0);
        assertTrue(em2.compareTo(em2) == 0);
    }
}
