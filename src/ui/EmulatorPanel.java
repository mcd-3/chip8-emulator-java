package ui;

import chip.Chip;

import javax.swing.*;
import java.awt.*;

public class EmulatorPanel extends JPanel {

    private Chip chip;

    public  EmulatorPanel(Chip chip) {
        this.chip = chip;
    }

    public Chip getChip() {
        return this.chip;
    }

    public void paint(Graphics g) {
        byte[] display = chip.getDisplay();
        for(int i = 0; i < display.length; i++) {
            if(display[i] == 0)
                g.setColor(Color.BLACK);
            else
                g.setColor(Color.WHITE);

            int x = (i % 64);
            int y = (int)Math.floor(i / 64);

            g.fillRect(x * 10, y * 10, 10, 10);
        }
    }

}
