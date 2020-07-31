package ui;

import chip.Chip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EmulatorFrame extends JFrame implements KeyListener {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 320;

    private EmulatorPanel panel;
    private byte[] keyBuffer;
    private int[] keysCodes;

    public EmulatorFrame(Chip chip) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        pack();
        setPreferredSize(
                new Dimension(
                        WIDTH + getInsets().left + getInsets().right,
                        HEIGHT + getInsets().top + getInsets().bottom
                )
        );
        panel = new EmulatorPanel(chip);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("CHIP-8 Emulator for Java");
        add(panel, BorderLayout.CENTER);
        addKeyListener(this);
        pack();
        setVisible(true);

        keysCodes = new int[256];
        keyBuffer = new byte[16];
        fillKeyCodes();
    }

    private void fillKeyCodes() {
        for(int i = 0; i < keysCodes.length; i++) {
            keysCodes[i] = -1;
        }
        keysCodes['1'] = 1;
        keysCodes['2'] = 2;
        keysCodes['3'] = 3;
        keysCodes['Q'] = 4;
        keysCodes['W'] = 5;
        keysCodes['E'] = 6;
        keysCodes['A'] = 7;
        keysCodes['S'] = 8;
        keysCodes['D'] = 9;
        keysCodes['Z'] = 0xA;
        keysCodes['X'] = 0;
        keysCodes['C'] = 0xB;
        keysCodes['4'] = 0xC;
        keysCodes['R'] = 0xD;
        keysCodes['F'] = 0xE;
        keysCodes['V'] = 0xF;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(keysCodes[e.getKeyCode()] != -1) {
            keyBuffer[keysCodes[e.getKeyCode()]] = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(keysCodes[e.getKeyCode()] != -1) {
            keyBuffer[keysCodes[e.getKeyCode()]] = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public byte[] getKeyBuffer() {
        return keyBuffer;
    }

}
