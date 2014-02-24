/**
 * Created by marcos on 23/02/14.
 */
public class NesAPU {
    private NesBus bus;
    public NesAPU(NesBus bus) {
        this.bus = bus;
    }
    private class APUWriter extends NesBusWriter {
        public APUWriter(NesBus bus) {
            super(bus);
        }
        @Override
        public void write(int address, int data) {
        }
    }

    private class APUReader extends NesBusReader {
        public APUReader(NesBus bus) {
            super(bus);
        }
        @Override
        public int read(int address) {
            return 0;
        }
    }
}
