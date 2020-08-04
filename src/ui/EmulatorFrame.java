package ui;

import chip.Chip;
import listeners.KeyPressListener;

import javax.swing.*;
import java.awt.*;

public class EmulatorFrame extends JFrame {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 320;

    private EmulatorPanel panel;
    private KeyPressListener listener;

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
        listener = new KeyPressListener(new int[256], new byte[16]);
        addKeyListener(listener);
        pack();
        setVisible(true);

    }

    public byte[] getKeyBuffer() {
        return listener.getKeyBuffer();
    }

}
