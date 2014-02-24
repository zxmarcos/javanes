/**
 * Created by marcos on 12/02/14.
 */

public class NesMapper {
    protected NesBus bus;

    public NesMapper(NesBus bus) {
        this.bus = bus;
    }

    public void setup() {
    }

    public static NesMapper getMapper(NesBus bus, int no) {
        switch (no) {
            case 2:
                return new MapperUNROM(bus);
            default:
                return new MapperNROM(bus);
        }

    }
}

class MapperNROM extends NesMapper {
    public MapperNROM(NesBus bus) {
        super(bus);
    }

    @Override
    public void setup() {
        NesCartridge cart = bus.getCartridge();

        if (cart.getPrgBanks() < 2) {
            bus.setPrgBank16k(0, 0);
            bus.setPrgBank16k(1, 0);
        } else {
            bus.setPrgBank16k(0, 0);
            bus.setPrgBank16k(1, bus.getLastPrgPage(16));
        }
        bus.setChrBank8k(0);
    }

}

class MapperUNROM extends NesMapper {
    public MapperUNROM(NesBus bus) {
        super(bus);
    }

    private class UNROMWriter extends NesBusWriter {
        public UNROMWriter(NesBus bus) {
            super(bus);
        }

        @Override
        public void write(int address, int value) {
            bus.setPrgBank16k(0, value);
        }
    }

    @Override
    public void setup() {
        NesCartridge cart = bus.getCartridge();

        if (cart.getPrgBanks() <= 4) {
            bus.setPrgBank16k(0, 0);
            bus.setPrgBank16k(1, 0);
        } else {
            bus.setPrgBank16k(0, 0);
            bus.setPrgBank16k(1, bus.getLastPrgPage(16));
        }
        bus.setChrBank8k(0);
        bus.mapWriter(0x8000, 0xFFFF, new UNROMWriter(bus));
    }
}