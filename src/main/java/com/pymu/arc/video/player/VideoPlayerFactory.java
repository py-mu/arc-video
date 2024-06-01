package com.pymu.arc.video.player;

import arc.Application.ApplicationType;
import arc.Core;
import com.pymu.arc.video.basic.VideoPlayer;

import java.lang.reflect.InvocationTargetException;

/**
 * 视频播放窗口的一个抽象工厂
 */
public class VideoPlayerFactory {

    /**
     * 根据当前环境构建一个video player
     *
     * @return player
     */
    public static VideoPlayer createVideoPlayer() {
        return createVideoPlayer(true);
    }

    public static VideoPlayer createVideoPlayer(boolean useLwjgl3) {
        Class<? extends VideoPlayer> videoPlayerClass;
        if (useLwjgl3 && Core.app.getType() == ApplicationType.desktop) {
            videoPlayerClass = DesktopLwjgl3VideoPlayer.class;
        } else {
            videoPlayerClass = getCurrentOsVideoPlayerClass();
        }
        return createVideoPlayer(videoPlayerClass);
    }

    /**
     * 通过类构建一个player
     * @param videoPlayerClass player class
     * @return player
     */
    public static VideoPlayer createVideoPlayer(Class<? extends VideoPlayer> videoPlayerClass) {
        try {
            return videoPlayerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取当前系统版本对应的播放器类型
     *
     * @return class
     */
    public static Class<? extends VideoPlayer> getCurrentOsVideoPlayerClass() {
        ApplicationType osType = Core.app.getType();
        switch (osType) {
            case desktop -> {
                return DesktopLwjgl3VideoPlayer.class;
            }
            case web, iOS, headless, android -> {
                return EmptyVideoVideoPlayer.class;
            }
        }
        return EmptyVideoVideoPlayer.class;
    }
}
