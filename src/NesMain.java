/**
 * Created by marcos on 11/02/14.
 */
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NesMain {
    public static void main(String args[]) {
        NesGui ui = new NesGui();
        ui.setVisible(true);
    }
}

class NesEmulator {
    private NesBus bus = null;
    private NesCartridge cartridge = null;
    private NesCpu cpu = null;
    private NesPPU ppu = null;
    private NesJoystick joystick = null;

    public NesEmulator() {
    }

    public NesPPU getPPU() { return ppu; }
    public NesCpu getCpu() { return cpu; }
    public NesBus getBus() { return bus; }
    public NesJoystick getJoystick() { return joystick; }

    public boolean load(String filename) {
        /*
         * A ideia é que todos os componentes do hardware sejam conectados através
         * do BUS, como no hardware real.
         * O unico objeto que não tem depência do BUS é o próprio cartucho.
         */
        cartridge = new NesCartridge();
        if (!cartridge.load(filename))
            return false;
        bus = new NesBus();
        bus.setCartridge(cartridge);
        bus.setupMapper();
        cpu = new NesCpu(bus);
        cpu.reset();
        ppu = new NesPPU(bus);
        joystick = new NesJoystick(bus);

        // libera toda a memória alocada
        System.gc();

        return true;
    }

    public void reset() {
        bus.setupMapper();
        cpu.reset();
    }

    public void doFrame() {
        final int cyclesPerLine = 341 / 3;

        int scanline = 0;
        int acc = 0;
        cpu.resetElapsed();
        while (scanline < 262) {
            if (scanline == 240) {
                ppu.run(scanline);
                acc = cpu.run(cyclesPerLine - acc);
            } else {
                acc = cpu.run(cyclesPerLine - acc);
                ppu.run(scanline);
            }
            ++scanline;
        }
    }
    public void run() {
    }
}