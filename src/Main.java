import javax.swing.SwingUtilities;
import ui.LoginFrame;

// main entry point to launch the library app
public class Main {
    public static void main(String[] args) {
        // start gui on event dispatch thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}