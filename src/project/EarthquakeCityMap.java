package project;

//Java utilities libraries
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Processing library
import processing.core.PApplet;
import processing.core.PGraphics;
//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
//Parsing library
import parsing.ParseFeed;

class CityMarker extends SimplePointMarker {
    
    // The size of the triangle marker
    // It's a good idea to use this variable in your draw method
    public static final int TRI_SIZE = 5;  
    
    public CityMarker(Location location) {
        super(location);
    }
    
    
    public CityMarker(Feature city) {
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
    public void draw(PGraphics pg, float x, float y) {
        // Save previous drawing style
        pg.pushStyle();
        
        pg.fill(0xFF8F00FF);
        pg.triangle(x-5, y+5, x+5, y+5, x, y-5);
        // TODO: Add code to draw a triangle to represent the CityMarker
        
        // Restore previous drawing style
        pg.popStyle();
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
    
    public float getPopulation()
    {
        return Float.parseFloat(getStringProperty("population"));
    }    
}


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

class LandEarthquakeMarker extends EarthquakeMarker {
    public LandEarthquakeMarker(Location location, float magnitude, float depth,
            String age) {
        super(location, magnitude, depth, age);
    }

    public void draw(PGraphics pg, float x, float y) {
        pg.pushStyle();
        pg.noStroke();
        
        pg.fill(getColor());
        pg.ellipse(x, y, getRadius(), getRadius());
        //pg.fill(getColor() | 0xFFFFFF);
        //pg.ellipse(x, y, getRadius()-10, getRadius()-10);
        pg.popStyle();
      }    
}

class OtherEarthquakeMarker extends EarthquakeMarker {
    public OtherEarthquakeMarker(Location location, float magnitude, float depth,
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

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
    // The files containing city names and info and country names and info
    private String cityFile = "city-data.json";
    private String countryFile = "countries.geo.json";

    private List<Marker> countryMarkers;

    private ArrayList<Marker> cityMarkers;

    private List<PointFeature> earthquakes;
	
	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
	
  earthquakesURL = "quiz1.atom";

		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
	    
        //     STEP 1: load country features and markers
        List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
        countryMarkers = MapUtils.createSimpleMarkers(countries);
        
        //      STEP2: load cities
        List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
        cityMarkers = new ArrayList<Marker>();
        for(Feature city : cities) {
          cityMarkers.add(new CityMarker(city));
        }
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    
	    for (PointFeature pointFeature : earthquakes) {

            map.addMarker(createMarker(pointFeature));
            System.out.println(pointFeature.getProperties());
        }
	    
	    map.addMarkers(cityMarkers);
	    
	    printQuakes();
	}
		
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature pointFeature)
	{
        float mag = Float.parseFloat(pointFeature.getProperty("magnitude").toString());
        float depth = Float.parseFloat(pointFeature.getProperty("depth").toString());
        String age = pointFeature.getProperty("age").toString();
        
        return (isLand(pointFeature)) ?
                  new LandEarthquakeMarker(pointFeature.location, mag, depth, age) :
                  new OtherEarthquakeMarker(pointFeature.location, mag, depth, age)   ;
	}
	
	public void draw() {
	    background(127);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
	    fill(255,255,255);
	    rect(50,50,100,300);
		// Remember you can use Processing's graphics methods here
	    for (int i = 0; i < EarthquakeMarker.colors.length; i++) {
            int color = EarthquakeMarker.colors[i];
            
            fill(color|0xFF000000);
            rect(70,70+20*i,40,20);
            text(Integer.toString(i), 120, 85+20*i);
        }
	}
	
	   
    // Checks whether this quake occurred on land.  If it did, it sets the 
    // "country" property of its PointFeature to the country where it occurred
    // and returns true.  Notice that the helper method isInCountry will
    // set this "country" property already.  Otherwise it returns false.
    private boolean isLand(PointFeature earthquake) {
        for (Marker marker : countryMarkers) {
            if (isInCountry(earthquake, marker))
                   return true;
        }

        return false;
    }
    
    
    // helper method to test whether a given earthquake is in a given country
    // This will also add the country property to the properties of the earthquake 
    // feature if it's in one of the countries.
    // You should not have to modify this code
    private boolean isInCountry(PointFeature earthquake, Marker country) {
        // getting location of feature
        Location checkLoc = earthquake.getLocation();

        // some countries represented it as MultiMarker
        // looping over SimplePolygonMarkers which make them up to use isInsideByLoc
        if(country.getClass() == MultiMarker.class) {
                
            // looping over markers making up MultiMarker
            for(Marker marker : ((MultiMarker)country).getMarkers()) {
                    
                // checking if inside
                if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
                    earthquake.addProperty("country", country.getProperty("name"));
                        
                    // return if is inside one
                    return true;
                }
            }
        }
            
        // check if inside country represented by SimplePolygonMarker
        else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
            earthquake.addProperty("country", country.getProperty("name"));
            
            return true;
        }

        earthquake.addProperty("country", "ocean");
        return false;
    }
    
    // prints countries with number of earthquakes
    // You will want to loop through the country markers or country features
    // (either will work) and then for each country, loop through
    // the quakes to count how many occurred in that country.
    // Recall that the country markers have a "name" property, 
    // And LandQuakeMarkers have a "country" property set.
    private void printQuakes() 
    {
        Map<String, Integer> map = new HashMap<>();
        
        for (PointFeature earthquake : earthquakes) {
            String country = earthquake.getProperty("country").toString();
            map.put(country, 1+map.getOrDefault(country, 0));
        }
        
        for (Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey()+": "+entry.getValue().toString());
        }

    }


}
