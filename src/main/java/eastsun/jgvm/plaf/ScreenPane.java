package eastsun.jgvm.plaf;

import eastsun.jgvm.module.ScreenModel;
import eastsun.jgvm.module.event.Area;
import eastsun.jgvm.module.ram.ScreenRam;

import java.awt.*;
import java.awt.image.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 * @version Aug 13, 2008
 * @author Eastsun
 */
public class ScreenPane extends JPanel {
    private final BufferedImage bufImg;

    private final Timer renderTimer = new Timer();
    private TimerTask renderTimerTask;

    private static final Color BG_COLOR = new Color(0xa5a3a7);
    private static final Color FG_COLOR = new Color(0x242529);
    private static final int PIXEL_SCALE = 2;

    public ScreenPane(ScreenModel screenModel) {
        int screenWidth = screenModel.getWidth();
        int screenHeight = screenModel.getHeight();

        ColorModel colorModel = new IndexColorModel(1, 2,
                new int[] { BG_COLOR.getRGB(), FG_COLOR.getRGB() },
                0, false, -1, DataBuffer.TYPE_BYTE);
        WritableRaster raster = WritableRaster.createPackedRaster(
                new DataBufferByte(((ScreenRam) screenModel.getGraphRam()).getInternalData(), screenWidth * screenHeight / 8),
                screenWidth, screenHeight, 1, new Point(0, 0));
        bufImg = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        setPreferredSize(new Dimension(screenWidth * PIXEL_SCALE, screenHeight * PIXEL_SCALE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(bufImg, 0, 0, getWidth(), getHeight(), null);
    }

    private void renderScreen() {
        repaint();
    }

    public void startRendering() {
        final int delay = 1000 / 60; // 60fps
        renderTimer.scheduleAtFixedRate(renderTimerTask = new RenderTimerTask(), delay, delay);
    }

    public void stopRendering() {
        if (renderTimerTask != null) {
            renderTimerTask.cancel();
        }
    }

    private class RenderTimerTask extends TimerTask {
        @Override
        public void run() {
            EventQueue.invokeLater(ScreenPane.this::renderScreen);
        }
    }
}
