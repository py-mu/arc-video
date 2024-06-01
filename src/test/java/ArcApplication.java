import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.files.Fi;
import arc.freetype.FreeTypeFontGenerator;
import arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import arc.freetype.FreeTypeFontGeneratorLoader;
import arc.freetype.FreetypeFontLoader;
import arc.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import arc.graphics.Camera;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.SortedSpriteBatch;
import arc.graphics.g2d.TextureAtlas;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.style.ScaledNinePatchDrawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectSet;
import arc.util.Align;
import com.pymu.arc.video.basic.VideoPlayer;
import com.pymu.arc.video.ui.VideoViewer;


public class ArcApplication extends ApplicationCore {
    public static TextureRegionDrawable whiteui;
    public static Font def;
    private static final String mainFont = "fonts/font.woff";
    public static ScaledNinePatchDrawable windowEmpty;
    static Dialog dialog;

    private static final ObjectSet<String> unscaled = ObjectSet.with("iconLarge");

    /**
     * 在setup 阶段需要把启动的必要资源准备完毕
     */
    @Override
    public void setup() {
        Core.assets = new AssetManager();
        Core.batch = new SortedSpriteBatch();
        Core.scene = new Scene();
        Core.camera = new Camera();
        // 加载贴图资源
        Core.assets.load(new AssetDescriptor<>("sprites/sprites.aatls", TextureAtlas.class)).loaded = t -> Core.atlas = t;
        loadDefaultFont();
        add(new TestUi());

    }

    /**
     * 初始化一个默认的字体
     */
    public void loadDefaultFont() {
        Core.assets.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(Core.files::internal));
        Core.assets.setLoader(Font.class, null, new FreetypeFontLoader(Core.files::internal) {
            final ObjectSet<FreeTypeFontParameter> scaled = new ObjectSet<>();

            @Override
            public Font loadSync(AssetManager manager, String fileName, Fi file, FreeTypeFontLoaderParameter parameter) {
                if (fileName.equals("outline")) {
                    parameter.fontParameters.borderWidth = Scl.scl(2f);
                    parameter.fontParameters.spaceX -= (int) parameter.fontParameters.borderWidth;
                }

                if (!scaled.contains(parameter.fontParameters) && !unscaled.contains(fileName)) {
                    parameter.fontParameters.size = (int) (Scl.scl(parameter.fontParameters.size));
                    scaled.add(parameter.fontParameters);
                }

                parameter.fontParameters.magFilter = Texture.TextureFilter.linear;
                parameter.fontParameters.minFilter = Texture.TextureFilter.linear;
                return super.loadSync(manager, fileName, file, parameter);
            }
        });
        Core.assets.load("default", Font.class, new FreeTypeFontLoaderParameter(mainFont, getFreeTypeFontParameter())).loaded = f -> def = f;
    }


    /**
     * @return font 参数
     */
    private FreeTypeFontParameter getFreeTypeFontParameter() {
        return new FreeTypeFontParameter() {{
            size = 18;
            shadowColor = Color.darkGray;
            shadowOffsetY = 2;
            incremental = true;
        }};
    }

    /**
     * 给监听器装载模块，如果是一个装载对象还需要加入到异步加载队列中
     *
     * @param module loadable
     */
    @Override
    public void add(ApplicationListener module) {
        super.add(module);

        //autoload modules when necessary
        if (module instanceof Loadable l) {
            Core.assets.load(l);
        }
    }

    /**
     * 事件循环
     */
    @Override
    public void update() {
        super.update();
        try {
            // 初始化队列(执行异步 load)，不然你的资源无法加载
            Core.assets.update();
        } catch (Exception ignored) {
        }
    }

    public static class TestUi implements ApplicationListener, Loadable {
        VideoPlayer player;

        /**
         * UI被装载器加载时，可以初始化内容了，比如设置页面
         */
        @Override
        public void loadSync() {
            loadStyle();
            dialog = new Dialog("arc-video") {{
                // 设置全屏显示
                setFillParent(true);
                // 设置页面标题居中
                title.setAlignment(Align.center);
            }};
            dialog.add(new VideoViewer(new Fi("src/test/resources/test.webm")));
            // 往屏幕中添加dialog
            Core.scene.root.addChild(dialog);
//            player = VideoPlayerFactory.createVideoPlayer();
//            try {
//                player.play();
//                player.setLooping(true);
//
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            }
            dialog.show();
        }

        /**
         * 初始化dialog默认样式， 用于创建一个初始窗口
         */
        public void loadStyle() {
            whiteui = (TextureRegionDrawable) Core.atlas.drawable("whiteui");
            windowEmpty = (ScaledNinePatchDrawable) Core.atlas.drawable("window-empty");

            Dialog.DialogStyle defaultDialog = new Dialog.DialogStyle() {{
                background = whiteui.tint(0f, 0f, 0f, 0.9f);
                titleFont = def;
                stageBackground = windowEmpty;
            }};
            arc.Core.scene.addStyle(Dialog.DialogStyle.class, defaultDialog);
        }

        /**
         * 渲染的事件循环，
         */
        @Override
        public void update() {
            Core.scene.act();

            Core.scene.draw();
            if (Core.input.keyTap(KeyCode.mouseLeft) && Core.scene.hasField()) {
                Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (!(e instanceof TextField)) {
                    Core.scene.setKeyboardFocus(null);
                }
            }
            if (player != null && player.isBuffered()) {
                player.update();
            }
        }
    }
}
