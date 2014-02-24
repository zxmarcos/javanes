/**
 * Created by marcos on 13/02/14.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.MemoryImageSource;
import java.util.Random;
import java.io.File;

public class NesGui extends JFrame {

    public static final int JOYSTICK_PLAYER1 = 0;
    public static final int JOYSTICK_PLAYER2 = 8;
    public static final int JOYSTICK_A = 0;
    public static final int JOYSTICK_B = 1;
    public static final int JOYSTICK_SELECT = 2;
    public static final int JOYSTICK_START = 3;
    public static final int JOYSTICK_UP = 4;
    public static final int JOYSTICK_DOWN = 5;
    public static final int JOYSTICK_LEFT = 6;
    public static final int JOYSTICK_RIGHT = 7;
    private static boolean[] keyboardState = new boolean[8 * 2];
    private static String lastPath = "";
    private final JFrame window = this;
    private NesEmulator emulator = null;
    private Canvas screen = null;
    private Thread manager = null;

    public NesGui() {
        emulator = new NesEmulator();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(new KeyboardManager());
        setFocusable(true);

        setTitle("Emulador de NES marocto");
        createMenus();

        screen = new Canvas();
        getContentPane().add(BorderLayout.CENTER, screen);
        pack();
        setResizable(false);

       // emulator.load("zelda/Zelda.NES");

        /* A thread responsável por cuidar da emulação */
        manager = new Thread(new EmulationManager());
        manager.start();
    }

    public static boolean getKeyState(int key) {
        return keyboardState[key];
    }

    /* Cria os menus do emulador */
    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu emulatorMenu = new JMenu("NES");
        JMenuItem loadMenuItem = new JMenuItem("Carregar");
        JMenuItem exitMenuItem = new JMenuItem("Sair");

        JMenu miscMenu = new JMenu("MISC");
        JMenuItem dumpVramMenuItem = new JMenuItem("Dump");
        JMenuItem dumpRegMenuItem = new JMenuItem("Registers");
/*
        dumpVramMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emulator.getPPU().saveVRAM();
                emulator.getBus().saveRoms();

            }
        });
        dumpRegMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emulator.getCpu().showRegisters();
            }
        });

*/
        /* Implementa a ação de carregar uma ROM */
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser choose = new JFileChooser();
                choose.setCurrentDirectory(new File(lastPath));
                int ret = choose.showOpenDialog(window);
                lastPath = choose.getSelectedFile().getPath();
                if (ret == JFileChooser.APPROVE_OPTION) {
                    emulator.load(choose.getSelectedFile().getAbsolutePath());
                    if (!manager.isAlive()) {
                        manager = new Thread(new EmulationManager());
                        manager.start();
                    }
                }
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.dispose();
            }
        });

        emulatorMenu.add(loadMenuItem);
        emulatorMenu.addSeparator();
        emulatorMenu.add(exitMenuItem);
/*
        miscMenu.add(dumpVramMenuItem);
        miscMenu.add(dumpRegMenuItem);
*/
        menuBar.add(emulatorMenu);
        menuBar.add(miscMenu);

        setJMenuBar(menuBar);
    }

    /* Thread responsável por gerenciar a emulação */
    private class EmulationManager implements Runnable {
        public EmulationManager() {
        }


        public void run() {
            double begin = 0;
            double end = 0;
            double last = 0;
            final double ratio = 1000 / 60.0;
            double delay = 0;

            try {
                while (true) {
                    begin = (double) System.currentTimeMillis();
                    emulator.doFrame();
                    screen.repaint();
                    end = (double) System.currentTimeMillis();

                    delay = ratio - (end - begin) - last;
                    if (delay > 0) {
                        Thread.sleep((long) delay);
                        last = 0;
                    } else {
                        last = delay;
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Interrompido! parando emulação.");
            }
        }
    }

    private class Canvas extends JComponent {
        private Random rand;
        private Font monoFont = new Font("Monospaced", Font.BOLD, 14);
        private FontMetrics fm;
        private final int scale = 1;


        public Canvas() {
        }

        @Override
        public void paint(Graphics g) {
            NesPPU ppu = emulator.getPPU();
            if (ppu != null) {
                Image img = createImage(new MemoryImageSource(NesPPU.WIDTH, NesPPU.HEIGHT, ppu.getFrameBuffer(), 0, NesPPU.WIDTH));
                g.drawImage(img, 0, 0, NesPPU.WIDTH * scale, NesPPU.HEIGHT * scale, null);
            } else {
                g.setColor(Color.black);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.white);
                g.setFont(monoFont);
                fm = g.getFontMetrics();
                int w = fm.stringWidth("NENHUMA ROM CARREGADA");
                int h = fm.getAscent();
                g.drawString("NENHUMA ROM CARREGADA", 0, (NesPPU.HEIGHT * scale) - h);
            }

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(256 * scale, 240 * scale);
        }
    }

    private class KeyboardManager extends KeyAdapter {

        public KeyboardManager() {
            super();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_UP] = true;
                    break;
                case KeyEvent.VK_DOWN:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_DOWN] = true;
                    break;
                case KeyEvent.VK_LEFT:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_LEFT] = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_RIGHT] = true;
                    break;
                case KeyEvent.VK_A:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_A] = true;
                    break;
                case KeyEvent.VK_S:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_B] = true;
                    break;
                case KeyEvent.VK_SPACE:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_SELECT] = true;
                    break;
                case KeyEvent.VK_ENTER:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_START] = true;
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_UP] = false;
                    break;
                case KeyEvent.VK_DOWN:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_DOWN] = false;
                    break;
                case KeyEvent.VK_LEFT:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_LEFT] = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_RIGHT] = false;
                    break;
                case KeyEvent.VK_A:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_A] = false;
                    break;
                case KeyEvent.VK_S:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_B] = false;
                    break;
                case KeyEvent.VK_SPACE:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_SELECT] = false;
                    break;
                case KeyEvent.VK_ENTER:
                    keyboardState[JOYSTICK_PLAYER1 + JOYSTICK_START] = false;
                    break;
            }
        }
    }

}
