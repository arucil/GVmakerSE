package eastsun.jgvm.module;

import eastsun.jgvm.module.event.Area;
import eastsun.jgvm.module.event.ScreenChangeListener;
import eastsun.jgvm.module.ram.RelativeRam;

/**
 * 屏幕模块,该模块保留对显存及缓冲区访问的可选实现
 * @version 0.7 2007/1/21  修改了外部得到屏幕内容的接口<p>
 *               2008/2/24  再次修改获得屏幕内容的接口,主要目的是优化刷屏速度
 * @author Eastsun
 */
public abstract class ScreenModel {

    /**
     * 屏幕宽度
     */
    public static final int WIDTH = 160;
    /**
     * 屏幕高度
     */
    public static final int HEIGHT = 80;

    /**
     * 创建一个ScreenModel实例
     */
    public static ScreenModel newScreenModel() {
        return new DefaultScreenModel();
    }

    protected ScreenModel() {
    }

    /**
     * 得到屏幕的宽度
     * @return width
     */
    public final int getWidth() {
        return WIDTH;
    }

    /**
     * 得到屏幕的高度
     * @return height
     */
    public final int getHeight() {
        return HEIGHT;
    }

    /**
     * 是否有相关联的显存以及显存缓冲区Ram
     * @return 当且仅当屏幕大小为160*80时返回true
     */
    public abstract boolean hasRelativeRam();

    /**
     * 得到与该屏幕显存相关联的Ram,可以将其安装到RamManager中,以使得LAVA程序能够直接访问显存
     * @return ram 得到关联的Ram,该Ram的内容与Screen内容保持同步变化
     * @throws IllegalStateException 如果hasRelativeRam()返回false
     * @see #hasRelativeRam()
     * @see RamManager#install(Ram)
     */
    public abstract RelativeRam getGraphRam();

    /**
     * 得到与屏幕缓冲区相关联的Ram,可以将其安装到RamManager中,以使得LAVA程序能够直接访问屏幕缓冲区
     * @return ram 得到关联的Ram,该Ram的内容与Screen缓冲区内容保持同步变化
     * @throws IllegalStateException 如果hasRelativeRam()返回false
     * @see #hasRelativeRam()
     * @see RamManager#install(Ram)
     */
    public abstract RelativeRam getBufferRam();

    /**
     * 获得屏幕绘图接口
     * @return render
     */
    public abstract Renderable getRender();

}
