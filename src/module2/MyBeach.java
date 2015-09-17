package module2;

import processing.core.PApplet;
import processing.core.PImage;

public class MyBeach extends PApplet {
    private static final long serialVersionUID = -8359165553589576955L;

    private final String backgroundImageURL
                = "http://www.livescience.com/images/i/000/054/836/i02/beach-sea-130716.jpg";
    private PImage  backgroundImage;
    
    @Override
    public void setup() {
        backgroundImage = loadImage(backgroundImageURL);
        size(backgroundImage.width, backgroundImage.height, OPENGL);
    }

    @Override
    public void draw() {
        background(200,200,220);
        image(backgroundImage, 0, 0);
        
        fill(255,209,0);
        ellipse(width/6, height/8, 50, 50);
    }
    
}
