/**
 * Created by marcos on 12/02/14.
 */

import java.io.*;

public class NesCartridge {

    public static final int PRG_BANK_SIZE = 0x4000;
    public static final int CHR_BANK_SIZE = 0x2000;

    private byte[] prgRom = null;
    private byte[] chrRom = null;
    private int prgBanks = 0;
    private int chrBanks = 0;
    private int flag6 = 0;
    private int flag7 = 0;
    private boolean isChrRam = false;

    public NesCartridge() {
    }

    public int getMirroring() {
        if ((flag6 & 1) == 1)
            return NesPPU.MIRROR_VERTICAL;
        else
            return NesPPU.MIRROR_HORIZONTAL;
    }
    private boolean checkSignature(byte[] buf) {
        if (buf[0] == 'N' && buf[1] == 'E' &&
                buf[2] == 'S' && buf[3] == 0x1A)
            return true;
        return false;
    }

    public byte[] getPrgRom() {
        return prgRom;
    }

    public byte[] getChrRom() {
        return chrRom;
    }

    public int getPrgBanks() {
        return prgBanks;
    }

    public int getChrBanks() {
        return chrBanks;
    }

    public boolean canWriteChr() { return isChrRam; }

    public int getMapperNumber() {

        return (flag7 & 0xF0) | ((flag6 & 0xF0) >> 4);
    }

    /*
     * Carrega uma ROM
     */
    public boolean load(String filename) {
        File file = new File(filename);
        FileInputStream stream = null;

        if (!file.exists()) {
            System.out.println("Não foi possível carregar o arquivo: " + filename);
            return false;
        }
        try {
            stream = new FileInputStream(file);

            byte[] signature = new byte[4];
            stream.read(signature);

            if (!checkSignature(signature)) {
                System.out.println("Não é uma ROM válida!");
                return false;
            }
            System.out.println("Arquivo de ROM válido, carregando...");

            prgBanks = stream.read();
            chrBanks = stream.read();
            flag6 = stream.read();
            flag7 = stream.read();

            System.out.println("PRG-BANKS: " + prgBanks);
            System.out.println("CHR-BANKS: " + chrBanks);
            System.out.println("MAPPER-NO: " + getMapperNumber());

            stream.skip(8);

            if (chrBanks == 0) {
                chrBanks = 1;
                isChrRam = true;
            } else isChrRam = false;

            int prgSize = prgBanks * PRG_BANK_SIZE;
            int chrSize = chrBanks * CHR_BANK_SIZE;

            prgRom = new byte[prgSize];
            chrRom = new byte[chrSize];

            stream.read(prgRom);
            stream.read(chrRom);
            stream.close();
        } catch (IOException e) {
            System.out.println("Erro de IO");
            return false;
        } finally {
        }
        System.out.println("ROM carregada com sucesso!");
        savePrg();
        saveChr();
        return true;
    }

    private void savePrg() {
        try {
        FileOutputStream fos = new FileOutputStream("prg.bin");
        fos.write(prgRom);
        fos.close();
        } catch (Exception e) {

        }
    }

    private void saveChr() {
        try {
            FileOutputStream fos = new FileOutputStream("chr.bin");
            fos.write(chrRom);
            fos.close();
        } catch (Exception e) {

        }
    }

}
