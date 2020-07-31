package chip;

import util.NibbleUtil;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import javax.sound.midi.*;

public class Chip {
    public static final double RUN_SPEED = 16.66;
    private static final int MEMORY_BYTES = 4096;
    private static final int V_REGISTERS = 16;
    private static final int STACK_LENGTH = 16;
    private static final int KEYPAD_KEYS = 16;
    private static final int PIXELS_PER_BYTE = 8;
    private static final int DISPLAY_WIDTH = 64;
    private static final int DISPLAY_HEIGHT = 32;
    private static final int DISPLAY_RESOLUTION = DISPLAY_WIDTH * DISPLAY_HEIGHT;
    private static final char MEMORY_ROM_START = 0x200;
    private static final char FONT_MEMORY_START = 0x0050;
    private static final char V_FLAG = 0xF;
    private static final int NOTE_SOUND = 50;
    private static final int[] FONT_SET = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    // RAM used by the interpreter
    // Should be 4kb in size
    private char[] memory;

    // General use registers
    // Goes from V0 to VF
    private char[] V;

    // Special register to hold memory addresses
    private char I;

    // Delay timer. Subtracts by 1 every 60Hz, then deactivates
    private int dt;

    // Sound timer. Subtracts by 1 every 60Hz, then deactivates
    // When > 0, a sound will play
    private int st;

    // Program counter. Stores currently executing address
    private char pc;

    // Stores the address that the interpreter returns to when finished with a subroutine
    private char[] stack;

    // Stack pointer. Points to the top of the stack
    private int sp;

    // Keypad used to control the program
    private byte[] keypad;

    // Monochrome display. Can contain 15 or 5 byte sprites
    private byte[] display;

    // Flag to see if the screen needs to be redrawn
    private boolean redrawFlag;

    // Midi synthesizer
    private Synthesizer synthesizer;

    // Midi channel to play sounds
    private MidiChannel midiChannel;

    public Chip() {
        memory = new char[MEMORY_BYTES];
        V = new char[V_REGISTERS];
        I = 0x0; // Set to null

        dt = 0;
        st = 0;

        stack = new char[STACK_LENGTH];
        sp = 0;
        pc = 0x200;

        keypad = new byte[KEYPAD_KEYS];
        display = new byte[DISPLAY_RESOLUTION];
        redrawFlag = false;

        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            System.err.println("MIDI: No midi channels to play sounds on");
        }

        loadFontset();
    }

    /**
     * Returns the display data for the chip
     * @return byte[]
     */
    public byte[] getDisplay() {
        return this.display;
    }

    /**
     * Returns whether or not the screen needs to be redrawn
     * @return boolean
     */
    public boolean getRedrawFlag() {
        return redrawFlag;
    }

    /**
     * Sets the redraw flag
     * @param flag boolean
     */
    public void setRedrawFlag(boolean flag) {
        this.redrawFlag = flag;
    }

    /**
     * Sets the keys for the keypad
     * @param keyBuffer
     */
    public void setKeypad(byte[] keyBuffer) {
        for(int i = 0; i < keypad.length; i++) {
            keypad[i] = keyBuffer[i];
        }
    }

    /**
     * Loads a chip-8 ROM into memory
     * @param rom file location of the ROM
     * @return boolean True if success, false if not
     */
    public boolean loadRom(String rom) {
        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new FileInputStream(rom));
            for (int i = 0; stream.available() > 0; i++) {
                memory[MEMORY_ROM_START + i] = (char) (stream.readByte() & 0xFF);
            }
            stream.close();
            return true;
        } catch (IOException e) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e2) {
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * Loads the fontset into memory
     */
    public void loadFontset() {
        for (int i = 0; i < FONT_SET.length; i++) {
            memory[FONT_MEMORY_START + i] = (char) (FONT_SET[i] & 0xFF);
        }
    }

    /**
     * Decreases delay and sound timers by 1
     */
    private void decreaseTimers() {
        if (dt > 0) dt--;
        if (st > 0) st--;
    }

    /**
     * Emulates one cycle for the CHIP-8
     * Gets the opcode and performs the correct operation
     * Also decreases timers
     */
    public void run() {
        char opcode = (char)((memory[pc] << 8) | memory[pc + 1]);
        char nibble = NibbleUtil.getStartNibble(opcode);
        int x = NibbleUtil.getX(opcode);
        int y = NibbleUtil.getY(opcode);
        int kk = NibbleUtil.getKK(opcode);
        int nnn = NibbleUtil.getNNN(opcode);

        switch (nibble) {
            case 0x0000: // 0nnn, 00E0, 00EE
                switch (NibbleUtil.getEndNibble(opcode, true)) {
                    case 0x00E0:  { // clear display
                        Arrays.fill(display, (byte) 0);
                        nextInstruction();
                        setRedrawFlag(true);
                        break;
                    }
                    case 0x00EE:
                        sp--;
                        pc = (char) (stack[sp] + 2);
                        break;
                    default:
                        break;
                }
                break;
            case 0x1000: // 1nnn
                pc = (char) nnn;
                break;
            case 0x2000: // 2nnn
                stack[sp] = pc;
                sp++;
                pc = (char) nnn;
                break;
            case 0x3000: // 3xkk
                if (V[x] == kk) {
                    skipInstruction();
                } else {
                    nextInstruction();
                }
                break;
            case 0x4000: // 4xkk
                if (V[x] != kk) {
                    skipInstruction();
                } else {
                    nextInstruction();
                }
                break;
            case 0x5000: // 5xy0
                if (V[x] == V[y]) {
                    skipInstruction();
                } else {
                    nextInstruction();
                }
                break;
            case 0x6000: // 6xkk
                // Vx = kk
                V[x] = (char) kk;
                nextInstruction();
                break;
            case 0x7000: // 7xkk
                // Vx = v[x] + kk
                V[x] = (char) ((V[x] + kk) & 0xFF);
                nextInstruction();
                break;
            case 0x8000: // 8xy0, 8xy1, 8xy2, 8xy3, 8xy4, 8xy5, 8xy6, 8xy7, 8xyE
                switch (NibbleUtil.getEndNibble(opcode)) {
                    case 0x0000:
                        V[x] = V[y];
                        nextInstruction();
                        break;
                    case 0x0001:
                        V[x] = (char)(V[x] | V[y]);
                        nextInstruction();
                        break;
                    case 0x0002: // 8xy2
                        V[x] = (char)(V[x] & V[y]);
                        nextInstruction();
                        break;
                    case 0x0003:
                        V[x] = (char)(V[x] ^ V[y]);
                        nextInstruction();
                        break;
                    case 0x0004: // 8xy4
                        V[x] = (char)(V[x] + V[y]);
                        if (V[x] > 255) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        nextInstruction();
                        break;
                    case 0x0005:
                        if (V[x] > V[y]) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] = (char) (V[x] - V[y]);
                        nextInstruction();
                        break;
                    case 0x0006:
                        V[V_FLAG] = (char) (V[x] & 0x1);
                        V[x] >>= 1; // divide by 2 by shifting bit to right
                        nextInstruction();
                        break;
                    case 0x0007:
                        if (V[y] > V[x]) {
                            V[V_FLAG] = 1;
                        } else {
                            V[V_FLAG] = 0;
                        }
                        V[x] = (char) (V[y] - V[x]);
                        nextInstruction();
                        break;
                    case 0x000E:
                        V[V_FLAG] = (char) (V[x] & 0x80);
                        V[x] <<= 1; // multiply by 2 by shifting bit to left
                        nextInstruction();
                        break;
                    default:
                        System.out.println("Unsupported Opcode: " + opcode);
                        System.exit(-1);
                        break;
                }
                break;
            case 0x9000: // 9xy0
                if (V[x] != V[y]) {
                    skipInstruction();
                } else {
                    nextInstruction();
                }
                break;
            case 0xA000: // Annn
                I = (char) nnn;
                nextInstruction();
                break;
            case 0xB000: // Bnnn
                pc = (char) (nnn + V[0]);
                break;
            case 0xC000: // Cxkk
                int randomNumber = new Random().nextInt(256) & kk;
                V[x] = (char) randomNumber;
                nextInstruction();
                break;
            case 0xD000:  { // Dxyn
                int cX = V[x]; // coordinate X
                int cY = V[y]; // coordinate Y
                int height = opcode & 0x000F; // pixel size: always 8 by X (last 4 bytes will tell us x)
                V[V_FLAG] = 0;

                // Draw a sprite on the screen at (Vx, Vy) and set Vf to true if collision occurs
                for (int i = 0; i < height; i++) { // Draw a pixel (8, i) using i
                    int row = memory[this.I + i];
                    for (int j = 0; j < PIXELS_PER_BYTE; j++) { // Draw a pixel (8, i) using 8 (j in this case)
                        int pixel = row & (0x80 >> j);
                        if (pixel != 0) {
                            int totalX = (cX + j) % DISPLAY_WIDTH;
                            int totalY = (cY + i) % DISPLAY_HEIGHT;
                            int index = (totalY * DISPLAY_WIDTH) + totalX;

                            if (display[index] == 1) { // XOR: check if it's active
                                V[V_FLAG] = 1;
                            }

                            display[index] ^= 1;
                        }
                    }
                }

                nextInstruction();
                setRedrawFlag(true);
                break;
            }
            case 0xE000: // Ex9E, ExA1
                switch (NibbleUtil.getEndNibble(opcode)) {
                    case 0x0001 -> {
                        int isNotPressed = 0;
                        if (keypad[V[x]] == isNotPressed) {
                            skipInstruction();
                        } else {
                            nextInstruction();
                        }
                    }
                    case 0x000E -> {
                        int isPressed = 1;
                        if (keypad[V[x]] == isPressed) {
                            skipInstruction();
                        } else {
                            nextInstruction();
                        }
                    }
                    default -> System.err.println("Unsupported Opcode: " + opcode);
                }
                break;
            case 0xF000: //  Fx07, Fx0A, Fx15, Fx18, Fx1E, Fx29, Fx33, Fx55, Fx65
                switch (NibbleUtil.getEndNibble(opcode, true)) {
                    case 0x0007 -> {
                        V[x] = (char) dt;
                        nextInstruction();
                    }
                    case 0x000A -> {
                        int isPressed = 1;
                        for (int i = 0; i < keypad.length; i++) {
                            if (keypad[i] == isPressed) {
                                V[x] = (char) i;
                                nextInstruction();
                                break;
                            }
                        }
                    }
                    case 0x0015 -> {
                        dt = V[x];
                        nextInstruction();
                    }
                    case 0x0018 -> {
                        st = V[x];
                        nextInstruction();
                    }
                    case 0x001E -> {
                        I = (char) (I + V[x]);
                        nextInstruction();
                    }
                    case 0x0029 -> {
                        I = (char) (FONT_MEMORY_START + (V[x] * 5));
                        nextInstruction();
                    }
                    case 0x0033 -> {
                        int tempVx = V[x];
                        int hundreds = (tempVx - (tempVx % 100)) / 100;
                        tempVx -= hundreds * 100;
                        int tens = (tempVx - (tempVx % 10)) / 10;
                        tempVx -= tens * 10;
                        memory[I] = (char) hundreds;
                        memory[I + 1] = (char) tens;
                        memory[I + 2] = (char) tempVx;
                        nextInstruction();
                    }
                    case 0x0055 -> {
                        for (int i = 0; i <= x; i++) {
                            memory[this.I + i] = V[i];
                        }
                        I = (char) (I + x + 1);
                        nextInstruction();
                    }
                    case 0x0065 -> {
                        for (int i = 0; i <= x; i++) {
                            V[i] = memory[this.I + i];
                        }
                        I = (char) (I + x + 1);
                        nextInstruction();
                    }
                }
                break;
            default:
                System.err.println("ERROR: Unsupported opcode. Shutting down...");
                System.exit(-1);
                break;
        }
        decreaseTimers();
        playSound();
    }

    /**
     * Goes to next instruction by adding 2 to the program counter
     */
    private void nextInstruction() {
        this.pc += 2;
    }

    /**
     * Skips an instruction by adding 4 to the program counter
     */
    private void skipInstruction() {
        this.pc += 4;
    }

    /**
     * Plays a "beep" sound
     */
    private void playSound() {
        if (st > 0) {
            midiChannel.noteOn(NOTE_SOUND, 60);
        }
        if (sp == 0 && midiChannel != null) {
            midiChannel.noteOff(NOTE_SOUND);
        }
    }

}
