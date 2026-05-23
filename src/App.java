import javax.swing.SwingUtilities;

import minewalker.MinewalkerApp;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MinewalkerApp().start());
    }
}
