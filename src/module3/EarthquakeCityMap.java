package module3;

//Java utilities libraries
import java.util.ArrayList;
import java.util.List;

//Processing library
import processing.core.PApplet;
import processing.core.PGraphics;
//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

class EarthquakeMarker extends SimplePointMarker {
    final int radius = 30;
    final float magnitude;
    static final int colors[] = {
      /*  0 */ 0xFF5AFF05,
      /*  1 */ 0xFF7AF005,
      /*  2 */ 0xFFB1F005,
      /*  3 */ 0xFFD0F005,
      /*  4 */ 0xFFF0E005,
      /*  5 */ 0xFFF09D05,
      /*  6 */ 0xFFF07E05,
      /*  7 */ 0xFFF06305,
      /*  8 */ 0xFFF04005,
      /*  9 */ 0xFFF02C05,
      /* 10 */ 0xFFF00505
    };
    
    public EarthquakeMarker(Location location, float magnitude) {
      super(location);
      
      this.magnitude = magnitude;
    }
   
    public void draw(PGraphics pg, float x, float y) {
      pg.pushStyle();
      pg.noStroke();
      
      float color = PApplet.map(magnitude, 0, 10, 0, 10);
      pg.fill(colors[Math.round(color)],150);
      pg.ellipse(x, y, radius, radius);
      pg.fill(255, 150);
      pg.ellipse(x, y, radius-10, radius-10);
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
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    
	    for (PointFeature pointFeature : earthquakes) {
            System.out.println(pointFeature.getProperties());

	        Object magObj = pointFeature.getProperty("magnitude");
            float mag = Float.parseFloat(magObj.toString());
            
            Marker marker = new EarthquakeMarker(pointFeature.location, mag);
            //Marker marker = new SimplePointMarker(pointFeature.location);
            //marker.setColor(color(255,0,0));

            markers.add(marker);
            
            map.addMarker(marker);
            
        }
	    //TODO: Add code here as appropriate
	}
		
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature)
	{
		// finish implementing and use this method, if it helps.
		return new SimplePointMarker(feature.getLocation());
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
            
            fill(color);
            rect(70,70+20*i,40,20);
            text(Integer.toString(i), 120, 85+20*i);
        }
	}
}
