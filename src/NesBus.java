import java.io.FileOutputStream;

/**
 * Created by marcos on 11/02/14.
 */


public class NesBus {

    public static final int ADDRESS_SIZE = 0x10000;
    public static final int ADDRESS_SHIFT = 0;
    public static final int PRG_BANKS = 8;
    public static final int PRG_ADDRESS_SIZE = 0x8000;
    public static final int PRG_BANK_SIZE = 0x1000;
    public static final int PRG_BANK_SHIFT = 12;
    public static final int PRG_BANK_MASK = 0xFFF;
    public static final int CHR_BANKS = 8;
    public static final int CHR_BANK_SIZE = 0x400;
    public static final int CHR_BANK_SHIFT = 10;
    public static final int CHR_BANK_MASK = 0x3FF;
    /*
     * Os handlers padrões
     */
    private static final NesBusReader defaultReader = new NesBusReader(null);
    private static final NesBusWriter defaultWriter = new NesBusWriter(null);
    private NesCartridge cartridge = null;
    private NesBusReader[] reader = null;
    private NesBusWriter[] writer = null;
    private byte[] prgMemory = null;
    private byte[] chrMemory = null;
    private byte[] ramMemory = new byte[0x800];
    private int[] prgBank = new int[PRG_BANKS];
    private int[] chrBank = new int[CHR_BANKS];
    private NesMapper mapper = null;

    private NesCpu cpu = null;
    private NesPPU ppu = null;

    public void setCpu(NesCpu cpu) { this.cpu = cpu; }
    public NesCpu getCpu() { return cpu; }
    public void setPPU(NesPPU ppu) { this.ppu = ppu; }
    public NesPPU getPPU() { return ppu; }

    public NesBus() {
        int pages = ADDRESS_SIZE >> ADDRESS_SHIFT;

        reader = new NesBusReader[pages];
        writer = new NesBusWriter[pages];

        for (int i = 0; i < pages; i++) {
            reader[i] = defaultReader;
            writer[i] = defaultWriter;
        }

        mapReader(0x0000, 0x1FFF, new RamReader(this));
        mapWriter(0x0000, 0x1FFF, new RamWriter(this));

        mapReader(0x8000, 0xFFFF, new PrgReader(this));
    }

    public void saveRoms() {
        try {
            FileOutputStream fos = new FileOutputStream("prg.bin");
            fos.write(prgMemory);
            fos.close();
        } catch (Exception e) {

        }
        try {
            FileOutputStream fos = new FileOutputStream("chr.bin");
            fos.write(chrMemory);
            fos.close();
        } catch (Exception e) {

        }
        try {
            FileOutputStream fos = new FileOutputStream("chrRAM.bin");
            byte[] stream = new byte[0x2000];
            for (int i = 0; i < 0x2000; i++) {
                stream[i] = (byte)(readChr(i) & 0xFF);
            }
            fos.write(stream);
            fos.close();
        } catch (Exception e) {

        }
    }

    public void setCartridge(NesCartridge cartridge) {
        this.cartridge = cartridge;
        prgMemory = cartridge.getPrgRom();
        chrMemory = cartridge.getChrRom();
    }

    public NesCartridge getCartridge() {
        return cartridge;
    }

    public void mapReader(int start, int end, NesBusReader reader) {
        int startPage = start >> ADDRESS_SHIFT;
        int endPage = end >> ADDRESS_SHIFT;
        int pagesToMap = endPage - startPage + 1;

        if (reader == null)
            reader = defaultReader;

        for (int page = 0; page < pagesToMap; page++) {
            this.reader[startPage + page] = reader;
        }
    }

    public void mapWriter(int start, int end, NesBusWriter writer) {
        int startPage = start >> ADDRESS_SHIFT;
        int endPage = end >> ADDRESS_SHIFT;
        int pagesToMap = endPage - startPage + 1;

        if (writer == null)
            writer = defaultWriter;

        for (int page = 0; page < pagesToMap; page++) {
            this.writer[startPage + page] = writer;
        }
    }

    public int read(int address) {
        address &= 0xFFFF;
        return reader[address >> ADDRESS_SHIFT].read(address) & 0xFF;
    }

    public int read16(int address) {
        address &= 0xFFFF;
        return ((read(address) & 0xFF) | ((read(address + 1) & 0xFF) << 8)) & 0xFFFF;
    }

    public void write(int address, int value) {
        address &= 0xFFFF;
        value &= 0xFF;
        writer[address >> ADDRESS_SHIFT].write(address, value);
    }

    private int getChrRamPages(int k) {
        return 0x2000 / (k * 0x400);
    }

    private int getChrRomPages(int k) {
        return chrMemory.length / (k * 0x400);
    }

    private int getPrgRamPages(int k) {
        return 0x8000 / (k * 0x400);
    }

    private int getPrgRomPages(int k) {
        return prgMemory.length / (k * 0x400);
    }

    public int getLastPrgPage(int k) {
        return getPrgRomPages(k) - 1;
    }

    public void setPrgBank4k(int page, int bank) {
        if (page < getPrgRamPages(4) && bank < getPrgRomPages(4)) {
            prgBank[page] = bank;
        }
    }

    public void setPrgBank8k(int page, int bank) {
        if (page < getPrgRamPages(8) && bank < getPrgRomPages(8)) {
            setPrgBank4k(page * 2, bank * 2);
            setPrgBank4k(page * 2 + 1, bank * 2 + 1);
        }
    }

    public void setPrgBank16k(int page, int bank) {
        if (page < getPrgRamPages(16) && bank < getPrgRomPages(16)) {
            setPrgBank8k(page * 2, bank * 2);
            setPrgBank8k(page * 2 + 1, bank * 2 + 1);
        }
    }

    public void setPrgBank32k(int bank) {
        if (bank < getPrgRomPages(32)) {
            setPrgBank16k(0, bank * 2);
            setPrgBank16k(1, bank * 2 + 1);
        }
    }

    public void setChrBank1k(int page, int bank) {
        if (page < getChrRamPages(1) && bank < getChrRomPages(1)) {
            chrBank[page] = bank;
        }
    }

    public void setChrBank2k(int page, int bank) {
        if (page < getChrRamPages(2) && bank < getChrRomPages(2)) {
            setChrBank1k(page * 2, bank * 2);
            setChrBank1k(page * 2 + 1, bank * 2 + 1);
        }
    }

    public void setChrBank4k(int page, int bank) {
        if (page < getChrRamPages(4) && bank < getChrRomPages(4)) {
            setChrBank2k(page * 2, bank * 2);
            setChrBank2k(page * 2 + 1, bank * 2 + 1);
        }
    }

    public void setChrBank8k(int bank) {
        if (bank < getChrRomPages(4)) {
            setChrBank4k(0, bank * 2);
            setChrBank4k(1, bank * 2 + 1);
        }
    }

    public void setupMapper() {
        mapper = NesMapper.getMapper(this, cartridge.getMapperNumber());
        mapper.setup();
    }

    /*
     * RAM handler
     */
    private class RamReader extends NesBusReader {
        public RamReader(NesBus bus) {
            super(bus);
        }

        @Override
        public int read(int address) {
            return ((int) ramMemory[address & 0x7FF]) & 0xFF;
        }
    }

    private class RamWriter extends NesBusWriter {
        public RamWriter(NesBus bus) {
            super(bus);
        }

        @Override
        public void write(int address, int value) {
            ramMemory[address & 0x7FF] = (byte) value;
        }
    }

    public int readChr(int address) {
        address &= 0x1FFF;
        int adr = address;
        int bank = adr >> CHR_BANK_SHIFT;

        int offset = address & CHR_BANK_MASK;
        int adrbank = chrBank[bank];


        return chrMemory[(adrbank << CHR_BANK_SHIFT) + offset];
    }

    public void writeChr(int address, int value) {
        if (!cartridge.canWriteChr())
            return;

        address &= 0x1FFF;
        value &= 0xFF;

        int bank = address >> CHR_BANK_SHIFT;
        int offset = address & CHR_BANK_MASK;
        int adrbank = chrBank[bank];

        chrMemory[(adrbank << CHR_BANK_SHIFT) + offset] = (byte) value;
    }

    /*
     * PRG handler
     */
    private class PrgReader extends NesBusReader {
        public PrgReader(NesBus bus) {
            super(bus);
        }

        @Override
        public int read(int address) {
            int adr = address - PRG_ADDRESS_SIZE;
            int bank = adr >> PRG_BANK_SHIFT;

            int offset = address & PRG_BANK_MASK;
            int adrbank = prgBank[bank];

            return prgMemory[(adrbank << PRG_BANK_SHIFT) + offset];
        }
    }
}
