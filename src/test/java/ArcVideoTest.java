import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.files.Fi;
import com.pymu.arc.video.basic.LibraryLoader;
import com.pymu.arc.video.basic.VideoPlayer;
import com.pymu.arc.video.decoder.VideoDecoder;
import com.pymu.arc.video.player.VideoPlayerFactory;
import org.junit.Assert;
import org.junit.Test;

public class ArcVideoTest {


    /**
     * 测试解码器有没有调用C++的代码正常获取到指针
     */
    @Test
    public void testDecoderInitPointer() {
        LibraryLoader.loadLibraries();
        VideoDecoder decoder = new VideoDecoder();
        long pointer = decoder.getNativePointer();
        System.out.printf(String.valueOf(pointer));
        Assert.assertNotEquals(pointer, 0L);
    }

    /**
     * 测试Arc框架能否启动
     */
    @Test
    public void testArcWindow() {
        new SdlApplication(new ArcApplication(), new SdlConfig() {{
            title = "test";
            maximized = true;
            width = 900;
            height = 700;
        }});
    }

    /**
     * 检查文件路径配置
     */
    @Test
    public void testMp4Exists(){
        Assert.assertTrue(new Fi("src/test/resources/test.webm").exists());
    }
}
