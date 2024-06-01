package com.pymu.arc.video.basic;

import arc.files.Fi;
import arc.graphics.Texture;
import arc.util.Disposable;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Null;

import java.io.FileNotFoundException;

/**
 * copy form com.badlogic.gdx.video VideoPlayer
 * make arc video player
 */
public interface VideoPlayer extends Disposable {

    interface VideoSizeListener {
        void onVideoSize(float width, float height);
    }

    /**
     * 视频播放完成
     */
    interface VideoCompletionListener {
        void onCompletionListener(Fi file);
    }

    /**
     * 视频第一次播放（或者是从头播放）
     */
    interface VideoPlayListener {
        void onFirstPlay(Fi file);
    }

    interface AudioCompletionListener {
        void onCompletion(Music music);
    }

    /**
     * 此函数将使VideoPlayer准备播放给定的文件。如果视频已经播放，它将被停止，新的视频将被加载。视频一加载就开始播放。
     * This function will prepare the VideoPlayer to play the given file. If a video is already played, it will be stopped, and
     * the new video will be loaded. The video starts playing as soon as it is loaded.
     *
     * @param file The file containing the video which should be played.
     * @return Whether loading the file was successful.
     * @throws FileNotFoundException if the file does not exist
     */
    boolean play(Fi file) throws FileNotFoundException;

    /**
     * 这个函数需要每帧调用一次，这样播放器就可以更新所有的缓冲区，你就可以绘制帧了。正常的用例是在 isBuffered() 返回true后开始渲染。
     * This function needs to be called every frame, so that the player can update all the buffers and you can draw the frame.
     * Normal use case is to start rendering after {@link #isBuffered()} returns true.
     *
     * @return if a new frame is available as texture
     */
    boolean update();

    /**
     * 渲染视频帧
     */
    void renderVideoFrame();

    /**
     * @return The current video frame. Null if video was not started yet
     */
    @Null
    Texture getTexture();

    /**
     * Whether the buffer containing the video is completely filled. The size of the buffer is platform specific, and cannot
     * necessarily be depended upon. Review the documentation per platform for specifics.
     *
     * @return buffer completely filled or not.
     */
    boolean isBuffered();

    /**
     * This pauses the video, and should be called when the app is paused, to prevent the video from playing while being swapped
     * away.
     */
    void pause();

    /**
     * This resumes the video after it is paused.
     */
    void resume();

    /**
     * This will stop playing the file, and implicitly clears all buffers and invalidate resources used.
     */
    void stop();

    /**
     * This will set a listener for whenever the video size of a file is known (after calling play). This is needed since the size
     * of the video is not directly known after using the play method.
     * <p>
     * This may be called on any thread, so it is not safe to call other VideoPlayer functions directly.
     *
     * @param listener The listener to set
     */
    void setOnVideoSizeListener(VideoSizeListener listener);

    /**
     * This will set a listener for when the video is done playing. The listener will be called every time a video is done
     * playing.
     * <p>
     * This may be called on any thread, so it is not safe to call other VideoPlayer functions directly.
     *
     * @param listener The listener to set
     */
    void setOnCompletionListener(VideoCompletionListener listener);

    /**
     * This will return the width of the currently playing video.
     * <p>
     * This function cannot be called until the {@link VideoSizeListener} has been called for the currently playing video. If this
     * callback has not been set, a good alternative is to wait until the {@link #isBuffered} function returns true, which
     * guarantees the availability of the videoSize.
     *
     * @return the width of the video
     */
    int getVideoWidth();

    /**
     * This will return the height of the currently playing video.
     * <p>
     * This function cannot be called until the {@link VideoSizeListener} has been called for the currently playing video. If this
     * callback has not been set, a good alternative is to wait until the {@link #isBuffered} function returns true, which
     * guarantees the availability of the videoSize.
     *
     * @return the height of the video
     */
    int getVideoHeight();

    /**
     * Whether the video is playing or not.
     *
     * @return whether the video is still playing
     */
    boolean isPlaying();

    /**
     * This will return the the time passed.
     *
     * @return the time elapsed in milliseconds
     */
    int getCurrentTimestamp();

    /**
     * Disposes the VideoPlayer and ensures all buffers and resources are invalidated and disposed.
     */
    void dispose();

    /**
     * This will update the volume of the audio associated with the currently playing video.
     *
     * @param volume The new volume value in range from 0.0 (mute) to 1.0 (maximum)
     */
    void setVolume(float volume);

    /**
     * This will return the volume of the audio associated with the currently playing video.
     *
     * @return The volume of the audio in range from 0.0 (mute) to 1.0 (maximum)
     */
    float getVolume();

    void setLooping(boolean looping);

    boolean isLooping();

    /**
     * This sets the texture filtering used for displaying the video on screen.
     *
     * @see Texture#setFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter)
     */
    void setFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter);
}
