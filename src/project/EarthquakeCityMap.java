package project;

//Java utilities libraries
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.omg.CORBA.FloatSeqHelper;

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
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import module5.CommonMarker;
//Parsing library
import parsing.ParseFeed;

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
    private ArrayList<POIMarker> poiMarkers;
    private ArrayList<EarthquakeMarker> quakeMarkers;
    private Marker            lastSelected;
    private Marker            lastClicked;


    private List<PointFeature> earthquakes;

    private int threatCount;
	
	public void setup() {
		size(950, 630, OPENGL);
		
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 530, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 530, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
	    
        //     STEP 1: load country features and markers
        List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
        countryMarkers = MapUtils.createSimpleMarkers(countries);
        
        //      STEP2: load cities
        List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
        poiMarkers = new ArrayList<>();
        for(Feature city : cities) {
            CityMarker marker = new CityMarker(city); 
            marker.setProperty("type", "city");
            poiMarkers.add(marker);
        }
			
        /*
        List<PointFeature> airports = ParseFeed.parseAirports(this, "airports.dat");
        for(Feature airport : airports) {
            AirportMarker marker = new AirportMarker(airport);
            poiMarkers.add(marker);
        }
        */
                
        // STEP 3: create earthquake markers
	    earthquakesURL = "quiz2.atom";
	    earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    
	    quakeMarkers = new ArrayList<>();
	    for (PointFeature pointFeature : earthquakes) {
	        EarthquakeMarker marker = createMarker(pointFeature);
	        marker.setProperty("type", "earthquake");
            quakeMarkers.add(marker);
            map.addMarker(marker);
            System.out.println(pointFeature.getProperties());
        }
	    
	    // STEP 4: add city markers to the map
	    for (POIMarker marker : poiMarkers) {
	        map.addMarker(marker);            
        }
	    
	    printQuakes();
	    sortAndPrint(100);
	}
		
	private void sortAndPrint(int numToPrint) {
	    EarthquakeMarker[] array = new EarthquakeMarker[quakeMarkers.size()];
	    
	    Arrays.sort(quakeMarkers.toArray(array));
	    
	    for(int i = 0; i < numToPrint && i < array.length; ++i) {
	        System.out.println(array[i].getTitle());
	    }
    }

    // A suggested helper method that takes in an earthquake feature and 
	// returns a EarthquakeMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private EarthquakeMarker createMarker(PointFeature pointFeature)
	{
        float mag = Float.parseFloat(pointFeature.getProperty("magnitude").toString());
        float depth = Float.parseFloat(pointFeature.getProperty("depth").toString());
        String age = pointFeature.getProperty("age").toString();
        
        return (isLand(pointFeature)) ?
                  new LandEarthquakeMarker(pointFeature.location, mag, depth, age, pointFeature.getProperties()) :
                  new OceanEarthquakeMarker(pointFeature.location, mag, depth, age, pointFeature.getProperties())   ;
	}
	
	public void draw() {
	    threatCount = 0;
	    EarthquakeMarker quake = null;
	    if (lastClicked instanceof EarthquakeMarker)
	        quake = (EarthquakeMarker)lastClicked;
	    else if (lastSelected instanceof EarthquakeMarker)
            quake = (EarthquakeMarker)lastSelected;
	   
	    TreeMap<Double,POIMarker> threatened = new TreeMap<>();
	    
        for (POIMarker city : poiMarkers) {
            boolean threat = (quake != null) 
                                && (city.getDistanceTo(quake.getLocation())<= quake.threatCircle());
            city.setThreatened(threat);
            if (threat) {
                threatCount++;
                if (threatCount<=5)
                    threatened.put(city.getDistanceTo(quake.getLocation()), city);
            }
	    }
	    
	    background(200);
	    map.draw();
	    
	    // TODO: this is ugly !
	    // Unfortunately, a marker instance does not have a reference to its map
	    // when drawing, so it cannot easily maps arbitrary location to screen positions
        if (quake != null)
            showThreadCircle(g, quake);
	 
	    addKey(threatened);
	}
	
    public void showThreadCircle(PGraphics pg, EarthquakeMarker marker) {        
        pg.pushStyle();
        
        pg.stroke(0);
        pg.strokeWeight(3);
        
        // show reticle
        ScreenPosition markerPos = map.getScreenPosition(marker.getLocation());
        line(markerPos.x, map.mapDisplay.offsetY-5,
                markerPos.x, map.mapDisplay.offsetY);
        line(map.mapDisplay.offsetX-5, markerPos.y,
                map.mapDisplay.offsetX, markerPos.y);
        line(markerPos.x, map.mapDisplay.getHeight()+map.mapDisplay.offsetY+5,
                markerPos.x, map.mapDisplay.getHeight()+map.mapDisplay.offsetY);
        line(map.mapDisplay.getWidth()+map.mapDisplay.offsetX+5, markerPos.y,
                map.mapDisplay.getWidth()+map.mapDisplay.offsetX, markerPos.y);
        

        pg.clip(map.mapDisplay.offsetX, map.mapDisplay.offsetY,
                map.mapDisplay.getWidth(), map.mapDisplay.getHeight());
        
        int grey = ((second()*1000+millis())/10) % 100-50;
        if (grey < 0)
            grey = -grey;
        
        pg.stroke(3*grey);

        
        ScreenPosition prev = null;
        for(float a = 0; a <= 360; a += 5) {
            Location hintPoint = GeoUtils.getDestinationLocation(marker.getLocation(), 
                                                                a, 
                                                                (float)marker.threatCircle());
            ScreenPosition pos = map.getScreenPosition(hintPoint);
            if (prev != null) {
                // Do not draw a line across the earth projection boundaries
                // (arbitrary 100px limit)
                if (Math.abs(prev.x-pos.x) < 100) {
                    pg.line(prev.x, prev.y, pos.x, pos.y);
                }
            }
            prev = pos;
        }
        
        ScreenPosition qpos = map.getScreenPosition(marker.getLocation());
        marker.draw(g, qpos.x, qpos.y);
        
        pg.noClip();
        pg.popStyle();
    }


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey(TreeMap<Double, POIMarker> threatened) 
	{	
	    fill(255,255,255);
	    rect(50,50,115,400);
		// Remember you can use Processing's graphics methods here
        textAlign(RIGHT);
	    for (int i = 0; i < EarthquakeMarker.colors.length; i++) {
            int color = EarthquakeMarker.colors[i];
            
            fill(color|0xFF000000);
            rect(70,70+20*i,40,20);
            text(Integer.toString(i), 120+12, 85+20*i);
        }
	    
        final int TRI_SIZE = 5;
        final int x = 70;
        int y = 310;
        fill(0xFF8F00FF);
        triangle(x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE, x, y-TRI_SIZE);
        textAlign(LEFT);
        text("City", 100, y+3);

        y += 20;
        fill(0xFFF00000);
        triangle(x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE, x, y-TRI_SIZE);
        text("Threat. " + ((threatCount > 0) ? Integer.toString(threatCount) : ""), 100, y+3);
        
        // fill(0xFF000000);
        for (Map.Entry<Double, POIMarker> entry : threatened.entrySet()) {
            y += 20;
            textAlign(RIGHT);
            text(String.format("%6.1f", entry.getKey()),100,y+3);
            textAlign(LEFT);
            text(String.format("%s", entry.getValue().getName()), 100+5, y+3);
        }
        
        Marker marker = lastClicked;
        if (marker == null)
            marker = lastSelected;
        
        fill(255,255,255);
        rect(50,460,115,125);
        if (marker != null) {
            Location location = marker.getLocation();
            String name = (String) marker.getProperty("title");
            String type = (String) marker.getProperty("type");
            
            if (name == null) {
                name = "";
            }
            if (type == null) {
                type = "";
            }
            
            float lon = location.getLon();
            float lat = location.getLat();
            
            fill(0);
            textAlign(LEFT);
            text(type, x, 475+0*17);
            text(name, x, 475+0*17+5, 80, 80);
            textAlign(RIGHT);
            text(floatToDMS(lon), x+80, 475+5*17);
            text(floatToDMS(lat), x+80, 475+6*17);
        }
	}
	
	private String floatToDMS(float v) {
	    char sign = (v < 0) ? '-' : '+';
	    
	    v = Math.abs(v);
	    int d = (int)v;
	    v = (v-d)*60;
	    int m = (int)v;
        v = (v-m)*60;
        
        return String.format("%c%d°%02d'%05.2f\"", sign,d,m,v);
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
    
    /** Event handler that gets called automatically when the 
     * mouse moves.
     */
    @Override
    public void mouseMoved()
    {
        // clear the last selection
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;
        
        }
        float x = mouseX;
        float y = mouseY;
        
        selectMarkerIfHover(quakeMarkers, x, y);
        selectMarkerIfHover(poiMarkers, x, y);
    }

    // If there is a marker under the cursor, and lastSelected is null 
    // set the lastSelected to be the first marker found under the cursor
    // Make sure you do not select two markers.
    // 
    private void selectMarkerIfHover(List<? extends Marker> markers, float x, float y)
    {
        if (lastSelected == null) {
            Marker marker = findMarker(markers, x, y);
            if (marker != null) {
                lastSelected = marker;
                lastSelected.setSelected(true);
            }
        }
    }

    private Marker findMarker(List<? extends Marker> markers, float x, float y)
    {
        for (Marker marker : markers) {
            if (!marker.isHidden() && marker.isInside(map, x, y)) {
                return marker;
            }
        }
        return null;
    }

    
    /** The event handler for mouse clicks
     * It will display an earthquake and its threat circle of cities
     * Or if a city is clicked, it will display all the earthquakes 
     * where the city is in the threat circle
     */
    @Override
    public void mouseClicked()
    {
        if (lastClicked != null) {
            lastClicked = null;
            unhideMarkers();
            return;
        }
        
        float x = mouseX;
        float y = mouseY;
        

        lastClicked = findMarker(quakeMarkers, x, y);
        if (lastClicked != null) {
            // Hide all other earthquake, and show only city in the thread circle
            EarthquakeMarker earthquake = (EarthquakeMarker) lastClicked;
            for (Marker m : quakeMarkers) {
                m.setHidden(m != earthquake);
            }
            for (Marker city : poiMarkers) {
                city.setHidden(city.getDistanceTo(earthquake.getLocation())> earthquake.threatCircle());
            }
        }
        else {
            lastClicked = findMarker(poiMarkers, x, y);
            if (lastClicked != null) {
                // Hide all other cities, and show all threatening earthquakes
                for(Marker city: poiMarkers) {
                    city.setHidden(city != lastClicked);
                }
                for(Marker m : quakeMarkers) {
                    EarthquakeMarker earthquake = (EarthquakeMarker)m;
                    earthquake.setHidden(lastClicked.getDistanceTo(earthquake.getLocation())> earthquake.threatCircle());

                    
                }
                
            }
        }
        
    }
    
    
    // loop over and unhide all markers
    private void unhideMarkers() {
        for(Marker marker : quakeMarkers) {
            marker.setHidden(false);
        }
            
        for(Marker marker : poiMarkers) {
            marker.setHidden(false);
        }
    }
}
