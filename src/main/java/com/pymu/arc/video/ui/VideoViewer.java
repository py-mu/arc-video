package com.pymu.arc.video.ui;

import arc.files.Fi;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.util.Align;
import com.pymu.arc.video.player.DesktopLwjgl3VideoPlayer;

import java.io.FileNotFoundException;

/**
 * 尝试将视频作为组件的形式
 */
public class VideoViewer extends DesktopLwjgl3VideoPlayer {
    float componentWidth;
    float componentHeight;
    float videoAspectRatio;
    float scaleRatio;

    public VideoViewer(Fi file) {
        looping = true;
        try {
            play(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        componentWidth = getWidth();
        componentHeight = getHeight();
        videoAspectRatio = (float) getVideoWidth() / (float) getVideoHeight();

        if (componentWidth / componentHeight > videoAspectRatio) {
            // 以高度为基准进行缩放
            scaleRatio = componentHeight / getVideoHeight();
        } else {
            // 以宽度为基准进行缩放
            scaleRatio = componentWidth / getVideoWidth();
        }
    }

    @Override
    public void draw() {
        super.draw();
        update();
    }

    @Override
    public void renderVideoFrame() {
        region.set(texture);
//        float pz = Draw.z();
        Draw.color(Color.valueOf("ffffff"), 1f);
        Draw.blend(Blending.additive);
        Draw.rect(region, 100,100,
                getVideoWidth()/2f,
                getVideoHeight()/2f,
               0f);
        Draw.blend();
        Draw.color();
    }
}
