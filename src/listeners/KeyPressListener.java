package listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class KeyPressListener implements KeyListener {

    private int[] keyCodes;
    private byte[] keyBuffer;

    public KeyPressListener(int[] keyCodes, byte[] keyBuffer) {
        this.keyCodes = keyCodes;
        this.keyBuffer = keyBuffer;
        fillKeyCodes();
    }

    public int[] getKeyCodes() {
        return keyCodes;
    }

    public byte[] getKeyBuffer() {
        return keyBuffer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(keyCodes[e.getKeyCode()] != -1) {
            keyBuffer[keyCodes[e.getKeyCode()]] = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(keyCodes[e.getKeyCode()] != -1) {
            keyBuffer[keyCodes[e.getKeyCode()]] = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void fillKeyCodes() {
        Arrays.fill(keyCodes, -1);
        keyCodes['1'] = 1;
        keyCodes['2'] = 2;
        keyCodes['3'] = 3;
        keyCodes['Q'] = 4;
        keyCodes['W'] = 5;
        keyCodes['E'] = 6;
        keyCodes['A'] = 7;
        keyCodes['S'] = 8;
        keyCodes['D'] = 9;
        keyCodes['Z'] = 0xA;
        keyCodes['X'] = 0;
        keyCodes['C'] = 0xB;
        keyCodes['4'] = 0xC;
        keyCodes['R'] = 0xD;
        keyCodes['F'] = 0xE;
        keyCodes['V'] = 0xF;
    }
}
