package military.gui;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * Verifies that ButtonBinder enqueues a legacy MouseEvent via GUIMiddleMan when a button is clicked.
 */
public class ButtonBinderTest {

    @Test
    public void bindEnqueuesLegacyMouseEvent() {
        JButton b = new JButton("Shift");
        // Ensure no event yet
        // Bind with menuIndex 0 (Shift)
        military.gui.controls.ButtonBinder.bind(b, null, 0);
        // Simulate a click by firing actionPerformed
        for (ActionListener al : b.getActionListeners()) {
            al.actionPerformed(new ActionEvent(b, ActionEvent.ACTION_PERFORMED, "click"));
        }
        java.awt.event.InputEvent evt = GUIMiddleMan.getInstance().getEvent();
        Assert.assertTrue("Should be a MouseEvent", evt instanceof MouseEvent);
        MouseEvent me = (MouseEvent) evt;
        Assert.assertEquals("menuIndex y should be 0 for Shift", 0, me.getY());
        Assert.assertEquals("x should be -1 per legacy encoding", -1, me.getX());
    }
}
