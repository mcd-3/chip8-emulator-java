package util;

public class NibbleUtil {

    /**
     * Returns the first nibble (4 bytes) of an opcode
     * @param opcode CHIP-8 machine instruction
     * @return char
     */
    public static char getStartNibble(char opcode) {
        return (char) (opcode & 0xF000);
    }

    /**
     * Returns the last nibble (4 bytes) of an opcode
     * @param opcode CHIP-8 machine instruction
     * @return char
     */
    public static char getEndNibble(char opcode) {
        return (char) (opcode & 0x000F);
    }

    /**
     * Returns the last nibble (4 or 8 bytes) of an opcode
     * @param opcode CHIP-8 machine instruction
     * @param isEightBytes Whether we grab 4 or 8 bytes
     * @return char
     */
    public static char getEndNibble(char opcode, boolean isEightBytes) {
        if (isEightBytes) {
            return (char) (opcode & 0x00FF);
        } else {
            return (char) (opcode & 0x000F);
        }
    }

    /**
     * Returns the last 12 bytes of an opcode
     * @param opcode CHIP-8 machine instruction
     * @return char
     */
    public static char getNNN(char opcode) {
        return (char) (opcode & 0x0FFF);
    }

    /**
     * Returns bytes 4 to 8 (0x0F00)
     * @param opcode CHIP-8 machine instruction
     * @return int
     */
    public static int getX(char opcode) {
        return (opcode & 0x0F00) >> 8;
    }

    /**
     * Returns bytes 8 to 12 (0x00F0)
     * @param opcode CHIP-8 machine instruction
     * @return int
     */
    public static int getY(char opcode) {
        return (opcode & 0x00F0) >> 4;
    }

    /**
     * Returns the last 2 nibbles of an opcode
     * @param opcode CHIP-8 machine instruction
     * @return char
     */
    public static char getKK(char opcode) {
        return (char) (opcode & 0x00FF);
    }
}
