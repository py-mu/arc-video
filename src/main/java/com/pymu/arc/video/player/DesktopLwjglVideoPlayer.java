package com.pymu.arc.video.player;

import arc.audio.Music;
import com.badlogic.gdx.video.VideoPlayer;
import com.pymu.arc.video.decoder.VideoDecoder;

import java.nio.ByteBuffer;

public class DesktopLwjglVideoPlayer extends BaseVideoVideoPlayer{

    /**
     * 从 VideoDecoder 实例中检索其音频
     *
     * @param decoder       解码器
     * @param audioBuffer   音频流
     * @param audioChannels 通道
     * @param sampleRate    采样率
     * @return 音乐
     */
    @Override
    Music getMusic(VideoDecoder decoder, ByteBuffer audioBuffer, int audioChannels, int sampleRate) {
        return null;
    }
}
