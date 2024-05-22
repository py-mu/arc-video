package com.pymu.arc.video.player;


import arc.Core;
import arc.audio.Music;
import arc.files.Fi;
import arc.graphics.Texture;
import com.badlogic.gdx.utils.Null;
import com.pymu.arc.video.basic.AbstractVideoVideoPlayer;
import com.pymu.arc.video.basic.LibraryLoader;
import com.pymu.arc.video.basic.VideoPlayerInterface;
import com.pymu.arc.video.basic.VideoDecoderBuffers;
import com.pymu.arc.video.decoder.VideoDecoder;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class BaseVideoVideoPlayer extends AbstractVideoVideoPlayer implements VideoPlayerInterface {
    protected VideoDecoder decoder;
    protected Texture texture;
    protected Music audio;
    protected long startTime = 0;
    protected boolean showAlreadyDecodedFrame = false;

    protected boolean paused = false;
    protected boolean looping = false;
    protected boolean isFirstFrame = true;
    protected long timeBeforePause = 0;

    protected int currentVideoWidth, currentVideoHeight;
    protected int videoBufferWidth;
    protected VideoSizeListener sizeListener;
    protected CompletionListener completionListener;
    protected Fi currentFile;

    protected BufferedInputStream inputStream;
    protected ReadableByteChannel fileChannel;

    protected boolean playing = false;

    /**
     * @param decoder       解码器
     * @param audioBuffer   音频流
     * @param audioChannels 通道
     * @param sampleRate    采样率
     * @return 音乐
     */
    abstract Music createMusic(VideoDecoder decoder, ByteBuffer audioBuffer, int audioChannels, int sampleRate);

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
                    audio = createMusic(decoder, audioBuffer, buffers.getAudioChannels(), buffers.getAudioSampleRate());
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
                    Core.gl.glTexImage2D(Core.gl20.GL_TEXTURE_2D, 0, Core.gl20.GL_RGB, getTextureWidth(), getTextureHeight(), 0, Core.gl20.GL_RGB,
                            Core.gl20.GL_UNSIGNED_BYTE, videoData);
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
                    if (completionListener != null) {
                        completionListener.onCompletionListener(currentFile);
                    }
                    return false;
                }
            }

            isFirstFrame = false;
            long currentVideoTime = System.currentTimeMillis() - startTime;
            long millisecondsAhead = (long) getCurrentTimestamp() - currentVideoTime;
            showAlreadyDecodedFrame = millisecondsAhead > 20;
            return newFrame;
        }
        return false;
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


    public void setOnCompletionListener(CompletionListener listener) {
        completionListener = listener;
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
