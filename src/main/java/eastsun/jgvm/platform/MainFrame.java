package eastsun.jgvm.platform;

import eastsun.jgvm.module.GvmConfig;
import eastsun.jgvm.module.JGVM;
import eastsun.jgvm.module.LavApp;
import eastsun.jgvm.module.ScreenModel;
import eastsun.jgvm.module.io.DefaultFileModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @version Aug 13, 2008
 * @author Eastsun
 */
public class MainFrame extends JFrame {
    public static final String TITLE = "GVmakerSE";

    private JGVM gvm;
    private LavApp lavApp;

    private VMThread vmThread;

    private final JFileChooser fileChooser;
    private final JLabel msgLabel;
    private final ScreenPane screenPane;
    private JMenuItem menuItemLoad, menuItemRun, menuItemStop, menuMemEdit;

    private Status status;

    private static final int steps = Config.getSteps();
    private static final int delay = Config.getDelay();
    private static volatile boolean isDelayEnabled = Config.isDelayEnabled();


    public MainFrame() {
        super(TITLE);

        KeyBoard keyBoard = new KeyBoard();

        ScreenModel screenModel = ScreenModel.newScreenModel();

        gvm = JGVM.newGVM(new GvmConfig(), new DefaultFileModel(new FileSysSE("GVM_ROOT")), screenModel, keyBoard.getKeyModel());

        screenPane = new ScreenPane(screenModel);

        fileChooser = new JFileChooser("GVM_ROOT");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GVmaker Application", ".lav"));

        msgLabel = new JLabel("结束");
        msgLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 0));

        addWindowListener(new WindowClosingListener());

        add(screenPane, BorderLayout.NORTH);
        add(msgLabel, BorderLayout.CENTER);
        add(keyBoard, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        pack();

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void updateStatus(Status status) {
        this.status = status;
        switch (status) {
        case INITIAL:
            menuItemLoad.setEnabled(true);
            menuItemRun.setEnabled(false);
            menuItemStop.setEnabled(false);
            menuItemRun.setText("运行");
            msgLabel.setText("准备就绪");
            break;
        case LOADED:
            menuItemLoad.setEnabled(true);
            menuItemRun.setEnabled(true);
            menuItemStop.setEnabled(false);
            menuItemRun.setText("运行");
            msgLabel.setText("已加载 [" + lavApp.getName() + "]");
            break;
        case RUNNING:
            menuItemLoad.setEnabled(false);
            menuItemRun.setEnabled(true);
            menuItemStop.setEnabled(true);
            menuItemRun.setText("暂停");
            msgLabel.setText("正在运行");
            break;
        case PAUSED:
            menuItemLoad.setEnabled(false);
            menuItemRun.setEnabled(true);
            menuItemStop.setEnabled(true);
            menuItemRun.setText("继续");
            msgLabel.setText("已暂停");
            break;
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = menuBar.add(new JMenu("文件"));

        menuItemLoad = menuFile.add(new JMenuItem("打开"));
        menuItemLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItemLoad.addActionListener(e -> openLavFile());

        menuFile.addSeparator();

        menuItemRun = menuFile.add(new JMenuItem("运行"));
        menuItemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        menuItemRun.addActionListener(e -> {
            Status newStatus = null;
            switch (status) {
            case LOADED:
                start();
                newStatus = Status.RUNNING;
                break;
            case RUNNING:
                pause();
                newStatus = Status.PAUSED;
                break;
            case PAUSED:
                resume();
                newStatus = Status.RUNNING;
                break;
            }
            updateStatus(newStatus);
        });

        menuItemStop = menuFile.add(new JMenuItem("停止"));
        menuItemStop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        menuItemStop.addActionListener(e -> {
            stop();
            // updateStatus(Status.LOADED); // VMThread结束时会更新status
        });

        menuFile.addSeparator();

        JMenuItem menuItemExit = menuFile.add(new JMenuItem("退出"));
        menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        menuItemExit.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        JMenu menuSet = menuBar.add(new JMenu("设置"));

        JCheckBoxMenuItem menuEnableDelay = new JCheckBoxMenuItem("减速运行");
        menuSet.add(menuEnableDelay);

        menuEnableDelay.addItemListener(e -> isDelayEnabled = menuEnableDelay.isSelected());
        menuEnableDelay.setSelected(isDelayEnabled);

        return menuBar;
    }

    private void openLavFile() {
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                lavApp = LavApp.createLavApp(file.getName(), in);
                updateStatus(Status.LOADED);
            } catch (IOException e) {
                e.printStackTrace();

                JOptionPane.showMessageDialog(this, "文件加载失败:" + e.getMessage(), "GVmakerSE", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void start() {
        gvm.loadApp(lavApp);

        vmThread = new VMThread();
        vmThread.start();
        screenPane.startRendering();
    }

    private void resume() {
        screenPane.startRendering();
        vmThread.setPaused(false);
    }

    private void pause() {
        vmThread.setPaused(true);
        screenPane.stopRendering();
    }

    private void stop() {
        screenPane.stopRendering();

        if (vmThread != null && vmThread.isAlive()) {
            vmThread.interrupt();
            try {
                vmThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class VMThread extends Thread {

        private boolean isPaused;

        VMThread() {
            setPaused(false);
        }

        public void run() {
            try {
                int step = 0;
                while (!(gvm.isEnd() || isInterrupted())) {
                    while (isPaused()) {
                        synchronized (this) {
                            wait();
                        }
                    }
                    gvm.nextStep();
                    if (++step >= steps && isDelayEnabled) {
                        step = 0;
                        Thread.sleep(0, delay);
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                e.printStackTrace();

                JOptionPane.showMessageDialog(MainFrame.this, "运行错误:" + e, TITLE, JOptionPane.ERROR_MESSAGE);
            } finally {
                gvm.dispose();
                EventQueue.invokeLater(() -> updateStatus(Status.LOADED));
            }
        }

        synchronized boolean isPaused() {
            return isPaused;
        }

        synchronized void setPaused(boolean p) {
            if (p != isPaused) {
                isPaused = p;
                notifyAll();
            }
        }
    }

    private class WindowClosingListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            stop();
        }
    }

    private enum Status {
        INITIAL, LOADED, RUNNING, PAUSED
    }
}
