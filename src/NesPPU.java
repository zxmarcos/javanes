/**
 * Created by marcos on 13/02/14.
 */

import java.io.FileOutputStream;

public class NesPPU {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 240;
    private int[] framebuffer = new int[WIDTH * HEIGHT];
    public static final int MIRROR_HORIZONTAL = 0;
    public static final int MIRROR_VERTICAL = 1;
    public static final int MIRROR_FOURSCREEN = 2;
    public static final int MIRROR_SINGLESCREEN = 3;
    private static final int REG_CONTROL = 0x2000;
    private static final int REG_MASK = 0x2001;
    private static final int REG_STATUS = 0x2002;
    private static final int REG_OAM_ADDR = 0x2003;
    private static final int REG_OAM_DATA = 0x2004;
    private static final int REG_SCROLL = 0x2005;
    private static final int REG_VRAM_ADDR = 0x2006;
    private static final int REG_VRAM_DATA = 0x2007;
    private static final int R_CONTROL_NMI = 0x80;
    private static final int R_CONTROL_SPR_SIZE = 0x20;
    private static final int R_CONTROL_BG_PATTERN = 0x10;
    private static final int R_CONTROL_SPR_PATTERN = 0x08;
    private static final int R_CONTROL_VRAM_INC = 0x04;
    private static final int R_CONTROL_NAMETABLE = 0x03;
    private static final int R_MASK_SPR_ON = 0x10;
    private static final int R_MASK_BG_ON = 0x08;
    private static final int R_MASK_SPR_CLIP = 0x04;
    private static final int R_MASK_BG_CLIP = 0x02;
    private static final int R_MASK_MONOCHROME = 0x01;
    private static final int R_STATUS_VBLANK = 0x80;
    private static final int R_STATUS_SPR0_HIT = 0x40;
    private static final int R_STATUS_SPR_LINE_COUNT = 0x20;
    private static final int[][] paletteColors = {
            {0x80, 0x80, 0x80}, {0x00, 0x00, 0xBB}, {0x37, 0x00, 0xBF}, {0x84, 0x00, 0xA6},
            {0xBB, 0x00, 0x6A}, {0xB7, 0x00, 0x1E}, {0xB3, 0x00, 0x00}, {0x91, 0x26, 0x00},
            {0x7B, 0x2B, 0x00}, {0x00, 0x3E, 0x00}, {0x00, 0x48, 0x0D}, {0x00, 0x3C, 0x22},
            {0x00, 0x2F, 0x66}, {0x00, 0x00, 0x00}, {0x05, 0x05, 0x05}, {0x05, 0x05, 0x05},

            {0xC8, 0xC8, 0xC8}, {0x00, 0x59, 0xFF}, {0x44, 0x3C, 0xFF}, {0xB7, 0x33, 0xCC},
            {0xFF, 0x33, 0xAA}, {0xFF, 0x37, 0x5E}, {0xFF, 0x37, 0x1A}, {0xD5, 0x4B, 0x00},
            {0xC4, 0x62, 0x00}, {0x3C, 0x7B, 0x00}, {0x1E, 0x84, 0x15}, {0x00, 0x95, 0x66},
            {0x00, 0x84, 0xC4}, {0x11, 0x11, 0x11}, {0x09, 0x09, 0x09}, {0x09, 0x09, 0x09},

            {0xFF, 0xFF, 0xFF}, {0x00, 0x95, 0xFF}, {0x6F, 0x84, 0xFF}, {0xD5, 0x6F, 0xFF},
            {0xFF, 0x77, 0xCC}, {0xFF, 0x6F, 0x99}, {0xFF, 0x7B, 0x59}, {0xFF, 0x91, 0x5F},
            {0xFF, 0xA2, 0x33}, {0xA6, 0xBF, 0x00}, {0x51, 0xD9, 0x6A}, {0x4D, 0xD5, 0xAE},
            {0x00, 0xD9, 0xFF}, {0x66, 0x66, 0x66}, {0x0D, 0x0D, 0x0D}, {0x0D, 0x0D, 0x0D},

            {0xFF, 0xFF, 0xFF}, {0x84, 0xBF, 0xFF}, {0xBB, 0xBB, 0xFF}, {0xD0, 0xBB, 0xFF},
            {0xFF, 0xBF, 0xEA}, {0xFF, 0xBF, 0xCC}, {0xFF, 0xC4, 0xB7}, {0xFF, 0xCC, 0xAE},
            {0xFF, 0xD9, 0xA2}, {0xCC, 0xE1, 0x99}, {0xAE, 0xEE, 0xB7}, {0xAA, 0xF7, 0xEE},
            {0xB3, 0xEE, 0xFF}, {0xDD, 0xDD, 0xDD}, {0x11, 0x11, 0x11}, {0x11, 0x11, 0x11}
    };
    private int patternTable = 0;
    private NesBus bus;
    /* Guardamos as informações no formato original do hardware */
    private int control;
    private int mask;
    private int status;
    private int oamAddress;
    private int oamData;
    private int latch;
    private int address;
    private int fineX;
    private boolean toogle = false;
    private int cachedData = 0;
    private byte[] oamMemory = new byte[0x100];
    private byte[] bgPalette = new byte[16];
    private byte[] sprPalette = new byte[16];
    private byte[] nameTable = new byte[0x800];
    private int[] nameTableArrange = new int[4];
    private int[] attributeAddr = new int[32 * 30];
    private int[] attributeBit = new int[32 * 30];
    private int[] nesColors = null;

    public NesPPU(NesBus bus) {
        this.bus = bus;

        generateTables();
        bus.setPPU(this);
        bus.mapWriter(0x2000, 0x2010, new PPUWriter(bus));
        bus.mapReader(0x2000, 0x2010, new PPUReader(bus));
        setMirroring(bus.getCartridge().getMirroring());

        mask = 0;
        control = 0;
        status = 0;
        address = 0;
        latch = 0;
        fineX = 0;

        /* cria a palheta de cores */
        nesColors = new int[64];
        for (int i = 0; i < 64; i++) {
            nesColors[i] = (paletteColors[i][0] << 16) |
                    (paletteColors[i][1] << 8) | paletteColors[i][2] | (255 << 24);
        }

        cachedData = 0xFF;

    }

    private void generateTables() {
        int shift = 0;

    /* Apanhei pra conseguir escrever as fórmulas certas xD */
        for (int v_tile = 0; v_tile < 30; v_tile++) {
            for (int h_tile = 0; h_tile < 32; h_tile++) {
                int tile_pos = (v_tile * 32) + h_tile;
                int h_pos = (h_tile >> 2);
                int v_pos = (v_tile >> 2);

                attributeAddr[tile_pos] = h_pos + (v_pos * 8);

                if (((v_tile >> 1) & 1) != 0) {
                    if (((h_tile >> 1) & 1) != 0)
                        shift = 6;
                    else
                        shift = 4;
                } else {
                    if (((h_tile >> 1) & 1) != 0)
                        shift = 2;
                    else
                        shift = 0;
                }

                attributeBit[tile_pos] = shift;
            }
        }
    }

    public void saveVRAM() {
        try {
            FileOutputStream fos = new FileOutputStream("vram.bin");
            fos.write(nameTable);
            fos.close();
        } catch (Exception e) {

        }
    }

    public void run(int scanline) {
        if (scanline == 261) {
            if ((mask & R_MASK_BG_ON) != 0)
                address = (address & 0x1F) | (latch & ~0x1F);
                patternTable = ((control & R_CONTROL_BG_PATTERN) != 0) ? 0x1000:0;
            //    address = latch;
            //status &= ~R_STATUS_VBLANK;
        } else if (scanline >= 0 && scanline <= 239) {
            renderScanline(scanline);
        } else if (scanline == 241) {
            if ((control & R_CONTROL_NMI) != 0) {
                bus.getCpu().triggerNMI();
            }
            status |= R_STATUS_VBLANK;
        }
    }

    private void renderScanline(int line) {

        if ((mask & R_MASK_BG_ON) != 0) {
            renderBackground(line);
            address = (address & ~0x41F) | (latch & 0x41F);
        } else {
            int color = nesColors[bgPalette[0] & 0x3F];
            for (int i = 0; i < WIDTH; i++) {
                framebuffer[line * WIDTH + i] = color;
            }
        }
    }

    private int ROL(int val) {
        val &= 0xFF;
        return ((val << 1) | ((val & 0x80) >> 7)) & 0xFF;
    }

    private void renderBackground(int line) {
        int dstX = (fineX ^ 7) - 7;
        int tileCount = 0;
        int tileIndex = 0;

        int vtile = ((address & 0x3E0) >> 5) & 0x1F;
        int fineY = (address & 0x7000) >> 12;
        int pattern = 0x0;
        if ((control & R_CONTROL_BG_PATTERN) != 0)
            pattern = 0x1000;

        while (tileCount < 34) {
            int htile = address & 0x1F;
            int nametbl = (((address & 0xC00) >> 10) * 0x400);
            int pixelA, pixelB;
            int colorBits;
            int tileNum = vtile * 32 + htile;

            if (tileNum < 960) {

                tileIndex = readFromNametable(nametbl + tileNum) & 0xFF;
                colorBits = readFromNametable(nametbl + 0x3C0 + attributeAddr[tileNum]);

                pixelA = bus.readChr(pattern + (tileIndex * 16) + fineY) & 0xFF;
                pixelB = bus.readChr(pattern + (tileIndex * 16) + fineY + 8) & 0xFF;

                colorBits = ((colorBits >> attributeBit[tileNum]) & 3) << 2;

                for (int px = 0; px < 8; px++) {
                    int color;

                    pixelA = ROL(pixelA);
                    pixelB = ROL(pixelB);

                    if (dstX >= 0 && dstX < 256) {
                        color = (pixelA & 1) | ((pixelB & 1) << 1);
                        color &= 0x3F;
                        if (color != 0) {
                            color |= colorBits;
                        }
                        int cindex = bgPalette[color & 0xF];
                        framebuffer[line * WIDTH + dstX] = nesColors[cindex & 0x3F];
                    }
                    ++dstX;
                }
            }
            clockHTile();
            ++tileCount;
        }
        clockFineY();
    }

    private void clockHTile() {
        if ((address & 0x1F) == 0x1F) {
            address ^= 0x41f;
        } else
            ++address;
    }

    private void clockFineY() {
        // Y increment
        if ((address & 0x7000) == 0x7000) {
            address &= ~0x7000;
            if ((address & 0x03E0) == 0x3E0)
                address ^= 0x03E0;
            else if ((address & 0x03E0) == 0x03A0)
                address ^= 0x03A0 | 0x0800;
            else
                address += 0x0020;
        } else
            address += 0x1000;
    }

    public void setMirroring(int mirror) {
        switch (mirror) {
            case MIRROR_VERTICAL:
                nameTableArrange[0] = 0;
                nameTableArrange[1] = 1;
                nameTableArrange[2] = 0;
                nameTableArrange[3] = 1;
                break;
            case MIRROR_HORIZONTAL:
            default:
                nameTableArrange[0] = 0;
                nameTableArrange[1] = 0;
                nameTableArrange[2] = 1;
                nameTableArrange[3] = 1;
                break;
        }
    }

    public int[] getFrameBuffer() {
        return framebuffer;
    }

    /*
     * Escreve o endereço da VRAM, isto é feito em duas escritas,
     * primeiro se escreve o byte superior, e depois o inferior.
     */
    private void writeVramAddress(int value) {
        if (toogle) {
            /* segunda escrita */
            latch |= value & 0xFF;
            address = latch & 0x7FFF;
        } else {
            /* primeira escrita */
            latch &= 0xFF;
            latch = (value & 0x7F) << 8;
        }
        toogle = !toogle;
    }

    /*
     * Escreve no registrador de scroll, as escritas aqui
     * modificam os dados do latch que são na verdade registradores
     * internos da PPU utilizados na renderização.
     * Esses bits são documentados em "2C02 technical reference"/Brad Taylor.
     */
    private void writeScroll(int value) {
        if (toogle) {
            /* segunda escrita */
            latch &= ~0x73E0;
            latch |= ((value >> 3) & 0x1F) << 5;
            latch |= (value & 7) << 12;

        } else {
            /* primeira escrita */
            fineX = value & 7;
            latch &= 0x1F;
            latch |= (value >> 3) & 0x1F;
        }
        toogle = !toogle;
    }

    public void writeToPalette(int address, int value) {
        address &= 0x1F;
        if (address == 0x10) {
            bgPalette[0] = (byte) value;
        } else if ((address & 0x10) != 0) {
            sprPalette[address & 0x0F] = (byte) value;
        } else {
            bgPalette[address & 0x0F] = (byte) value;
        }
    }

    public int readFromPalette(int address) {
        address &= 0x1F;

        if (address == 0x10) {
            return bgPalette[0];
        } else if ((address & 0x10) != 0) {
            return sprPalette[address & 0x0F];
        } else {
            return bgPalette[address & 0x0F];
        }
    }

    public void writeToNametable(int address, int value) {
        int bank = nameTableArrange[(address >> 10) & 3];
        nameTable[bank * 0x400 + (address & 0x3FF)] = (byte) (value & 0xFF);
    }

    public int readFromNametable(int address) {
        int bank = nameTableArrange[(address >> 10) & 3];
        return nameTable[bank * 0x400 + (address & 0x3FF)];
    }

    public void writeToVram(int value) {
        address &= 0x3FFF;
        if (address >= 0x0000 && address <= 0x1FFF)
            bus.writeChr(address, value);
        else if (address >= 0x2000 && address <= 0x2FFF)
            writeToNametable(address, value);
        else if (address >= 0x3F00 && address <= 0x3FFF)
            writeToPalette(address, value);
        else {
            //System.out.println(String.format("I/W: %04X", address));
        }

        if ((control & R_CONTROL_VRAM_INC) != 0)
            address += 32;
        else address++;
    }

    public int readFromVram() {
        address &= 0x3FFF;
        int data = cachedData;
        if (address >= 0x0000 && address <= 0x1FFF)
            cachedData = bus.readChr(address);
        else if (address >= 0x2000 && address <= 0x2FFF)
            cachedData = readFromNametable(address);
        else if (address >= 0x3000 && address <= 0x3EFF)
            cachedData = readFromNametable(address);
        else if (address >= 0x3F00 && address <= 0x3FFF) {
            data = readFromPalette(address);
        } else {
            //System.out.println(String.format("I/R: %04X", address));
        }

        if ((control & R_CONTROL_VRAM_INC) != 0)
            address += 32;
        else address++;
        return data;
    }

    /*
     * Nossa classe responsável por responder as leituras dos
     * registradores da PPU.
     */
    private class PPUReader extends NesBusReader {
        public PPUReader(NesBus bus) {
            super(bus);
        }

        @Override
        public int read(int address) {
            int data = 0;
            switch (address & 0xFFFF) {
                /* Ao ler o status, o indicador de Vblank é zerado, e
                 * o flip-flop de escrita no endereço da vram reiniciado.
                 */
                case REG_STATUS:
                    data = status | 0x40;
                    status &= ~R_STATUS_VBLANK;
                    toogle = false;
                    break;

                case REG_OAM_DATA:
                    data = oamMemory[oamAddress & 0xFF];
                    oamAddress = (oamAddress + 1) & 0xFF;
                    break;

                case REG_VRAM_DATA:
                    data = readFromVram();
                    break;
            }
            return data & 0xFF;
        }
    }

    /*
     * Nossa classe responsável por responder as escritas nos
     * registradores da PPU.
     */
    private class PPUWriter extends NesBusWriter {
        public PPUWriter(NesBus bus) {
            super(bus);
        }

        @Override
        public void write(int address, int value) {
            /* os 5 bits menos significantes do status recebem os
             * mesmos bits do último dado escrito na PPU.
             */
            status = (status & ~0x1F) | (value & 0x1F);

            switch (address & 0xFFFF) {
                case REG_CONTROL:
                    /* afetamos os bits 11 e 12 */
                    latch &= ~(3 << 10);
                    latch |= (value & 3) << 10;
                    //System.out.println(String.format("Escrevendo em $2000 = %02X", value));
                    control = value;
                    break;
                case REG_SCROLL:
                    writeScroll(value);
                    break;
                case REG_MASK:
                    mask = value;
                    break;

                case REG_VRAM_ADDR:
                    writeVramAddress(value);
                    break;

                case REG_VRAM_DATA:
                    writeToVram(value);
                    break;
            }
        }
    }
}
