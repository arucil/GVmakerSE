/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eastsun.jgvm.platform;

import eastsun.jgvm.module.KeyModel;
import eastsun.jgvm.module.io.DefaultKeyMap;
import eastsun.jgvm.module.io.DefaultKeyModel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import static java.awt.event.KeyEvent.*;

/**
 *
 * @author Administrator
 */
public class KeyBoard extends JComponent {

    private static int[] rawKeyCodes = {
        VK_F1, VK_F2, VK_F3, VK_F4,
        VK_Q, VK_W, VK_E, VK_R, VK_T, VK_Y, VK_U, VK_I, VK_O, VK_P,
        VK_A, VK_S, VK_D, VK_F, VK_G, VK_H, VK_J, VK_K, VK_L, VK_ENTER,
        VK_Z, VK_X, VK_C, VK_V, VK_B, VK_N, VK_M, VK_PAGE_UP, VK_UP, VK_PAGE_DOWN,
        VK_CONTROL, VK_SHIFT, VK_CAPS_LOCK, VK_ESCAPE, VK_0, VK_PERIOD, VK_SPACE, VK_LEFT, VK_DOWN, VK_RIGHT,
    };
    private static char[] gvmKeyValues = {
        (char) 28, (char) 29, (char) 30, (char) 31,
        'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p',
        'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', (char) 13,
        'z', 'x', 'c', 'v', 'b', 'n', 'm', (char) 19, (char) 20, (char) 14,
        (char) 25, (char) 26, (char) 18, (char) 27, '0', '.', ' ', (char) 23, (char) 21, (char) 22
    };

    private BufferedImage image;
    private DefaultKeyModel keyModel;
    private boolean[] isKeyPressed;

    public KeyBoard() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/board.PNG"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        isKeyPressed = new boolean[rawKeyCodes.length];
        keyModel = new DefaultKeyModel(new SysInfoSE());
        keyModel.setKeyMap(new DefaultKeyMap(rawKeyCodes, gvmKeyValues));

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            switch (e.getID()) {
            case KeyEvent.KEY_PRESSED: keyPressed(e); break;
            case KeyEvent.KEY_RELEASED: keyReleased(e); break;
            }
            return false;
        });

        setPreferredSize(new Dimension(320, 110));
        // addKeyListener(new KeyLis());
        // setFocusable(true);
        // addMouseListener(new MouseLis());
        // addMouseMotionListener(new MouseLis());
    }

    public KeyModel getKeyModel() {
        return keyModel;
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    private void keyPressed(KeyEvent e) {
        int keyCode = translateNumKey(e.getKeyCode());
        int index = indexOfKeyCode(keyCode);
        if (index == -1 || isKeyPressed[index]) {
            return;
        }
        isKeyPressed[index] = true;
        keyModel.keyPressed(keyCode);
    }

    private void keyReleased(KeyEvent e) {
        int keyCode = translateNumKey(e.getKeyCode());
        int index = indexOfKeyCode(keyCode);
        if (index >= 0) {
            isKeyPressed[index] = false;
            keyModel.keyReleased(keyCode);
        }
    }

    private static int translateNumKey(int rawKeyCode) {
        switch (rawKeyCode) {
        case KeyEvent.VK_1: case KeyEvent.VK_NUMPAD1: rawKeyCode = KeyEvent.VK_B; break;
        case KeyEvent.VK_2: case KeyEvent.VK_NUMPAD2: rawKeyCode = KeyEvent.VK_N; break;
        case KeyEvent.VK_3: case KeyEvent.VK_NUMPAD3: rawKeyCode = KeyEvent.VK_M; break;
        case KeyEvent.VK_4: case KeyEvent.VK_NUMPAD4: rawKeyCode = KeyEvent.VK_G; break;
        case KeyEvent.VK_5: case KeyEvent.VK_NUMPAD5: rawKeyCode = KeyEvent.VK_H; break;
        case KeyEvent.VK_6: case KeyEvent.VK_NUMPAD6: rawKeyCode = KeyEvent.VK_J; break;
        case KeyEvent.VK_7: case KeyEvent.VK_NUMPAD7: rawKeyCode = KeyEvent.VK_T; break;
        case KeyEvent.VK_8: case KeyEvent.VK_NUMPAD8: rawKeyCode = KeyEvent.VK_Y; break;
        case KeyEvent.VK_9: case KeyEvent.VK_NUMPAD9: rawKeyCode = KeyEvent.VK_U; break;
        }
        return rawKeyCode;
    }

    private int indexOfKeyCode(int code) {
        int index = -1;
        for (int i = 0; i < rawKeyCodes.length; i++) {
            if (rawKeyCodes[i] == code) {
                index = i;
                break;
            }
        }
        return index;
    }

    private class MouseLis extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }
}
