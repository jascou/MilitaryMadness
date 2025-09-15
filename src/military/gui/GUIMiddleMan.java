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
    private volatile boolean inputsEnabled = true;
    
    public static GUIMiddleMan getInstance(){
        if(instance == null){
            instance = new GUIMiddleMan();
        }
        return instance;
    }
    
    private GUIMiddleMan() {
        ready = false;
    }

    public void setInputsEnabled(boolean enabled) {
        this.inputsEnabled = enabled;
    }

    public synchronized InputEvent getEvent() {         //used by consumer: engine
        if (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ready = false;
        notify();
        return event;
    }

    public synchronized void putEvent(InputEvent event) { //used by producer: GUI
        if (!inputsEnabled) {
            return; // ignore user input while disabled (e.g., during AI turn)
        }
        if (ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            ex.printStackTrace();
        }
    }

}
