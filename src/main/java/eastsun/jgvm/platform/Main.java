package eastsun.jgvm.platform;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Exception e = Config.getConfigLoadingException();
        if (e != null) {
            EventQueue.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "配置文件加载失败：\n" + e, MainFrame.TITLE, JOptionPane.ERROR_MESSAGE));
        } else {
            EventQueue.invokeLater(() -> {
                JFrame frame = new MainFrame();
                frame.setVisible(true);
            });
        }
    }
}
