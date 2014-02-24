/**
 * Created by marcos on 17/02/14.
 */
public class NesJoystick {
    private NesBus bus = null;
    private int currentBit;

    public NesJoystick(NesBus bus) {
        this.bus = bus;
        currentBit = 0;
        bus.mapWriter(0x4016, 0x4017, new JoystickWriter(bus));
        bus.mapReader(0x4016, 0x4017, new JoystickReader(bus));
    }

    private class JoystickReader extends NesBusReader {
        public JoystickReader(NesBus bus) {
            super(bus);
        }
        @Override
        public int read(int address) {
            int data = 0;
            if (currentBit >= 8)
                currentBit = 0;
            switch (address & 0xFFFF) {
                // player 1
                case 0x4016:
                    if (NesGui.getKeyState(NesGui.JOYSTICK_PLAYER1 + currentBit))
                        data = 1;
                    currentBit++;
                    break;
                // player 2
                case 0x4017:
                    if (NesGui.getKeyState(NesGui.JOYSTICK_PLAYER2 + currentBit))
                        data = 1;
                    currentBit++;
                    break;
            }
            return data & 1;
        }
    }

    private class JoystickWriter extends NesBusWriter {
        public JoystickWriter(NesBus bus) {
            super(bus);
        }
        @Override
        public void write(int address, int data) {
            switch (address & 0xFFFF) {
                case 0x4017:
                    currentBit = 0;
                    break;
            }
        }
    }
}
