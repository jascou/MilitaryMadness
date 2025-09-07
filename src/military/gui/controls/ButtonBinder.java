package military.gui.controls;

import military.gui.GUIMiddleMan;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * Centralizes wiring of GUI buttons to both GameActions callbacks and the legacy
 * event dispatch mechanism (GUIMiddleMan). Helps keep GUI free of repetitive listener code.
 */
public final class ButtonBinder {
    private ButtonBinder() {}

    /**
     * Bind a JButton to an action and also enqueue a synthetic MouseEvent understood by the game loop.
     * @param button the Swing button to wire
     * @param onAction optional Runnable to invoke (may be null)
     * @param menuIndex legacy menu index for GUIMiddleMan (-1,x) encoding: 0=Shift,1=Attack,2=Info,3=End
     */
    public static void bind(JButton button, Runnable onAction, int menuIndex) {
        if (button == null) return;
        ActionListener al = evt -> {
            if (onAction != null) {
                try { onAction.run(); } catch (Exception ignored) { }
            }
            // Enqueue legacy event so the existing Game loop handles it consistently
            GUIMiddleMan.getInstance().putEvent(
                    new MouseEvent(button, 0, 0L, 0, -1, menuIndex, 1, false)
            );
        };
        button.addActionListener(al);
    }
}
