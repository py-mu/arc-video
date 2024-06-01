package com.pymu.arc.video.player;


import arc.audio.Music;
import arc.files.Fi;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import com.badlogic.gdx.utils.Null;
import com.pymu.arc.video.basic.LibraryLoader;
import com.pymu.arc.video.basic.VideoDecoderBuffers;
import com.pymu.arc.video.basic.VideoPlayer;
import com.pymu.arc.video.decoder.VideoDecoder;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class BaseVideoVideoPlayer extends Element implements VideoPlayer {
    /**
     * 视频解码器
     */
    protected VideoDecoder decoder;
    /**
     * 视频每一帧的纹理
     */
    protected Texture texture;
    /**
     * 视频的音频文件
     */
    protected Music audio;
    /**
     * 开始时间
     */
    protected long startTime = 0;
    /**
     * 显示已经解码的帧
     */
    protected boolean showAlreadyDecodedFrame = false;

    /**
     * 暂停状态？
     */
    protected boolean paused = false;
    /**
     * 循环播放？
     */
    protected boolean looping = false;
    /**
     * 是否是第一帧？
     */
    protected boolean isFirstFrame = true;
    /**
     * 暂停时的，已经播放的时间
     */
    protected long timeBeforePause = 0;

    /**
     * 视频宽高
     */
    protected int currentVideoWidth, currentVideoHeight;
    /**
     * 视频帧长度
     */
    protected int videoBufferWidth;
    /**
     * 视频播放的监听器
     */
    protected VideoSizeListener sizeListener;
    /**
     * 视频播放结束监听器
     */
    protected VideoCompletionListener videoCompletionListener;
    /**
     * 当前播放的文件
     */
    protected Fi currentFile;

    /**
     * 输入流
     */
    protected BufferedInputStream inputStream;
    /**
     * 读取的字节通道
     */
    protected ReadableByteChannel fileChannel;

    /**
     * 视频是否在播放
     */
    protected boolean playing = false;
    /**
     * 视频显示纹理区域
     */
    protected final TextureRegion region = new TextureRegion();

    protected Texture.TextureFilter minFilter = Texture.TextureFilter.linear;
    protected Texture.TextureFilter magFilter = Texture.TextureFilter.linear;


    public void setFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {
        if (this.minFilter == minFilter && this.magFilter == magFilter) return;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        Texture texture = getTexture();
        if (texture == null) return;
        texture.setFilter(minFilter, magFilter);
    }

    /**
     * 从 VideoDecoder 实例中检索其音频，当前的方法是给视频设置音频( TODO 无法实现音频转码)
     *
     * @param decoder       解码器
     * @param audioBuffer   音频流
     * @param audioChannels 通道
     * @param sampleRate    采样率
     * @return 音乐
     */
    Music getMusic(VideoDecoder decoder, ByteBuffer audioBuffer, int audioChannels, int sampleRate){
        return null;
    }

    void setMusic(Music music){
        audio = music;
    }

    /**
     * 获取视频宽度
     *
     * @return 宽度
     */
    protected int getTextureWidth() {
        return videoBufferWidth;
    }

    /**
     * 获取视频高度
     *
     * @return 高度
     */
    protected int getTextureHeight() {
        return currentVideoHeight;
    }


    public boolean play(Fi file) throws FileNotFoundException {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file: " + file.path());
        }
        if (!LibraryLoader.isLoaded()) {
            LibraryLoader.loadLibraries();
            LibraryLoader.setDebugLogging(true);
        }
        if (decoder != null) {
            // Do all the cleanup
            stop();
        }
        currentFile = file;
        inputStream = file.read(256 * 1024);
        fileChannel = Channels.newChannel(inputStream);

        isFirstFrame = true;
        decoder = new VideoDecoder();
        VideoDecoderBuffers buffers;
        try {
            buffers = decoder.loadStream(this::readFileContents);
            if (buffers != null) {
                ByteBuffer audioBuffer = buffers.getAudioBuffer();
                if (audioBuffer != null) {
                    if (audio != null) audio.dispose();
                    audio = getMusic(decoder, audioBuffer, buffers.getAudioChannels(), buffers.getAudioSampleRate());
                }
                currentVideoWidth = buffers.getVideoWidth();
                currentVideoHeight = buffers.getVideoHeight();
                videoBufferWidth = buffers.getVideoBufferWidth();
                if (texture != null && (texture.width != getTextureWidth() || texture.height != getTextureHeight())) {
                    texture.dispose();
                    texture = null;
                }
            } else {
                return false;
            }
        } catch (Exception ignore) {
            return false;
        }

        if (sizeListener != null) {
            sizeListener.onVideoSize(currentVideoWidth, currentVideoHeight);
        }

        playing = true;
        return true;
    }

    /**
     * Called by jni to fill in the file buffer.
     *
     * @param buffer The buffer that needs to be filled
     * @return The amount that has been filled into the buffer.
     */
    protected int readFileContents(ByteBuffer buffer) {
        try {
            buffer.rewind();
            return fileChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean update() {
        if (decoder != null && (!paused || isFirstFrame) && playing) {
            if (!paused && startTime == 0) {
                // Since startTime is 0, this means that we should now display the first frame of the video, and set the
                // time.
                startTime = System.currentTimeMillis();
                if (audio != null) {
                    audio.play();
                }
            }

            boolean newFrame = false;
            if (!showAlreadyDecodedFrame) {
                ByteBuffer videoData = decoder.nextVideoFrame();
                if (videoData != null) {
                    if (texture == null) {
                        texture = new Texture(getTextureWidth(), getTextureHeight());
                        texture.setFilter(minFilter, magFilter);
                    }
                    texture.bind();
                    Gl.texImage2D(Gl.texture2d, 0, Gl.rgb, getTextureWidth(), getTextureHeight(), 0, Gl.rgb, Gl.unsignedByte, videoData);
                    newFrame = true;
                } else if (isFirstFrame) {
                    return false;
                } else if (looping) {
                    try {
                        // NOTE: this just creates a new decoder instead of reusing the existing one.
                        float volume = getVolume();
                        play(currentFile);
                        setVolume(volume);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return false;
                } else {
                    playing = false;
                    if (videoCompletionListener != null) {
                        videoCompletionListener.onCompletionListener(currentFile);
                    }
                    return false;
                }
            }
            renderVideoFrame();
            isFirstFrame = false;
            long currentVideoTime = System.currentTimeMillis() - startTime;
            long millisecondsAhead = (long) getCurrentTimestamp() - currentVideoTime;
            showAlreadyDecodedFrame = millisecondsAhead > 20;
            return newFrame;
        }
        return false;
    }

    public void renderVideoFrame() {
        // 渲染视频帧
        region.set(texture);
        float pz = Draw.z();
        Draw.z(999);
        Draw.color(Color.valueOf("ffffff"), 1f);
        Draw.blend(Blending.additive);
        Draw.rect(region, 100, 100, 0f);
        Draw.blend();
        Draw.color();
        Draw.z(999);
    }


    @Null
    public Texture getTexture() {
        return texture;
    }

    /**
     * Will return whether the buffer is filled. At the time of writing, the buffer used can store 10 frames of video. You can
     * find the value in jni/VideoDecoder.h
     *
     * @return whether buffer is filled.
     */

    public boolean isBuffered() {
        if (decoder != null) {
            return decoder.isBuffered();
        }
        return false;
    }


    public void stop() {
        playing = false;

        if (audio != null) {
            audio.dispose();
            audio = null;
        }
        if (decoder != null) {
            decoder.dispose();
            decoder = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            }
            inputStream = null;
        }

        startTime = 0;
        showAlreadyDecodedFrame = false;
        isFirstFrame = true;
    }


    public void pause() {
        if (!paused) {
            paused = true;
            if (audio != null) {
                audio.pause(true);
            }
            if (startTime != 0L) {
                timeBeforePause = System.currentTimeMillis() - startTime;
            } else {
                timeBeforePause = 0L;
            }
        }
    }


    public void resume() {
        if (paused) {
            paused = false;
            if (audio != null) {
                audio.play();
            }
            startTime = System.currentTimeMillis() - timeBeforePause;
        }
    }


    public void dispose() {
        stop();
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }


    public void setOnVideoSizeListener(VideoSizeListener listener) {
        sizeListener = listener;
    }


    public void setOnCompletionListener(VideoCompletionListener listener) {
        videoCompletionListener = listener;
    }


    public int getVideoWidth() {
        return currentVideoWidth;
    }


    public int getVideoHeight() {
        return currentVideoHeight;
    }


    public boolean isPlaying() {
        return playing;
    }


    public void setVolume(float volume) {
        if (audio != null) audio.setVolume(volume);
    }


    public float getVolume() {
        if (audio == null) return 0;
        return audio.getVolume();
    }


    public void setLooping(boolean looping) {
        this.looping = looping;
    }


    public boolean isLooping() {
        return looping;
    }


    public int getCurrentTimestamp() {
        return (int) (decoder.getCurrentFrameTimestamp() * 1000);
    }

}
