package demos;

import processing.core.PApplet;

public class MoveWindow extends PApplet {
    private static final long serialVersionUID = 1L;
    
    @Override
    public void setup() {
        size(100,100,P2D);
        background(255);            //set canvas color
    }

    @Override
    public void draw() {
        if (frameCount > 1) {
            frame.setLocation(300, 200);
        }
    }
}
