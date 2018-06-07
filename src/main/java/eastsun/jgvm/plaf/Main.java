package eastsun.jgvm.plaf;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
