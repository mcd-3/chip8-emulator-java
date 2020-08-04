package main;

import chip.Chip;
import ui.EmulatorFrame;
import ui.EmulatorWindow;

public class Main extends Thread {

    private Chip chip;
    private EmulatorFrame frame;

    public Main() {
        this.chip = new Chip();
        chip.loadRom("./roms/pong2.c8");
        this.frame = new EmulatorFrame(this.chip);
    }

    public void run() {
        while (true) {
            chip.setKeypad(frame.getKeyBuffer());
            chip.run();
            if (chip.getRedrawFlag()) {
                frame.repaint();
                chip.setRedrawFlag(false);
            }
            try {
                Thread.sleep(2);//(long) Chip.RUN_SPEED);
            } catch (InterruptedException e) { }
        }
    }

//    public static void main(String[] args) {
//        Main main = new Main();
//        main.start();
//    }
}
