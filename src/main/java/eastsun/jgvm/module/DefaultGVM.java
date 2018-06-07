package eastsun.jgvm.module;

import eastsun.jgvm.module.io.*;
import eastsun.jgvm.module.event.Area;
import eastsun.jgvm.module.event.ScreenChangeListener;
import eastsun.jgvm.module.ram.Getable;
import eastsun.jgvm.module.ram.RuntimeRam;
import eastsun.jgvm.module.ram.StringRam;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * 完全支持GVmaker1.0的JGVM实现
 *
 * @author Eastsun
 * @version 1.0 2008/1/15
 */
final class DefaultGVM extends JGVM {

    private static final int FALSE = 0;
    private static final int TRUE = -1;
    private GvmConfig config;
    private RamManager ramManager;
    private RuntimeRam runtimeRam;
    private StringRam stringRam;
    private ScreenModel screen;
    private Renderable render;
    private TextModel text;
    private KeyModel key;
    private FileModel file;
    private KeyModel.SysInfo keyInf;
    private InputMethod input;
    private LavApp app;
    private DataStack dataStack;
    private int seed;
    private boolean end;
    private Calendar cal = Calendar.getInstance();
    private Date date = new Date();

    public DefaultGVM(GvmConfig cfg, FileModel fileModel, ScreenModel screenModel, KeyModel keyModel) {
        this.config = cfg;
        runtimeRam = new RuntimeRam(cfg.runtimeRamSize());
        stringRam = new StringRam(cfg.stringRamSize());
        dataStack = new DataStack(cfg.stackSize());
        ramManager = new RamManager(runtimeRam, stringRam, dataStack);

        text = new TextModel();
        screen = screenModel;
        render = screen.getRender();

        text.setScreenModel(screen);
        if (screen.hasRelativeRam()) {
            ramManager.install(screen.getGraphRam());
            ramManager.install(screen.getBufferRam());
        }
        if (text.hasRelativeRam()) {
            ramManager.install(text.getTextRam());
        }
        key = keyModel;
        keyInf = key.getSysInfo();
        file = fileModel;
    }

    /**
     * 设置此GVM运行的lav程序文件,并对JGVM做适当的初始化
     *
     * @param app GVmaker程序
     * @throws java.lang.IllegalStateException 如果不支持此app的运行
     */
    public void loadApp(LavApp app) throws IllegalStateException {
        if (this.app != null) {
            init();
        }

        this.app = app;
        app.reset();
        end = false;

    }

    private void init() {
        render.clearBuffer();
        render.refresh();
        ramManager.clear();
        text.setTextMode(0);
    }

    /**
     * 卸去目前执行的app,并释放及清理相应资源
     */
    public void dispose() {
        if (this.app == null) {
            return;
        }
        file.dispose();
        // render.clearBuffer();
        // render.refresh();
        // ramManager.clear();
        // text.setTextMode(0);
        this.app = null;
        this.end = true;
    }

    public boolean isEnd() {
        return end;
    }

    public InputMethod setInputMethod(InputMethod im) {
        InputMethod oldValue = input;
        input = im;
        return oldValue;
    }

    /**
     * 运行下一个指令
     *
     * @throws java.lang.IllegalStateException 程序已经结束或不支持的操作
     * @throws InterruptedException            运行期间当前线程被中断
     */
    public void nextStep() throws IllegalStateException, InterruptedException {
        if (isEnd()) {
            throw new IllegalStateException("程序已经终止!");
        }
        int cmd = app.getChar();
        //System.out.println(Integer.toHexString(cmd));
        switch (cmd) {
        case 0x00:
            break;
        case 0x01:
            dataStack.push(app.getChar());
            break;
        case 0x02:
            dataStack.push(app.getInt16());
            break;
        case 0x03:
            dataStack.push(app.getInt32());
            break;
        case 0x04:
            dataStack.push(ramManager.getChar(app.getInt16() & 0xffff));
            break;
        case 0x05:
            dataStack.push(ramManager.getInt(app.getInt16() & 0xffff));
            break;
        case 0x06:
            dataStack.push(ramManager.getLong(app.getInt16() & 0xffff));
            break;
        case 0x07:
            dataStack.push(ramManager.getChar((dataStack.pop() + app.getInt16()) & 0xffff));
            break;
        case 0x08:
            dataStack.push(ramManager.getInt((dataStack.pop() + app.getInt16()) & 0xffff));
            break;
        case 0x09:
            dataStack.push(ramManager.getLong((dataStack.pop() + app.getInt16()) & 0xffff));
            break;
        case 0x0a:
            dataStack.push((app.getInt16() + dataStack.pop()) & 0xffff | 0x00010000);
            break;
        case 0x0b:
            dataStack.push((app.getInt16() + dataStack.pop()) & 0xffff | 0x00020000);
            break;
        case 0x0c:
            dataStack.push((app.getInt16() + dataStack.pop()) & 0xffff | 0x00040000);
            break;
        case 0x0d:
            dataStack.push(stringRam.addString(app) | 0x00100000);
            break;
        case 0x0e:
            dataStack.push(ramManager.getChar((app.getInt16() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x0f:
            dataStack.push(ramManager.getInt((app.getInt16() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x10:
            dataStack.push(ramManager.getLong((app.getInt16() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x11:
            dataStack.push(ramManager.getChar((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x12:
            dataStack.push(ramManager.getInt((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x13:
            dataStack.push(ramManager.getLong((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff));
            break;
        case 0x14:
            dataStack.push((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff | 0x00010000);
            break;
        case 0x15:
            dataStack.push((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff | 0x00020000);
            break;
        case 0x16:
            dataStack.push((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff | 0x00040000);
            break;
        case 0x17:
            dataStack.push((app.getInt16() + dataStack.pop()) & 0xffff);
            break;
        case 0x18:
            dataStack.push((app.getInt16() + dataStack.pop() + runtimeRam.getRegionStartAddr()) & 0xffff);
            break;
        case 0x19:
            dataStack.push((app.getInt16() + runtimeRam.getRegionStartAddr()) & 0xffff);
            break;
        case 0x1a:
            dataStack.push(text.getTextRam().getStartAddr());
            break;
        case 0x1b:
            dataStack.push(screen.getGraphRam().getStartAddr());
            break;
        case 0x1c:
            dataStack.push(-dataStack.pop());
            break;
        case 0x1d:
        case 0x1e:
        case 0x1f:
        case 0x20: {
            int data = dataStack.pop();
            int addr = data & 0xffff;
            if ((data & 0x00800000) != 0) { // ???，前面的指令不可能产生局部变量的指针？？？
                addr += runtimeRam.getRegionStartAddr();
            }
            int len = (data >>> 16) & 0x7f;
            int value = ramManager.getBytes(addr, len);
            if (len == 2) {
                //lvm的int为有符号两字节数据
                value = (short) value;
            }
            switch (cmd) {
            case 0x1d:
                dataStack.push(++value);
                break;
            case 0x1e:
                dataStack.push(--value);
                break;
            case 0x1f:
                dataStack.push(value++);
                break;
            case 0x20:
                dataStack.push(value--);
                break;
            }
            ramManager.setBytes(addr, len, value);
        }
        break;
        case 0x21:
            dataStack.push(dataStack.pop() + dataStack.pop());
            break;
        case 0x22: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 - v1);
        }
        break;
        case 0x23:
            dataStack.push(dataStack.pop() & dataStack.pop());
            break;
        case 0x24:
            dataStack.push(dataStack.pop() | dataStack.pop());
            break;
        case 0x25:
            dataStack.push(~dataStack.pop());
            break;
        case 0x26:
            dataStack.push(dataStack.pop() ^ dataStack.pop());
            break;
        case 0x27: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push((v1 != 0 && v2 != 0) ? TRUE : FALSE);
        }
        break;
        case 0x28: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push((v1 != 0 || v2 != 0) ? TRUE : FALSE);
        }
        break;
        case 0x29:
            dataStack.push(dataStack.pop() == 0 ? TRUE : FALSE);
            break;
        case 0x2a:
            dataStack.push(dataStack.pop() * dataStack.pop());
            break;
        case 0x2b: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v1 == 0 ? -1 : v2 / v1);
        }
        break;
        case 0x2c: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v1 == 0 ? 0 : v2 % v1);
        }
        break;
        case 0x2d: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 << v1);
        }
        break;
        case 0x2e: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 >> v1);
        }
        break;
        case 0x2f: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v1 == v2 ? TRUE : FALSE);
        }
        break;
        case 0x30: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v1 != v2 ? TRUE : FALSE);
        }
        break;
        case 0x31: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 <= v1 ? TRUE : FALSE);
        }
        break;
        case 0x32: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 >= v1 ? TRUE : FALSE);
        }
        break;
        case 0x33: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 > v1 ? TRUE : FALSE);
        }
        break;
        case 0x34: {
            int v1 = dataStack.pop();
            int v2 = dataStack.pop();
            dataStack.push(v2 < v1 ? TRUE : FALSE);
            break;
        }
        case 0x35: {
            int data = dataStack.pop();
            int offset = dataStack.pop();
            int addr = offset & 0xffff;
            if ((offset & 0x00800000) != 0) {
                addr += runtimeRam.getRegionStartAddr();
            }
            int len = (offset >>> 16) & 0x7f;
            ramManager.setBytes(addr, len, data);
            dataStack.push(data);
            break;
        }
        case 0x36:
            dataStack.push(ramManager.getChar(dataStack.pop() & 0xffff));
            break;
        case 0x37:
            dataStack.push(dataStack.pop() & 0xffff | 0x00010000);
            break;
        case 0x38:
            dataStack.pop();
            break;
        case 0x39: {
            int addr = app.getUint24();
            // if (dataStack.peek(0) == 0) {
            if (dataStack.lastValue() == 0) {
                app.setOffset(addr);
            }
            break;
        }
        case 0x3a: {
            int addr = app.getUint24();
            // if (dataStack.peek(0) != 0) {
            if (dataStack.lastValue() != 0) {
                app.setOffset(addr);
            }
            break;
        }
        case 0x3b:
            app.setOffset(app.getUint24());
            break;
        case 0x3c: {
            int addr = app.getInt16() & 0xffff;
            runtimeRam.setRegionEndAddr(addr);
            runtimeRam.setRegionStartAddr(addr);
        }
        break;
        case 0x3d: {
            //invoke
            int nextAddr = app.getUint24();
            int currAddr = app.getOffset();
            ramManager.setAddr(runtimeRam.getRegionEndAddr(), currAddr);
            app.setOffset(nextAddr);
        }
        break;
        case 0x3e: {
            //function entry
            ramManager.setBytes(runtimeRam.getRegionEndAddr() + 3, 2, runtimeRam.getRegionStartAddr());
            runtimeRam.setRegionStartAddr(runtimeRam.getRegionEndAddr());
            runtimeRam.setRegionEndAddr(runtimeRam.getRegionStartAddr() + (app.getInt16() & 0xffff));
            int paramCount = app.getChar();
            while (--paramCount >= 0) {
                ramManager.setLong(runtimeRam.getRegionStartAddr() + 5 + 4 * paramCount, dataStack.pop());
            }
        }
        break;
        case 0x3f: {
            int addr = ramManager.getAddr(runtimeRam.getRegionStartAddr());
            runtimeRam.setRegionEndAddr(runtimeRam.getRegionStartAddr());
            runtimeRam.setRegionStartAddr(ramManager.getInt(runtimeRam.getRegionEndAddr() + 3) & 0xffff);
            app.setOffset(addr);
        }
        break;
        case 0x40:
            end = true;
            break;
        case 0x41: {
            int addr = app.getInt16() & 0xffff;
            int len = app.getInt16() & 0xffff;
            byte b;
            while (--len >= 0) {
                //ramManager.setChar(addr++, app.getChar());
                //正常GVmaker中,这些数据是保存在runtimeRam中
                b = (byte) app.getChar();
                runtimeRam.setByte(addr++, b);
            }
        }
        break;
        case 0x42:
            dataStack.push(screen.getBufferRam().getStartAddr());
            break;
        case 0x43:
            throw new IllegalStateException("未知的指令: 0x43");
        case 0x44:
            //loadall
            break;
        case 0x45:
            dataStack.push(app.getInt16() + dataStack.pop());
            break;
        case 0x46:
            dataStack.push(dataStack.pop() - app.getInt16());
            break;
        case 0x47:
            dataStack.push(dataStack.pop() * app.getInt16());
            break;
        case 0x48: {
            int v1 = app.getInt16();
            int v2 = dataStack.pop();
            dataStack.push(v1 == 0 ? -1 : v2 / v1);
        }
        break;
        case 0x49: {
            int v1 = app.getInt16();
            int v2 = dataStack.pop();
            dataStack.push(v1 == 0 ? 0 : v2 % v1);
        }
        break;
        case 0x4a:
            dataStack.push(dataStack.pop() << app.getInt16());
            break;
        case 0x4b:
            // 无符号右移
            dataStack.push(dataStack.pop() >>> app.getInt16());
            break;
        case 0x4c:
            dataStack.push(app.getInt16() == dataStack.pop() ? TRUE : FALSE);
            break;
        case 0x4d:
            dataStack.push(app.getInt16() != dataStack.pop() ? TRUE : FALSE);
            break;
        case 0x4e:
            dataStack.push(app.getInt16() < dataStack.pop() ? TRUE : FALSE);
            break;
        case 0x4f:
            dataStack.push(app.getInt16() > dataStack.pop() ? TRUE : FALSE);
            break;
        case 0x50:
            dataStack.push(app.getInt16() <= dataStack.pop() ? TRUE : FALSE);
            break;
        case 0x51:
            dataStack.push(app.getInt16() >= dataStack.pop() ? TRUE : FALSE);
            break;

        //system function
        case 0x80:
            text.addChar((char) (dataStack.pop() & 0xff));
            text.updateLCD(0);
            break;
        case 0x81:
            dataStack.push(key.getchar());
            break;
        case 0x82:
            printf();
            break;
        //strcpy
        case 0x83: {
            int source = dataStack.pop() & 0xffff;
            int dest = dataStack.pop() & 0xffff;
            byte b;
            do {
                b = ramManager.getByte(source++);
                ramManager.setByte(dest++, b);
            } while (b != 0);
            //这个应该不会改变显存与屏幕缓冲,但可能修改文本缓冲以及读取字符堆
            break;
        }
        case 0x84: {
            int addr = dataStack.pop() & 0xffff;
            int length = 0;
            while (ramManager.getByte(addr++) != 0) {
                length++;
            }
            dataStack.push(length);
        }
        break;
        case 0x85:
            text.setTextMode(dataStack.pop() & 0xff);
            break;
        case 0x86:
            text.updateLCD(dataStack.pop());
            break;
        case 0x87: {
            int delayTime = dataStack.pop() & 0x7fff;
            if (delayTime * 3 / 4 > 0) {
                Thread.sleep(delayTime * 3 / 4);
            }
        }
        break;
        case 0x88:
            dataStack.movePointer(-6);
            render.setDrawMode(dataStack.peek(4));
            render.drawRegion((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3),
                    ramManager, dataStack.peek(5) & 0xffff);
            break;
        case 0x89:
            render.refresh();
            break;
        case 0x8a:
            dataStack.movePointer(-4);
            render.setDrawMode(dataStack.peek(3));
            render.drawString((short) dataStack.peek(0), (short) dataStack.peek(1),
                    ramManager, dataStack.peek(2) & 0xffff);
            break;
        case 0x8b:
            dataStack.movePointer(-5);
            render.setDrawMode(dataStack.peek(4) | render.RENDER_FILL_TYPE);
            render.drawRect((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3));
            break;
        case 0x8c:
            dataStack.movePointer(-5);
            render.setDrawMode(dataStack.peek(4));
            render.drawRect((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3));
            break;
        case 0x8d:
            //exit
            end = true;
            break;
        case 0x8e:
            render.clearBuffer();
            break;
        case 0x8f: {
            int value = dataStack.pop();
            dataStack.push(value >= 0 ? value : -value);
        }
        break;
        case 0x90:
            seed = seed * 22695477 + 1;
            dataStack.push((seed >> 16) & 0x7fff);
            break;
        case 0x91:
            seed = dataStack.pop();
            break;
        case 0x92: {
            int col = dataStack.pop() & 0xff;
            int row = dataStack.pop() & 0xff;
            text.setLocation(row, col);
        }
        break;
        case 0x93:
            dataStack.push(key.inkey());
            break;
        case 0x94:
            dataStack.movePointer(-3);
            render.setDrawMode(dataStack.peek(2) ^ Renderable.RENDER_GRAPH_TYPE);
            render.drawPoint((short) dataStack.peek(0), (short) dataStack.peek(1));
            break;
        case 0x95: {
            int y = (short) dataStack.pop();
            int x = (short) dataStack.pop();
            dataStack.push(render.getPoint(x, y));
        }
        break;
        case 0x96:
            dataStack.movePointer(-5);
            render.setDrawMode(dataStack.peek(4) ^ Renderable.RENDER_GRAPH_TYPE);
            render.drawLine((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3));
            break;
        case 0x97: {
            dataStack.movePointer(-6);
            int mode = dataStack.peek(5) ^ Renderable.RENDER_GRAPH_TYPE;
            if ((dataStack.peek(4) & 0xff) != 0) {
                mode |= Renderable.RENDER_FILL_TYPE;
            }
            render.setDrawMode(mode);
            render.drawRect((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3));
        }
        break;
        case 0x98: {
            dataStack.movePointer(-5);
            int mode = dataStack.peek(4) ^ Renderable.RENDER_GRAPH_TYPE;
            if ((dataStack.peek(3) & 0xff) != 0) {
                mode |= Renderable.RENDER_FILL_TYPE;
            }
            render.setDrawMode(mode);
            render.drawOval((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(2));
        }
        break;
        case 0x99: {
            dataStack.movePointer(-6);
            int mode = dataStack.peek(5) ^ Renderable.RENDER_GRAPH_TYPE;
            if ((dataStack.peek(4) & 0xff) != 0) {
                mode |= Renderable.RENDER_FILL_TYPE;
            }
            render.setDrawMode(mode);
            render.drawOval((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3));
        }
        break;
        case 0x9a:
            //beep,do nothing
            break;
        case 0x9b: {
            int c = dataStack.pop() & 0xff;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                dataStack.push(TRUE);
            } else {
                dataStack.push(FALSE);
            }
        }
        break;
        case 0x9c: {
            int c = dataStack.pop() & 0xff;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                dataStack.push(TRUE);
            } else {
                dataStack.push(FALSE);
            }
        }
        break;
        case 0x9d: {
            int c = dataStack.pop() & 0xff;
            if ((c >= 0 && c <= 0x1f) || c == 0x7f) {
                dataStack.push(TRUE);
            } else {
                dataStack.push(FALSE);
            }
        }
        break;
        case 0x9e: {
            int c = dataStack.pop() & 0xff;
            dataStack.push((c >= '0' && c <= '9') ? TRUE : FALSE);
        }
        break;
        case 0x9f: {
            int c = dataStack.pop() & 0xff;
            dataStack.push((c >= 0x21 && c <= 0x7e) ? TRUE : FALSE);
        }
        break;
        case 0xa0: {
            int c = dataStack.pop() & 0xff;
            dataStack.push((c >= 'a' && c <= 'z') ? TRUE : FALSE);
        }
        break;
        case 0xa1: {
            int c = dataStack.pop() & 0xff;
            dataStack.push((c >= 0x20 && c <= 0x7e) ? TRUE : FALSE);
        }
        break;
        //ispunct
        case 0xa2: {
            int c = dataStack.pop() & 0xff;
            if (c < 0x20 || c > 0x7e) {
                dataStack.push(FALSE);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                dataStack.push(FALSE);
            } else if ((c >= '0' && c <= '9') || c == 0x20) {
                dataStack.push(FALSE);
            } else {
                dataStack.push(TRUE);
            }
        }
        break;
        case 0xa3: {
            int c = dataStack.pop() & 0xff;
            if (c == 0x09 || c == 0x0a || c == 0x0b || c == 0x0c || c == 0x0d || c == 0x20) {
                dataStack.push(TRUE);
            } else {
                dataStack.push(FALSE);
            }
        }
        break;
        case 0xa4: {
            int c = dataStack.pop() & 0xff;
            dataStack.push((c <= 'Z' && c >= 'A') ? TRUE : FALSE);
        }
        break;
        case 0xa5: {
            int c = dataStack.pop() & 0xff;
            if (c <= 'F' && c >= 'A') {
                dataStack.push(TRUE);
            } else if (c <= 'f' && c >= 'a') {
                dataStack.push(TRUE);
            } else if (c <= '9' && c >= '0') {
                dataStack.push(TRUE);
            } else {
                dataStack.push(FALSE);
            }
        }
        break;
        //strcat
        case 0xa6: {
            //不会修改显存相关
            int src = dataStack.pop() & 0xffff;
            int dst = dataStack.pop() & 0xffff;
            while (ramManager.getByte(dst) != 0) {
                dst++;
            }
            byte b;
            do {
                b = ramManager.getByte(src++);
                ramManager.setByte(dst++, b);
            } while (b != 0);
        }
        break;
        //strchr
        case 0xa7: {
            byte c = (byte) dataStack.pop();
            int addr = dataStack.pop() & 0xffff;
            while (true) {
                byte b = ramManager.getByte(addr);
                if (b == c) {
                    break;
                }
                if (b == 0) {
                    addr = 0;
                    break;
                }
                addr++;
            }
            dataStack.push(addr);
        }
        break;
        case 0xa8: {
            int str2 = dataStack.pop() & 0xffff;
            int str1 = dataStack.pop() & 0xffff;
            int cmp = 0;
            while (true) {
                int c1 = ramManager.getChar(str1++);
                int c2 = ramManager.getChar(str2++);
                cmp = c1 - c2;
                if (cmp != 0 || c1 == 0) {
                    break;
                }
            }
            dataStack.push(cmp);
        }
        break;
        //strstr
        case 0xa9: {
            int str2 = dataStack.pop() & 0xffff;
            int str1 = dataStack.pop() & 0xffff;
            int addr = 0;
            caseA9Loop:
            while (ramManager.getByte(str1) != 0) {
                int s1 = str1;
                int s2 = str2;
                while (true) {
                    if (ramManager.getByte(s2) == 0) {
                        addr = str1;
                        break caseA9Loop;
                    }
                    if (ramManager.getByte(s1) == 0) {
                        break caseA9Loop;
                    }
                    if (ramManager.getByte(s1) != ramManager.getByte(s2)) {
                        break;
                    }
                    s1++;
                    s2++;
                }
                str1++;
            }
            dataStack.push(addr);
        }
        break;
        case 0xaa: {
            int c = dataStack.pop() & 0xff;
            if (c >= 'A' && c <= 'Z') {
                c += 'a' - 'A';
            }
            dataStack.push(c);
        }
        break;
        case 0xab: {
            int c = dataStack.pop() & 0xff;
            if (c >= 'a' && c <= 'z') {
                c += 'A' - 'a';
            }
            dataStack.push(c);
        }
        break;
        case 0xac: {
            int len = (short) dataStack.pop();
            byte b = (byte) dataStack.pop();
            int addr = dataStack.pop() & 0xffff;
            //System.out.println(addr+","+len+","+b);
            int start = addr;
            while (--len >= 0) {
                ramManager.setByte(addr++, b);
            }
        }
        break;
        case 0xad: {
            int len = (short) dataStack.pop();
            int str2 = dataStack.pop() & 0xffff;
            int str1 = dataStack.pop() & 0xffff;
            int start = str1;
            while (--len >= 0) {
                ramManager.setByte(str1++, ramManager.getByte(str2++));
            }
        }
        break;
        //fopen
        case 0xae:
            dataStack.movePointer(-2);
            dataStack.push(file.fopen(ramManager, dataStack.peek(0) & 0xffff, dataStack.peek(1) & 0xffff));
            break;
        //fclose
        case 0xaf:
            file.fclose(dataStack.pop());
            break;
        //fread
        case 0xb0: {
            dataStack.movePointer(-4);
            int v = file.fread(ramManager, dataStack.peek(0) & 0xffff,
                    (short) dataStack.peek(2), dataStack.peek(3));
            dataStack.push(v);
        }
        break;
        //fwrite
        case 0xb1: {
            dataStack.movePointer(-4);
            int v = file.fwrite(ramManager, dataStack.peek(0) & 0xffff,
                    (short) dataStack.peek(2), dataStack.peek(3));
            dataStack.push(v);
        }
        break;
        //fseek
        case 0xb2: {
            dataStack.movePointer(-3);
            int v = file.fseek(dataStack.peek(0), dataStack.peek(1), dataStack.peek(2));
            dataStack.push(v);
        }
        break;
        //ftell
        case 0xb3:
            dataStack.push(file.ftell(dataStack.pop()));
            break;
        //feof
        case 0xb4:
            dataStack.push(file.feof(dataStack.pop()) ? TRUE : FALSE);
            break;
        //rewind
        case 0xb5:
            file.rewind(dataStack.pop());
            break;
        //fgetc
        case 0xb6:
            dataStack.push(file.getc(dataStack.pop()));
            break;
        //fputc
        case 0xb7: {
            int fp = dataStack.pop();
            int ch = dataStack.pop();
            dataStack.push(file.putc(ch, fp));
        }
        break;
        //sprintf
        case 0xb8:
            sprintf();
            break;
        //makeDir
        case 0xb9:
            dataStack.push(file.makeDir(ramManager, dataStack.pop() & 0xffff) ? TRUE : FALSE);
            break;
        //deletefILE
        case 0xba:
            dataStack.push(file.deleteFile(ramManager, dataStack.pop() & 0xffff) ? TRUE : FALSE);
            break;
        //getms
        case 0xbb: {
            int ms = (int) (System.currentTimeMillis() % 1000);
            ms = ms * 256 / 1000;
            dataStack.push(ms);
        }
        break;
        //checkKey
        case 0xbc: {
            char c = (char) dataStack.pop();
            dataStack.push(key.checkKey(c));
        }
        break;
        //memmove
        case 0xbd: {
            int len = (short) dataStack.pop();
            int src = dataStack.pop() & 0xffff;
            int dst = dataStack.pop() & 0xffff;
            if (src > dst) {
                for (int index = 0; index < len; index++) {
                    ramManager.setByte(dst + index, ramManager.getByte(src + index));
                }
            } else {
                for (int index = len - 1; index >= 0; index--) {
                    ramManager.setByte(dst + index, ramManager.getByte(src + index));
                }
            }
        }
        break;
        //crc16
        case 0xbe: {
            int length = (short) dataStack.pop();
            int addr = dataStack.pop() & 0xffff;
            dataStack.push(Util.getCrc16Value(ramManager, addr, length));
        }
        break;
        //secret
        case 0xbf: {
            int strAddr = dataStack.pop() & 0xffff;
            int length = (short) dataStack.pop();
            int memAddr = dataStack.pop() & 0xffff;
            int index = 0;
            while (--length >= 0) {
                byte mask = ramManager.getByte(strAddr + index);
                if (mask == 0) {
                    index = 0;
                    mask = ramManager.getByte(strAddr + index);
                }
                byte value = ramManager.getByte(memAddr);
                ramManager.setByte(memAddr, (byte) (value ^ mask));
                index++;
                memAddr++;
            }
        }
        break;
        //chDir
        case 0xc0:
            dataStack.push(file.changeDir(ramManager, dataStack.pop() & 0xffff) ? TRUE : FALSE);
            break;
        //fileList
        case 0xc1:
            dataStack.push(fileList());
            break;
        //getTime
        case 0xc2: {
            date.setTime(System.currentTimeMillis());
            cal.setTime(date);
            int addr = dataStack.pop() & 0xffff;
            ramManager.setBytes(addr, 2, cal.get(Calendar.YEAR));
            ramManager.setBytes(addr + 2, 1, cal.get(Calendar.MONTH));
            ramManager.setBytes(addr + 3, 1, cal.get(Calendar.DAY_OF_MONTH));
            ramManager.setBytes(addr + 4, 1, cal.get(Calendar.HOUR_OF_DAY));
            ramManager.setBytes(addr + 5, 1, cal.get(Calendar.MINUTE));
            ramManager.setBytes(addr + 6, 1, cal.get(Calendar.SECOND));
            ramManager.setBytes(addr + 7, 1, cal.get(Calendar.DAY_OF_WEEK));
        }
        break;
        //setTime
        case 0xc3:
            //忽略之
            dataStack.pop();
            break;
//                throw new IllegalStateException("不支持的函数: SetTime");
        //getWord
        case 0xc4: {
            int mode = dataStack.pop();
            char c;
            if (input == null) {
                c = key.getchar();
            } else {
                input.setMode(mode);
                c = input.getWord(key, screen);
            }
            dataStack.push(c);
        }
        break;
        //xDraw
        case 0xc5:
            render.xdraw(dataStack.pop());
            break;
        //releaseKey
        case 0xc6:
            key.releaseKey((char) dataStack.pop());
            break;
        //getBlock
        case 0xc7: {
            dataStack.movePointer(-6);
            render.setDrawMode(dataStack.peek(4));
            int addr = dataStack.peek(5) & 0xffff;
            int length = render.getRegion((short) dataStack.peek(0), (short) dataStack.peek(1),
                    (short) dataStack.peek(2), (short) dataStack.peek(3),
                    ramManager, addr);
        }
        break;
        case 0xc8: {
            int arc = (short) dataStack.pop();
            dataStack.push(Util.cos(arc));
        }
        break;
        case 0xc9: {
            int arc = (short) dataStack.pop();
            dataStack.push(Util.sin(arc));
        }
        break;
        case 0xca:
            throw new IllegalStateException("不支持的函数: FillArea");
        }
    }

    public GvmConfig getConfig() {
        return config;
    }

    private int fileList() throws InterruptedException {
        int addr = dataStack.pop() & 0xffff;
        int count = file.getFileNum();
        byte[][] encodes = new byte[count + 1][];
        String[] dirName = new String[1];
        encodes[0] = new byte[]{'.', '.'};
        for (int index = 0; index < count; index++) {
            file.listFiles(dirName, index, 1);
            try {
                encodes[index + 1] = dirName[0].getBytes("gb2312");
            } catch (UnsupportedEncodingException uee) {
                encodes[index + 1] = dirName[0].getBytes();
            }
        }
        GetableImp getter = new GetableImp();
        int maxRow = screen.getHeight() / 13;
        int first = 0, current = 0;
        for (; ; ) {
            //绘制文件名与反显条
            render.setDrawMode(Renderable.RENDER_FILL_TYPE |
                    Renderable.DRAW_CLEAR_TYPE |
                    Renderable.RENDER_GRAPH_TYPE);
            render.drawRect(0, 0, screen.getWidth(), screen.getHeight());
            render.setDrawMode(Renderable.DRAW_COPY_TYPE | Renderable.RENDER_GRAPH_TYPE);
            for (int row = 0; row < maxRow && row + first <= count; row++) {
                getter.setBuffer(encodes[row + first]);
                render.drawString(0, row * 13, getter, 0, encodes[row + first].length);
            }
            render.setDrawMode(Renderable.DRAW_NOT_TYPE |
                    Renderable.RENDER_FILL_TYPE |
                    Renderable.RENDER_GRAPH_TYPE);
            render.drawRect(0, 13 * current, screen.getWidth(), 13 * current + 12);
            //接受按键
            for (; ; ) {
                int keyValue = key.getRawKey();
                if (keyValue == keyInf.getEnter()) {
                    int index = 0;
                    while (index < encodes[first + current].length) {
                        ramManager.setByte(addr++, encodes[first + current][index++]);
                    }
                    ramManager.setByte(addr, (byte) 0);
                    return TRUE;
                }
                if (keyValue == keyInf.getEsc()) {
                    return FALSE;
                }
                if (keyValue == keyInf.getDown() || keyValue == keyInf.getRight()) {
                    if (first + current >= count) {
                        continue;
                    } else {
                        if (current < maxRow - 1) {
                            current++;
                        } else {
                            first++;
                        }
                        break;
                    }
                }
                if (keyValue == keyInf.getUp() || keyValue == keyInf.getLeft()) {
                    if (first + current == 0) {
                        continue;
                    } else {
                        if (current > 0) {
                            current--;
                        } else {
                            first--;
                        }
                        break;
                    }
                }
            }
        }
    }

    private class GetableImp implements Getable {

        private byte[] buf;

        public void setBuffer(byte[] buf) {
            this.buf = buf;
        }

        public byte getByte(int addr) throws IndexOutOfBoundsException {
            return buf[addr];
        }
    }

    private void sprintf() {
        int paramCount = dataStack.pop() & 0xff;
        //弹出参数
        dataStack.movePointer(-paramCount);
        int index = 0;
        //保存字符串的地址
        int data = dataStack.peek(index++) & 0xffff;
        //格式化字符串起始地址
        int addr = dataStack.peek(index++) & 0xffff;
        byte fstr, t, b;
        while ((fstr = ramManager.getByte(addr++)) != 0) {
            if (fstr == 0x25) {
                //%
                t = ramManager.getByte(addr++);
                if (t == 0) {
                    break;
                }
                switch (t) {
                //%d
                case 0x64: {
                    byte[] array = Util.intToGB(dataStack.peek(index++));
                    for (int k = 0; k < array.length; k++) {
                        ramManager.setByte(data++, array[k]);
                    }
                }
                break;
                //%c
                case 0x63:
                    ramManager.setByte(data++, (byte) dataStack.peek(index++));
                    break;
                //%s
                case 0x73: {
                    int strAddr = dataStack.peek(index++) & 0xffff;
                    while ((b = ramManager.getByte(strAddr++)) != 0) {
                        ramManager.setByte(data++, b);
                    }
                }
                break;
                default:
                    ramManager.setByte(data++, t);
                    break;
                }//switch

            } else {
                ramManager.setByte(data++, fstr);
            }
        }
        ramManager.setByte(data, (byte) 0);
    }

    private void printf() {
        int paramCount = dataStack.pop() & 0xff;
        //弹出参数
        dataStack.movePointer(-paramCount);
        int index = 0;
        //格式化字符串起始地址
        int addr = dataStack.peek(index++) & 0xffff;
        byte fstr, data, b;
        char c;
        while ((fstr = ramManager.getByte(addr++)) != 0) {
            if (fstr == 0x25) {
                //%
                data = ramManager.getByte(addr++);
                if (data == 0) {
                    break;
                }
                switch (data) {
                //%d
                case 0x64: {
                    byte[] array = Util.intToGB(dataStack.peek(index++));
                    for (int k = 0; k < array.length; k++) {
                        text.addChar((char) array[k]);
                    }
                }
                break;
                //%c
                case 0x63:
                    text.addChar((char) (dataStack.peek(index++) & 0xff));
                    break;
                //%s
                case 0x73: {
                    int strAddr = dataStack.peek(index++) & 0xffff;
                    while ((b = ramManager.getByte(strAddr++)) != 0) {
                        if (b >= 0) {
                            text.addChar((char) b);
                        } else {
                            c = (char) (b & 0xff);
                            b = ramManager.getByte(strAddr++);
                            if (b == 0) {
                                text.addChar(c);
                                break;
                            }
                            c |= b << 8;
                            text.addChar(c);
                        }
                    }
                }
                break;
                default:
                    text.addChar((char) (data & 0xff));
                    break;
                }//switch

            } else if (fstr > 0) {
                text.addChar((char) fstr);
            } else {
                b = ramManager.getByte(addr++);
                if (b == 0) {
                    text.addChar((char) (fstr & 0xff));
                    break;
                }
                c = (char) ((fstr & 0xff) | (b << 8));
                text.addChar(c);
            }
        }
        text.updateLCD(0);
    }

}




















