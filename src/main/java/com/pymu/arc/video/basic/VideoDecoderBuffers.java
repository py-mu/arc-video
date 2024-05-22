package com.pymu.arc.video.basic;

import java.nio.ByteBuffer;

/**
 *
 */
public class VideoDecoderBuffers {
    private final ByteBuffer videoBuffer;
    private final ByteBuffer audioBuffer;
    private final int videoBufferWidth;
    private final int videoWidth;
    private final int videoHeight;
    private final int audioChannels;
    private final int audioSampleRate;

    // If constructor parameters are changed, please also update the native code to call the new constructor!
    private VideoDecoderBuffers(ByteBuffer videoBuffer, ByteBuffer audioBuffer, int videoBufferWidth, int videoWidth,
                                int videoHeight, int audioChannels, int audioSampleRate) {
        this.videoBuffer = videoBuffer;
        this.audioBuffer = audioBuffer;
        this.videoBufferWidth = videoBufferWidth;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.audioChannels = audioChannels;
        this.audioSampleRate = audioSampleRate;
    }

    /**
     * @return The audiobuffer
     */
    public ByteBuffer getAudioBuffer() {
        return audioBuffer;
    }

    /**
     * @return The videobuffer
     */
    public ByteBuffer getVideoBuffer() {
        return videoBuffer;
    }

    /**
     * @return The amount of audio channels
     */
    public int getAudioChannels() {
        return audioChannels;
    }

    /**
     * @return The audio's samplerate
     */
    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    /**
     * @return The number of pixels per row in the decoding buffer, may be larger than the video width
     */
    public int getVideoBufferWidth() {
        return videoBufferWidth;
    }

    /**
     * @return The height of the video
     */
    public int getVideoHeight() {
        return videoHeight;
    }

    /**
     * @return The width of the video
     */
    public int getVideoWidth() {
        return videoWidth;
    }
}
