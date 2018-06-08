package eastsun.jgvm.module;

import eastsun.jgvm.util.IOUtil;
import java.io.IOException;
import java.io.InputStream;

/**
 * 封装一个可执行的lav程序数据,其内部维持一个文件指针
 * @author Eastsun
 */
public final class LavApp {

    private final String name;
    private byte[] appData;
    private int offset;

    /**
     * 通过一个输入流创建一个LavApp对象
     * @throws java.lang.IllegalArgumentException 发生IO错误或数据格式不正确
     */
    public static LavApp createLavApp(String name, InputStream in) throws IllegalArgumentException, IOException {
        return new LavApp(name, in);
    }

    private LavApp(String name, InputStream in) throws IllegalArgumentException, IOException {
        this.name = name;
        this.appData = IOUtil.readAll(in);
        verifyData();
        reset();
    }

    public String getName() {
        return name;
    }

    /**
     * lav程序数据大小(字节数)
     * @return size 这个lav程序数据的总大小,含文件头
     */
    public final int size() {
        return appData.length;
    }

    /**
     * 在pointer处读取一字节无符号整数,并使pointer加一<p>
     */
    public final int getUint8() {
        return appData[offset++] & 0xff;
    }

    /**
     * 从app中读取两字节有符号整数,对应lav中的int类型
     * @return int
     */
    public final short getInt16() {
        return (short) (appData[offset++] & 0xff | (appData[offset++] & 0xff) << 8);
    }

    public final int getUint16() {
        return appData[offset++] & 0xff | (appData[offset++] & 0xff) << 8;
    }

    /**
     * 从app中读取三字节数据(无符号),对应lav中文件指针数据
     */
    public final int getUint24() {
        return appData[offset++] & 0xff | (appData[offset++] & 0xff) << 8 | (appData[offset++] & 0xff) << 16;
    }

    /**
     * 从app中读取四字节数据,对应lav中的long类型
     */
    public final int getInt32() {
        return appData[offset++] & 0xff | (appData[offset++] & 0xff) << 8 | (appData[offset++] & 0xff) << 16 | (appData[offset++] & 0xff) << 24;
    }

    /**
     * 得到当前数据偏移量
     * @return pointer 下次读取时的位置
     */
    public final int getOffset() {
        return offset;
    }

    /** 设置读取偏移量
     * @param pos 偏移量,下次读取数据时开始位置
     */
    public final void setOffset(int pos) {
        offset = pos;
    }

    /**
     * 复位数据指针
     */
    public final void reset() {
        offset = 16;
    }

    /**
     * 检查数据格式并设置相应参数
     * @throws java.lang.IllegalArgumentException 不正确的lava格式
     */
    private void verifyData() throws IllegalArgumentException {
        if (appData.length <= 16) {
            throw new IllegalArgumentException("不是有效的LAV文件");
        }
        if (appData[0] != 0x4c || appData[1] != 0x41 || appData[2] != 0x56) {
            throw new IllegalArgumentException("不是有效的LAV文件");
        }
        if (appData[3] != 0x12) {
            throw new IllegalArgumentException("不支持的LAV文件版本:" + Integer.toString(appData[3] & 0xff, 16));
        }
    }
}
