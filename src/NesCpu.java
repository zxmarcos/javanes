/**
 * Created by marcos on 11/02/14.
 */

import java.io.*;

enum AM {
    IMP, ACC, REL, ZPG, ZPX, ZPY, ABS, ABX, ABY, IND, IZX, IZY, IMM
}

;

public class NesCpu {

    private static final int[] cyclesTable = {
            7, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
            0, 6, 0, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5,
            2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
            0, 5, 0, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4,
            2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
            2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
            0, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    };
    private static final String[] opcodeName = {

            "BRK", "ORA", "*KIL", "*SLO", "*NOP", "ORA", "ASL", "*SLO", "PHP", "ORA", "ASL", "*ANC", "*NOP", "ORA", "ASL", "*SLO",
            "BPL", "ORA", "*KIL", "*SLO", "*NOP", "ORA", "ASL", "*SLO", "CLC", "ORA", "*NOP", "*SLO", "*NOP", "ORA", "ASL", "*SLO",
            "JSR", "AND", "*KIL", "*RLA", "BIT", "AND", "ROL", "*RLA", "PLP", "AND", "ROL", "*ANC", "BIT", "AND", "ROL", "*RLA",
            "BMI", "AND", "*KIL", "*RLA", "*NOP", "AND", "ROL", "*RLA", "SEC", "AND", "*NOP", "*RLA", "*NOP", "AND", "ROL", "*RLA",
            "RTI", "EOR", "*KIL", "*SRE", "*NOP", "EOR", "LSR", "*SRE", "PHA", "EOR", "LSR", "*ALR", "JMP", "EOR", "LSR", "*SRE",
            "BVC", "EOR", "*KIL", "*SRE", "*NOP", "EOR", "LSR", "*SRE", "CLI", "EOR", "*NOP", "*SRE", "*NOP", "EOR", "LSR", "*SRE",
            "RTS", "ADC", "*KIL", "*RRA", "*NOP", "ADC", "ROR", "*RRA", "PLA", "ADC", "ROR", "*ARR", "JMP", "ADC", "ROR", "*RRA",
            "BVS", "ADC", "*KIL", "*RRA", "*NOP", "ADC", "ROR", "*RRA", "SEI", "ADC", "*NOP", "*RRA", "*NOP", "ADC", "ROR", "*RRA",
            "*NOP", "STA", "*NOP", "*SAX", "STY", "STA", "STX", "*SAX", "DEY", "*NOP", "TXA", "*XAA", "STY", "STA", "STX", "*SAX",
            "BCC", "STA", "*KIL", "*AHX", "STY", "STA", "STX", "*SAX", "TYA", "STA", "TXS", "*TAS", "*SHY", "STA", "*SHX", "*AHX",
            "LDY", "LDA", "LDX", "*LAX", "LDY", "LDA", "LDX", "*LAX", "TAY", "LDA", "TAX", "*LAX", "LDY", "LDA", "LDX", "*LAX",
            "BCS", "LDA", "*KIL", "*LAX", "LDY", "LDA", "LDX", "*LAX", "CLV", "LDA", "TSX", "*LAS", "LDY", "LDA", "LDX", "*LAX",
            "CPY", "CMP", "*NOP", "*DCP", "CPY", "CMP", "DEC", "*DCP", "INY", "CMP", "DEX", "*AXS", "CPY", "CMP", "DEC", "*DCP",
            "BNE", "CMP", "*KIL", "*DCP", "*NOP", "CMP", "DEC", "*DCP", "CLD", "CMP", "*NOP", "*DCP", "*NOP", "CMP", "DEC", "*DCP",
            "CPX", "SBC", "*NOP", "*ISC", "CPX", "SBC", "INC", "*ISC", "INX", "SBC", "NOP", "*SBC", "CPX", "SBC", "INC", "*ISC",
            "BEQ", "SBC", "*KIL", "*ISC", "*NOP", "SBC", "INC", "*ISC", "SED", "SBC", "*NOP", "*ISC", "*NOP", "SBC", "INC", "*ISC"
    };
    private static final AM[] addressingMode = {
            AM.IMP, AM.IZX, AM.IMP, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.ACC, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
            AM.ABS, AM.IZX, AM.IMP, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.ACC, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
            AM.IMP, AM.IZX, AM.IMP, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.ACC, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
            AM.IMP, AM.IZX, AM.IMP, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.ACC, AM.IMM, AM.IND, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
            AM.IMM, AM.IZX, AM.IMM, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.IMP, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPY, AM.ZPY, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABY, AM.ABY,
            AM.IMM, AM.IZX, AM.IMM, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.IMP, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPY, AM.ZPY, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABY, AM.ABY,
            AM.IMM, AM.IZX, AM.IMM, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.IMP, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
            AM.IMM, AM.IZX, AM.IMM, AM.IZX, AM.ZPG, AM.ZPG, AM.ZPG, AM.ZPG, AM.IMP, AM.IMM, AM.IMP, AM.IMM, AM.ABS, AM.ABS, AM.ABS, AM.ABS,
            AM.REL, AM.IZY, AM.IMP, AM.IZY, AM.ZPX, AM.ZPX, AM.ZPX, AM.ZPX, AM.IMP, AM.ABY, AM.IMP, AM.ABY, AM.ABX, AM.ABX, AM.ABX, AM.ABX,
    };
    private static final int C_FLAG = 0x01;
    private static final int Z_FLAG = 0x02;
    private static final int I_FLAG = 0x04;
    private static final int D_FLAG = 0x08;
    private static final int B_FLAG = 0x10;
    private static final int R_FLAG = 0x20;
    private static final int V_FLAG = 0x40;
    private static final int N_FLAG = 0x80;
    /* posições dos vetores */
    private static final int nmiVector = 0xFFFA;
    private static final int irqVector = 0xFFFC;
    private static final int resetVector = 0xFFFE;
    /* registradores gerais */
    private int X;
    private int Y;
    private int A;
    private int stack;
    private int PC;
    /* Flags do estado da cpu */
    private boolean carryFlag;
    private boolean zeroFlag;
    private boolean interruptFlag;
    private boolean decimalFlag;
    private boolean breakFlag;
    private boolean reservedFlag;
    private boolean overflowFlag;
    private boolean negativeFlag;
    private boolean irqLine;
    private boolean nmiLine;
    private int address;
    private int data;
    private NesBus bus;
    private int cycles;

    public NesCpu(NesBus bus) {
        this.bus = bus;
        bus.setCpu(this);
/*
        try {
            logFile = new File("trace.txt");
            writer = new PrintStream(logFile);
        } catch (Exception e) {
            System.out.println("Erro ao criar arquivo de trace.txt");
        }
*/
    }
/*
    File logFile = null;
    PrintStream writer = null;

    private void trace() {
        try {
            writer.printf("%04X  %s\n", (PC & 0xFFFF), dissambly(PC));
        } catch (Exception e){
            System.out.println("Erro ao escrever");
        }
    }

    public void showRegisters() {
        System.out.println(String.format("PC: %04X", PC));
    }

    protected void finalize() throws Throwable {
        System.out.println("Fechando trace...");
        try {
            writer.flush();
            writer.close();        // close open files
        } finally {
            super.finalize();
        }
    }
*/
    public void reset() {
        stack = 0xFD;
        A = 0;
        X = 0;
        Y = 0;

        /* define o estado padrão dos flags */
        carryFlag = false;
        zeroFlag = false;
        interruptFlag = true;
        decimalFlag = false;
        breakFlag = false;
        reservedFlag = true;
        overflowFlag = false;
        negativeFlag = false;

        /* nenhuma linha de interrupção está ativa */
        irqLine = false;
        nmiLine = false;

        PC = bus.read16(resetVector);
        System.out.println(String.format("RESET: PC = %04X", PC));
    }



    /*
     * Emula a cpu um determinado número de ciclos
     */
    public int run(int amount) {

        cycles = 0;
        while (cycles <= amount) {
            if (nmiLine) {
                NMI();
                cycles += 7;
                continue;
            }
            if (!interruptFlag && irqLine) {
                IRQ();
                cycles += 7;
                continue;
            }

           // trace();
            int opcode = bus.read(PC++) & 0xFF;
            cycles += cyclesTable[opcode];

            switch (opcode) {
                /*
                * JSR $addr
                * RTS
                */
                case 0x20:
                    absolute();
                    JSR();
                    break;
                case 0x60:
                    RTS();
                    break;
                /*
                * JMP $addr
                * JMP ($addr)
                */
                case 0x4C:
                    absolute();
                    JMP();
                    break;
                case 0x6C:
                    indirect();
                    JMP();
                    break;

                /*
                * LDA #imm
                * LDA $zp
                * LDA $zp, x
                * LDA $addr
                * LDA $addr, x
                * LDA $addr, y
                * LDA ($zp, x)
                * LDA ($zp), y
                */
                case 0xA9:
                    readImmediate();
                    LDA();
                    break;
                case 0xA5:
                    readZeroPage();
                    LDA();
                    break;
                case 0xB5:
                    readZeroPageX();
                    LDA();
                    break;
                case 0xAD:
                    readAbsolute();
                    LDA();
                    break;
                case 0xBD:
                    readAbsoluteX();
                    LDA();
                    break;
                case 0xB9:
                    readAbsoluteY();
                    LDA();
                    break;
                case 0xA1:
                    readIndirectX();
                    LDA();
                    break;
                case 0xB1:
                    readIndirectY();
                    LDA();
                    break;
                /*
                * LDX #imm
                * LDX $zp
                * LDX $zp, y
                * LDX $addr
                * LDX $addr, y
                */
                case 0xA2:
                    readImmediate();
                    LDX();
                    break;
                case 0xA6:
                    readZeroPage();
                    LDX();
                    break;
                case 0xB6:
                    readZeroPageY();
                    LDX();
                    break;
                case 0xAE:
                    readAbsolute();
                    LDX();
                    break;
                case 0xBE:
                    readAbsoluteY();
                    LDX();
                    break;
                /*
                * LDY #imm
                * LDY $zp
                * LDY $zp, x
                * LDY $addr
                * LDY $addr, x
                */
                case 0xA0:
                    readImmediate();
                    LDY();
                    break;
                case 0xA4:
                    readZeroPage();
                    LDY();
                    break;
                case 0xB4:
                    readZeroPageX();
                    LDY();
                    break;
                case 0xAC:
                    readAbsolute();
                    LDY();
                    break;
                case 0xBC:
                    readAbsoluteX();
                    LDY();
                    break;
                /*
                * ROR A
                * ROR $zp
                * ROR $zp, x
                * ROR $addr
                * ROR $addr, x
                */
                case 0x6A:
                    RORA();
                    break;
                case 0x66:
                    readZeroPage();
                    ROR();
                    break;
                case 0x76:
                    readZeroPageX();
                    ROR();
                    break;
                case 0x6E:
                    readAbsolute();
                    ROR();
                    break;
                case 0x7E:
                    readAbsoluteX();
                    ROR();
                    break;
                /*
                * ROL A
                * ROL $zp
                * ROL $zp, x
                * ROL $addr
                * ROL $addr, x
                */
                case 0x2A:
                    ROLA();
                    break;
                case 0x26:
                    readZeroPage();
                    ROL();
                    break;
                case 0x36:
                    readZeroPageX();
                    ROL();
                    break;
                case 0x2E:
                    readAbsolute();
                    ROL();
                    break;
                case 0x3E:
                    readAbsoluteX();
                    ROL();
                    break;



                /*
                * LSR A
                * LSR $zp
                * LSR $zp, x
                * LSR $addr
                * LSR $addr, x
                */
                case 0x4A:
                    LSRA();
                    break;
                case 0x46:
                    readZeroPage();
                    LSR();
                    break;
                case 0x56:
                    readZeroPageX();
                    LSR();
                    break;
                case 0x4E:
                    readAbsolute();
                    LSR();
                    break;
                case 0x5E:
                    readAbsoluteX();
                    LSR();
                    break;
                /*
                * ASL A
                * ASL $zp
                * ASL $zp, x
                * ASL $addr
                * ASL $addr, x
                */
                case 0x0A:
                    ASLA();
                    break;
                case 0x06:
                    readZeroPage();
                    ASL();
                    break;
                case 0x16:
                    readZeroPageX();
                    ASL();
                    break;
                case 0x0E:
                    readAbsolute();
                    ASL();
                    break;
                case 0x1E:
                    readAbsoluteX();
                    ASL();
                    break;
                /*
                * INC $zp
                * INC $zp, x
                * INC $addr
                * INC $addr, x
                */
                case 0xE6:
                    readZeroPage();
                    INC();
                    break;
                case 0xF6:
                    readZeroPageX();
                    INC();
                    break;
                case 0xEE:
                    readAbsolute();
                    INC();
                    break;
                case 0xFE:
                    readAbsoluteX();
                    INC();
                    break;
                /*
                * DEC $zp
                * DEC $zp, x
                * DEC $addr
                * DEC $addr, x
                */
                case 0xC6:
                    readZeroPage();
                    DEC();
                    break;
                case 0xD6:
                    readZeroPageX();
                    DEC();
                    break;
                case 0xCE:
                    readAbsolute();
                    DEC();
                    break;
                case 0xDE:
                    readAbsoluteX();
                    DEC();
                    break;

                /*
                * CPX #imm
                * CPX $zp
                * CPX $addr
                */
                case 0xE0:
                    readImmediate();
                    CPX();
                    break;
                case 0xE4:
                    readZeroPage();
                    CPX();
                    break;
                case 0xEC:
                    readAbsolute();
                    CPX();
                    break;

                /*
                * CPY #imm
                * CPY $zp
                * CPY $addr
                */
                case 0xC0:
                    readImmediate();
                    CPY();
                    break;
                case 0xC4:
                    readZeroPage();
                    CPY();
                    break;
                case 0xCC:
                    readAbsolute();
                    CPY();
                    break;

                /*
                * ADC #imm
                * ADC $zp
                * ADC $zp, x
                * ADC $addr
                * ADC $addr, x
                * ADC $addr, y
                * ADC ($zp, x)
                * ADC ($zp), y
                */
                case 0x69:
                    readImmediate();
                    ADC();
                    break;
                case 0x65:
                    readZeroPage();
                    ADC();
                    break;
                case 0x75:
                    readZeroPageX();
                    ADC();
                    break;
                case 0x6D:
                    readAbsolute();
                    ADC();
                    break;
                case 0x7D:
                    readAbsoluteX();
                    ADC();
                    break;
                case 0x79:
                    readAbsoluteY();
                    ADC();
                    break;
                case 0x61:
                    readIndirectX();
                    ADC();
                    break;
                case 0x71:
                    readIndirectY();
                    ADC();
                    break;
                /*
                * SBC #imm
                * SBC $zp
                * SBC $zp, x
                * SBC $addr
                * SBC $addr, x
                * SBC $addr, y
                * SBC ($zp, x)
                * SBC ($zp), y
                */
                case 0xE9:
                    readImmediate();
                    SBC();
                    break;
                case 0xE5:
                    readZeroPage();
                    SBC();
                    break;
                case 0xF5:
                    readZeroPageX();
                    SBC();
                    break;
                case 0xED:
                    readAbsolute();
                    SBC();
                    break;
                case 0xFD:
                    readAbsoluteX();
                    SBC();
                    break;
                case 0xF9:
                    readAbsoluteY();
                    SBC();
                    break;
                case 0xE1:
                    readIndirectX();
                    SBC();
                    break;
                case 0xF1:
                    readIndirectY();
                    SBC();
                    break;
                /*
                * EOR #imm
                * EOR $zp
                * EOR $zp, x
                * EOR $addr
                * EOR $addr, x
                * EOR $addr, y
                * EOR ($zp, x)
                * EOR ($zp), y
                */
                case 0x49:
                    readImmediate();
                    EOR();
                    break;
                case 0x45:
                    readZeroPage();
                    EOR();
                    break;
                case 0x55:
                    readZeroPageX();
                    EOR();
                    break;
                case 0x4D:
                    readAbsolute();
                    EOR();
                    break;
                case 0x5D:
                    readAbsoluteX();
                    EOR();
                    break;
                case 0x59:
                    readAbsoluteY();
                    EOR();
                    break;
                case 0x41:
                    readIndirectX();
                    EOR();
                    break;
                case 0x51:
                    readIndirectY();
                    EOR();
                    break;
                /*
                * ORA #imm
                * ORA $zp
                * ORA $zp, x
                * ORA $addr
                * ORA $addr, x
                * ORA $addr, y
                * ORA ($zp, x)
                * ORA ($zp), y
                */
                case 0x09:
                    readImmediate();
                    ORA();
                    break;
                case 0x05:
                    readZeroPage();
                    ORA();
                    break;
                case 0x15:
                    readZeroPageX();
                    ORA();
                    break;
                case 0x0D:
                    readAbsolute();
                    ORA();
                    break;
                case 0x1D:
                    readAbsoluteX();
                    ORA();
                    break;
                case 0x19:
                    readAbsoluteY();
                    ORA();
                    break;
                case 0x01:
                    readIndirectX();
                    ORA();
                    break;
                case 0x11:
                    readIndirectY();
                    ORA();
                    break;
                /*
                * CMP #imm
                * CMP $zp
                * CMP $zp, x
                * CMP $addr
                * CMP $addr, x
                * CMP $addr, y
                * CMP ($zp, x)
                * CMP ($zp), y
                */
                case 0xC9:
                    readImmediate();
                    CMP();
                    break;
                case 0xC5:
                    readZeroPage();
                    CMP();
                    break;
                case 0xD5:
                    readZeroPageX();
                    CMP();
                    break;
                case 0xCD:
                    readAbsolute();
                    CMP();
                    break;
                case 0xDD:
                    readAbsoluteX();
                    CMP();
                    break;
                case 0xD9:
                    readAbsoluteY();
                    CMP();
                    break;
                case 0xC1:
                    readIndirectX();
                    CMP();
                    break;
                case 0xD1:
                    readIndirectY();
                    CMP();
                    break;
                /*
                * AND #imm
                * AND $zp
                * AND $zp, x
                * AND $addr
                * AND $addr, x
                * AND $addr, y
                * AND ($zp, x)
                * AND ($zp), y
                */
                case 0x29:
                    readImmediate();
                    AND();
                    break;
                case 0x25:
                    readZeroPage();
                    AND();
                    break;
                case 0x35:
                    readZeroPageX();
                    AND();
                    break;
                case 0x2D:
                    readAbsolute();
                    AND();
                    break;
                case 0x3D:
                    readAbsoluteX();
                    AND();
                    break;
                case 0x39:
                    readAbsoluteY();
                    AND();
                    break;
                case 0x21:
                    readIndirectX();
                    AND();
                    break;
                case 0x31:
                    readIndirectY();
                    AND();
                    break;
                /*
                * BIT $zp
                * BIT $addr
                */
                case 0x24:
                    readZeroPage();
                    BIT();
                    break;
                case 0x2C:
                    readAbsolute();
                    BIT();
                    break;
                /*
                * BCC $disp
                * BCS $disp
                * BEQ $disp
                * BMI $disp
                * BNE $disp
                * BPL $disp
                * BVC $disp
                * BVS $disp
                */
                case 0x90:
                    relative();
                    BCC();
                    break;
                case 0xB0:
                    relative();
                    BCS();
                    break;
                case 0xF0:
                    relative();
                    BEQ();
                    break;
                case 0x30:
                    relative();
                    BMI();
                    break;
                case 0xD0:
                    relative();
                    BNE();
                    break;
                case 0x10:
                    relative();
                    BPL();
                    break;
                case 0x50:
                    relative();
                    BVC();
                    break;
                case 0x70:
                    relative();
                    BVS();
                    break;


                /*
                * STA $zp
                * STA $zp, x
                * STA $addr
                * STA $addr, x
                * STA $addr, y
                * STA ($zp, x)
                * STA ($zp), y
                */
                case 0x85:
                    zeroPage();
                    STA();
                    break;
                case 0x95:
                    zeroPageX();
                    STA();
                    break;
                case 0x8D:
                    absolute();
                    STA();
                    break;
                case 0x9D:
                    absoluteX();
                    STA();
                    break;
                case 0x99:
                    absoluteY();
                    STA();
                    break;
                case 0x81:
                    indirectX();
                    STA();
                    break;
                case 0x91:
                    indirectY();
                    STA();
                    break;
                /*
                * STX $zp
                * STX $zp, y
                * STX $addr
                */
                case 0x86:
                    zeroPage();
                    STX();
                    break;
                case 0x96:
                    zeroPageY();
                    STX();
                    break;
                case 0x8E:
                    absolute();
                    STX();
                    break;
                /*
                * STY $zp
                * STY $zp, x
                * STY $addr
                */
                case 0x84:
                    zeroPage();
                    STY();
                    break;
                case 0x94:
                    zeroPageX();
                    STY();
                    break;
                case 0x8C:
                    absolute();
                    STY();
                    break;
                /*
                * PLA
                * PLP
                */
                case 0x68:
                    PLA();
                    break;
                case 0x28:
                    PLP();
                    break;
                /*
                * PHA
                * PHP
                */
                case 0x48:
                    PHA();
                    break;
                case 0x08:
                    PHP();
                    break;
                /*
                * SEC
                * SED
                * SEI
                */
                case 0x38:
                    SEC();
                    break;
                case 0xF8:
                    SED();
                    break;
                case 0x78:
                    SEI();
                    break;
                /*
                * CLC
                * CLD
                * CLI
                * CLV
                */
                case 0x18:
                    CLC();
                    break;
                case 0xD8:
                    CLD();
                    break;
                case 0x58:
                    CLI();
                    break;
                case 0xB8:
                    CLV();
                    break;
                /*
                * NOP
                */
                case 0xEA:
                    NOP();
                    break;
                /*
                * RTI
                * BRK
                */
                case 0x40:
                    RTI();
                    break;
                case 0x00:
                    BRK();
                    break;
                /*
                * INX
                * INY
                */
                case 0xE8:
                    INX();
                    break;
                case 0xC8:
                    INY();
                    break;
                /*
                * DEX
                * DEY
                */
                case 0xCA:
                    DEX();
                    break;
                case 0x88:
                    DEY();
                    break;
                /*
                * TAX
                * TAY
                * TSX
                * TXA
                * TXS
                * TYA
                */
                case 0xAA:
                    TAX();
                    break;
                case 0xA8:
                    TAY();
                    break;
                case 0xBA:
                    TSX();
                    break;
                case 0x8A:
                    TXA();
                    break;
                case 0x9A:
                    TXS();
                    break;
                case 0x98:
                    TYA();
                    break;

                /*
                * NOP
                * NOP #$imm
                * NOP $zp
                * NOP $zp, x
                * NOP $addr
                * NOP $addr, x
                */
                case 0x1A:
                case 0x3A:
                case 0x5A:
                case 0x7A:
                case 0xDA:
                case 0xFA:
                    NOP();
                    break;
                case 0x04:
                case 0x44:
                case 0x64:
                    zeroPage();
                    NOP();
                    break;
                case 0x14:
                case 0x34:
                case 0x54:
                case 0x74:
                case 0xD4:
                case 0xF4:
                    zeroPageX();
                    NOP();
                    break;
                case 0x0C:
                    absolute();
                    NOP();
                    break;
                case 0x80:
                    immediate();
                    NOP();
                    break;
                case 0x1C:
                case 0x3C:
                case 0x5C:
                case 0x7C:
                case 0xDC:
                case 0xFC:
                    absoluteX();
                    NOP();
                    break;

                /*
                * LAX $zp
                * LAX $zp, y
                * LAX $addr
                * LAX $addr, y
                * LAX ($zp, x)
                * LAX ($zp), y
                */
                case 0xA7:
                    readZeroPage();
                    LAX();
                    break;
                case 0xB7:
                    readZeroPageY();
                    LAX();
                    break;
                case 0xAF:
                    readAbsolute();
                    LAX();
                    break;
                case 0xBF:
                    readAbsoluteY();
                    LAX();
                    break;
                case 0xA3:
                    readIndirectX();
                    LAX();
                    break;
                case 0xB3:
                    readIndirectY();
                    LAX();
                    break;

                /*
                * LAX $zp
                * LAX $zp, y
                * LAX $addr
                * LAX ($zp, x)
                */
                case 0x87:
                    zeroPage();
                    SAX();
                    break;
                case 0x97:
                    readZeroPageY();
                    SAX();
                    break;
                case 0x8F:
                    readAbsolute();
                    SAX();
                    break;
                case 0x83:
                    readIndirectX();
                    SAX();
                    break;

                /*
                * SBC #$imm
                */
                case 0xEB:
                    readImmediate();
                    SBC();
                    break;

                /*
                * DCP $zp
                * DCP $zp, x
                * DCP $addr
                * DCP $addr, x
                * DCP $addr, y
                * DCP ($zp, x)
                * DCP ($zp), y
                */
                case 0xC7:
                    readZeroPage();
                    DCP();
                    break;
                case 0xD7:
                    readZeroPageX();
                    DCP();
                    break;
                case 0xCF:
                    readAbsolute();
                    DCP();
                    break;
                case 0xDF:
                    readAbsoluteX();
                    DCP();
                    break;
                case 0xDB:
                    readAbsoluteY();
                    DCP();
                    break;
                case 0xC3:
                    readIndirectX();
                    DCP();
                    break;
                case 0xD3:
                    readIndirectY();
                    DCP();
                    break;

                /*
                * ISC $zp
                * ISC $zp, x
                * ISC $addr
                * ISC $addr, x
                * ISC $addr, y
                * ISC ($zp, x)
                * ISC ($zp), y
                */
                case 0xE7:
                    readZeroPage();
                    ISC();
                    break;
                case 0xF7:
                    readZeroPageX();
                    ISC();
                    break;
                case 0xEF:
                    readAbsolute();
                    ISC();
                    break;
                case 0xFF:
                    readAbsoluteX();
                    ISC();
                    break;
                case 0xFB:
                    readAbsoluteY();
                    ISC();
                    break;
                case 0xE3:
                    readIndirectX();
                    ISC();
                    break;
                case 0xF3:
                    readIndirectY();
                    ISC();
                    break;

                /*
                * SLO $zp
                * SLO $zp, x
                * SLO $addr
                * SLO $addr, x
                * SLO $addr, y
                * SLO ($zp, x)
                * SLO ($zp), y
                */
                case 0x07:
                    readZeroPage();
                    SLO();
                    break;
                case 0x17:
                    readZeroPageX();
                    SLO();
                    break;
                case 0x0F:
                    readAbsolute();
                    SLO();
                    break;
                case 0x1F:
                    readAbsoluteX();
                    SLO();
                    break;
                case 0x1B:
                    readAbsoluteY();
                    SLO();
                    break;
                case 0x03:
                    readIndirectX();
                    SLO();
                    break;
                case 0x13:
                    readIndirectY();
                    SLO();
                    break;

                /*
                * RLA $zp
                * RLA $zp, x
                * RLA $addr
                * RLA $addr, x
                * RLA $addr, y
                * RLA ($zp, x)
                * RLA ($zp), y
                */
                case 0x27:
                    readZeroPage();
                    RLA();
                    break;
                case 0x37:
                    readZeroPageX();
                    RLA();
                    break;
                case 0x2F:
                    readAbsolute();
                    RLA();
                    break;
                case 0x3F:
                    readAbsoluteX();
                    RLA();
                    break;
                case 0x3B:
                    readAbsoluteY();
                    RLA();
                    break;
                case 0x23:
                    readIndirectX();
                    RLA();
                    break;
                case 0x33:
                    readIndirectY();
                    RLA();
                    break;

                /*
                * SRE $zp
                * SRE $zp, x
                * SRE $addr
                * SRE $addr, x
                * SRE $addr, y
                * SRE ($zp, x)
                * SRE ($zp), y
                */
                case 0x47:
                    readZeroPage();
                    SRE();
                    break;
                case 0x57:
                    readZeroPageX();
                    SRE();
                    break;
                case 0x4F:
                    readAbsolute();
                    SRE();
                    break;
                case 0x5F:
                    readAbsoluteX();
                    SRE();
                    break;
                case 0x5B:
                    readAbsoluteY();
                    SRE();
                    break;
                case 0x43:
                    readIndirectX();
                    SRE();
                    break;
                case 0x53:
                    readIndirectY();
                    SRE();
                    break;

                /*
                * RRA $zp
                * RRA $zp, x
                * RRA $addr
                * RRA $addr, x
                * RRA $addr, y
                * RRA ($zp, x)
                * RRA ($zp), y
                */
                case 0x67:
                    readZeroPage();
                    RRA();
                    break;
                case 0x77:
                    readZeroPageX();
                    RRA();
                    break;
                case 0x6F:
                    readAbsolute();
                    RRA();
                    break;
                case 0x7F:
                    readAbsoluteX();
                    RRA();
                    break;
                case 0x7B:
                    readAbsoluteY();
                    RRA();
                    break;
                case 0x63:
                    readIndirectX();
                    RRA();
                    break;
                case 0x73:
                    readIndirectY();
                    RRA();
                    break;
                default:
                    System.out.println(String.format("%04X: Opcode inválido: %x", PC, opcode));
                    break;
            }
        }
        elapsed += cycles;
        return cycles - amount;
    }

    public long getElapsed() { return elapsed; }
    private long elapsed = 0;
    public void resetElapsed() { elapsed = 0; }

    public int opcodeSize(int opcode) {
        switch (addressingMode[opcode & 0xFF]) {
            case ABS:
            case ABX:
            case ABY:
            case IND:
                return 3;
            case ACC:
            case IMP:
                return 1;
            case IMM:
            case IZX:
            case IZY:
            case ZPG:
            case ZPX:
            case ZPY:
            case REL:
                return 2;
        }
        return 1;
    }

    public String dissambly(int at) {
        int opcode = bus.read(at) & 0xFF;
        int addr = bus.read16(at + 1);
        int relative = (at + 2) + ((int) (byte) (addr & 0xFF));
        int addr8 = addr & 0xFF;

        AM type = addressingMode[opcode];

        switch (type) {
            case IMP:
                return String.format("%s", opcodeName[opcode]);
            case ACC:
                return String.format("%s A", opcodeName[opcode]);

            case REL:
                return String.format("%s $%04X", opcodeName[opcode], relative);

            case ZPG:
                return String.format("%s $%02X", opcodeName[opcode], addr8);

            case ZPX:
                return String.format("%s $%02X,X", opcodeName[opcode], addr8);

            case ZPY:
                return String.format("%s $%02X,Y", opcodeName[opcode], addr8);

            case ABS:
                return String.format("%s $%04X", opcodeName[opcode], addr);

            case ABX:
                return String.format("%s $%04X,X", opcodeName[opcode], addr);

            case ABY:
                return String.format("%s $%04X,Y", opcodeName[opcode], addr);

            case IND:
                return String.format("%s ($%04X)", opcodeName[opcode], addr);

            case IZX:
                return String.format("%s ($%02X,X)", opcodeName[opcode], addr8);

            case IZY:
                return String.format("%s ($%02X),Y", opcodeName[opcode], addr8);

            case IMM:
                return String.format("%s #$%02X", opcodeName[opcode], addr8);
        }
        return "";
    }


    private void immediate() {
        address = PC++;
    }

    private void zeroPage() {
        address = bus.read(PC++);
    }

    private void zeroPageX() {
        zeroPage();
        address = (address + X) & 0xFF;
    }

    private void zeroPageY() {
        zeroPage();
        address = (address + Y) & 0xFF;
    }

    private void absolute() {
        address = bus.read16(PC);
        PC += 2;
    }

    private void absoluteX() {
        address = bus.read16(PC) + (X & 0xFF);
        PC += 2;
    }

    private void absoluteY() {
        address = bus.read16(PC) + (Y & 0xFF);
        PC += 2;
    }

    private void indirect() {
        absolute();
        int temp = address;
        address = bus.read(temp);
        if ((temp & 0xFF) == 0xFF)
            temp -= 0x100;
        address |= bus.read(temp + 1) << 8;
    }

    private void indirectX() {
        zeroPage();
        address = (address + X) & 0xFF;
        int temp = address;
        address = bus.read(temp);
        temp = (temp + 1) & 0xFF;
        address |= bus.read(temp) << 8;
    }

    private void indirectY() {
        zeroPage();
        address &= 0xFF;
        int temp = address;
        address = bus.read(temp);
        temp = (temp + 1) & 0xFF;
        address |= bus.read(temp) << 8;
        address += Y;
    }

    private void relative() {
        int displace = bus.read(PC++);
        address = (PC + ((int) (byte) (displace & 0xFF))) & 0xFFFF;
    }

    private void readImmediate() {
        immediate();
        data = bus.read(address);
    }

    private void readZeroPage() {
        zeroPage();
        data = bus.read(address);
    }

    private void readZeroPageX() {
        zeroPageX();
        data = bus.read(address);
    }

    private void readZeroPageY() {
        zeroPageY();
        data = bus.read(address);
    }

    private void readAbsolute() {
        absolute();
        data = bus.read(address);
    }

    private void readAbsoluteX() {
        absoluteX();
        data = bus.read(address);
    }

    private void readAbsoluteY() {
        absoluteY();
        data = bus.read(address);
    }

    private void readIndirect() {
        indirect();
        data = bus.read(address);
    }

    private void readIndirectX() {
        indirectX();
        data = bus.read(address);
    }

    private void readIndirectY() {
        indirectY();
        data = bus.read(address);
    }

    private void push(int value) {
        bus.write(0x100 | stack, value);
        --stack;
    }

    private int pop() {
        ++stack;
        return bus.read(0x100 | stack);
    }

    private void updateZN(int value) {
        value &= 0xFF;
        zeroFlag = (value == 0);
        negativeFlag = (value & 0x80) == 0x80;
    }

    private void unpackFlags(int value) {
        carryFlag = (value & C_FLAG) == C_FLAG;
        zeroFlag = (value & Z_FLAG) == Z_FLAG;
        interruptFlag = (value & 0x04) == 0x04;
        decimalFlag = (value & 0x08) == 0x08;
        breakFlag = (value & 0x10) == 0x10;
        reservedFlag = (value & 0x20) == 0x20;
        overflowFlag = (value & 0x40) == 0x40;
        negativeFlag = (value & 0x80) == 0x80;
    }

    private int packFlags() {
        int value = 0;
        if (carryFlag)
            value |= 0x01;
        if (zeroFlag)
            value |= 0x02;
        if (interruptFlag)
            value |= 0x04;
        if (decimalFlag)
            value |= 0x08;
        if (breakFlag)
            value |= 0x10;
        if (reservedFlag)
            value |= 0x20;
        if (overflowFlag)
            value |= 0x40;
        if (negativeFlag)
            value |= 0x80;
        return value;
    }

    private void LDA() {
        A = data;
        updateZN(A);
    }

    private void LDX() {
        X = data;
        updateZN(X);
    }

    private void LDY() {
        Y = data;
        updateZN(Y);
    }

    private void AND() {
        A &= data;
        updateZN(A);
    }

    private void ORA() {
        A |= data;
        updateZN(A);
    }

    private void EOR() {
        A ^= data;
        updateZN(A);
    }

    private void CMP() {
        int tmp = (A - data) & 0xFF;
        carryFlag = (A >= data);
        updateZN(tmp);
    }

    private void CPX() {
        int tmp = (X - data) & 0xFF;
        carryFlag = (X >= data);
        updateZN(tmp);
    }

    private void CPY() {
        int tmp = (Y - data) & 0xFF;
        carryFlag = (Y >= data);
        updateZN(tmp);
    }

    private void BIT() {
        zeroFlag = (A & data) == 0;
        overflowFlag = (data & 0x40) == 0x40;
        negativeFlag = (data & 0x80) == 0x80;
    }

    /* Não emulamos o modo decimal :P */
    private void ADC() {
        A &= 0xFF;
        data &= 0xFF;
        int sum = (A + data + (carryFlag ? 1 : 0)) & 0x1FF;
        overflowFlag = false;
        carryFlag = (sum > 0xFF);
        /* positivo + positivo = negativo */
        if (((A ^ sum) & (data ^ sum) & 0x80) == 0x80)
            overflowFlag = true;
        A = sum & 0xFF;
        updateZN(A);
    }

    private void SBC() {
        data ^= 0xFF;
        ADC();
    }

    private void LSRA() {
        carryFlag = (A & 1) == 1;
        A >>= 1;
        updateZN(A);
    }

    private void LSR() {
        carryFlag = (data & 1) == 1;
        data >>= 1;
        updateZN(data);
        bus.write(address, data);
    }

    private void ASLA() {
        carryFlag = (A & 0x80) == 0x80;
        A <<= 1;
        updateZN(A);
    }

    private void ASL() {
        carryFlag = (data & 0x80) == 0x80;
        data <<= 1;
        updateZN(data);
        bus.write(address, data);
    }

    private void ROLA() {
        boolean bit = carryFlag;
        carryFlag = (A & 0x80) == 0x80;
        A <<= 1;
        if (bit)
            A |= 1;
        updateZN(A);
    }

    private void ROL() {
        boolean bit = carryFlag;
        carryFlag = (data & 0x80) == 0x80;
        data <<= 1;
        if (bit)
            data |= 1;
        updateZN(data);
        bus.write(address, data);
    }

    private void RORA() {
        boolean bit = carryFlag;
        carryFlag = (A & 1) == 1;
        A >>= 1;
        if (bit)
            A |= 0x80;
        updateZN(A);
    }

    private void ROR() {
        boolean bit = carryFlag;
        carryFlag = (data & 1) == 1;
        data >>= 1;
        if (bit)
            data |= 0x80;
        updateZN(A);
        bus.write(address, data);
    }

    private void INC() {
        ++data;
        updateZN(data);
        bus.write(address, data);
    }

    private void DEC() {
        --data;
        updateZN(data);
        bus.write(address, data);
    }

    private void JSR() {
        --PC;
        push((PC & 0xFF00) >> 8);
        push(PC & 0xFF);
        PC = address;
    }

    private void RTS() {
        PC = pop() & 0xFF;
        PC |= (pop() & 0xFF) << 8;
        ++PC;
    }

    private void RTI() {
        unpackFlags(pop() & ~B_FLAG);
        reservedFlag = true;
        PC = pop() & 0xFF;
        PC |= (pop() & 0xFF) << 8;
    }

    private void PLA() {
        A = pop();
        updateZN(A);
    }

    private void PLP() {
        unpackFlags(pop());
        breakFlag = false;
        reservedFlag = true;
    }

    private void JMP() {
        PC = address;
    }

    private void INX() {
        ++X;
        X &= 0xFF;
        updateZN(X);
    }

    private void INY() {
        ++Y;
        Y &= 0xFF;
        updateZN(Y);
    }

    private void DEX() {
        --X;
        X &= 0xFF;
        updateZN(X);
    }

    private void DEY() {
        --Y;
        Y &= 0xFF;
        updateZN(Y);
    }

    private void TAX() {
        X = A;
        updateZN(X);
    }

    private void TAY() {
        Y = A;
        updateZN(Y);
    }

    private void TXA() {
        A = X;
        updateZN(A);
    }

    private void TYA() {
        A = Y;
        updateZN(A);
    }

    private void TSX() {
        X = stack;
        updateZN(X);
    }

    private void TXS() {
        stack = X;
    }

    private void NOP() {
    }

    private void CLC() {
        carryFlag = false;
    }

    private void CLD() {
        decimalFlag = false;
    }

    private void CLI() {
        interruptFlag = false;
    }

    private void CLV() {
        overflowFlag = false;
    }

    private void SEC() {
        carryFlag = true;
    }

    private void SED() {
        decimalFlag = true;
    }

    private void SEI() {
        interruptFlag = true;
    }

    private void PHA() {
        push(A);
    }

    private void PHP() {
        push(packFlags() | 0x30);
    }

    private void STA() {
        bus.write(address, A);
    }

    private void STX() {
        bus.write(address, X);
    }

    private void STY() {
        bus.write(address, Y);
    }

    private void BCC() {
        if (!carryFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BCS() {
        if (carryFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BEQ() {
        if (zeroFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BNE() {
        if (!zeroFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BMI() {
        if (negativeFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BPL() {
        if (!negativeFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BVC() {
        if (!overflowFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BVS() {
        if (overflowFlag) {
            PC = address;
            cycles += 3;
        } else
            cycles += 2;
    }

    private void BRK() {
        push((PC & 0xFF00) >> 8);
        push((PC & 0xFF) + 1);
        PHP();
        SEI();
        PC = bus.read16(irqVector);
    }

    private void LAX() {
        X = A = data;
        updateZN(A);
    }

    private void SAX() {
        bus.write(address, X & A);
    }

    private void DCP() {
        --data;
        bus.write(address, data);
        CMP();
    }

    private void ISC() {
        ++data;
        bus.write(address, data);
        SBC();
    }

    private void SLO() {
        ASL();
        ORA();
    }

    private void RLA() {
        ROL();
        AND();
    }

    private void SRE() {
        LSR();
        EOR();
    }

    private void RRA() {
        ROR();
        ADC();
    }

    public void triggerNMI() {
        nmiLine = true;
    }
    public void triggerIRQ() {
        irqLine = true;
    }
    /*
     * Interrompe a cpu e chama a rotina de NMI
     */
    private void NMI() {
        push((PC & 0xFF00) >> 8);
        push((PC & 0xFF));
        PHP();
        PC = bus.read16(nmiVector);
        nmiLine = false;
    }

    /*
     * Interrompe a cpu e chama a rotina de IRQ
     */
    private void IRQ() {
        push((PC & 0xFF00) >> 8);
        push((PC & 0xFF));
        PHP();
        SEI();
        PC = bus.read16(irqVector);
        irqLine = false;
    }

}
