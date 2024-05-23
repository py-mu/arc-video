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
import arc.struct.Seq;


public class ArcApplication extends ApplicationCore {
    public static TextureRegionDrawable whiteui;
    public static Font def;
    private static final String mainFont = "fonts/font.woff";
    public static ScaledNinePatchDrawable windowEmpty;

    private static final ObjectSet<String> unscaled = ObjectSet.with("iconLarge");

    /**
     * 在setup 阶段需要把启动的必要资源准备完毕
     */
    @Override
    public void setup() {
        Core.assets = new AssetManager();
        Core.batch = new SortedSpriteBatch();
        Core.scene = new Scene();
        Core.assets.load(new AssetDescriptor<>("sprites/sprites.aatls", TextureAtlas.class)).loaded = t -> Core.atlas = t;
        loadDefaultFont();
        add(new TestUi());

    }

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

    @Override
    public void add(ApplicationListener module) {
        super.add(module);

        //autoload modules when necessary
        if (module instanceof Loadable l) {
            Core.assets.load(l);
        }
    }

    @Override
    public void update() {
        super.update();
        try {
            Core.assets.update();
        } catch (Exception ignored) {
        }
    }

    public static class TestUi implements ApplicationListener, Loadable {
        Dialog dialog;


        @Override
        public void loadSync() {
            def.getData().markupEnabled = true;
            def.setOwnsTexture(true);
            Core.assets.getAll(Font.class, new Seq<>()).each(font -> font.setUseIntegerPositions(true));
            Core.input.addProcessor(Core.scene);
            int[] insets = Core.graphics.getSafeInsets();
            Core.scene.marginLeft = insets[0];
            Core.scene.marginRight = insets[1];
            Core.scene.marginTop = insets[2];
            Core.scene.marginBottom = insets[3];
            loadStyle();
            dialog = new Dialog("arc-video");
            dialog.add(dialog);
            Core.scene.add(dialog);
        }

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

        @Override
        public void update() {
            Core.scene.act();
            Core.scene.draw();
            if (dialog != null) {
                dialog.show();
            }
            if (Core.input.keyTap(KeyCode.mouseLeft) && Core.scene.hasField()) {
                Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (!(e instanceof TextField)) {
                    Core.scene.setKeyboardFocus(null);
                }
            }
        }
    }
}
