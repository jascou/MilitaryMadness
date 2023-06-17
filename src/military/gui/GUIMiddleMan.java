/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.gui;

import java.awt.event.InputEvent;

/**
 *
 * @author Nate
 */
public class GUIMiddleMan {
    private InputEvent event;
    private boolean ready;
    private static GUIMiddleMan instance;
    
    public static GUIMiddleMan getInstance(){
        if(instance == null){
            instance = new GUIMiddleMan();
        }
        return instance;
    }
    
    private GUIMiddleMan() {
        ready = false;
    }

    public synchronized InputEvent getEvent() {         //used by consumer: engine
        if (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        ready = false;
        notify();
        return event;
    }

    public synchronized void putEvent(InputEvent event) {//used by producer: GUI
        if (ready) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.event = event;
        ready = true;
        notify();
    }

    public void sleep(GUI gui) {
        try {
            Thread.sleep(20);
        } catch (InterruptedException ex) {
        }
    }

}
