package com.pymu.arc.video.player;

import arc.files.Fi;
import arc.graphics.Texture;
import com.pymu.arc.video.basic.VideoPlayer;

import java.io.FileNotFoundException;

/**
 * 构建一个空的播放器
 */
public class EmptyVideoVideoPlayer implements VideoPlayer {
    /**
     * This function will prepare the VideoPlayer to play the given file. If a video is already played, it will be stopped, and
     * the new video will be loaded. The video starts playing as soon as it is loaded.
     *
     * @param file The file containing the video which should be played.
     * @return Whether loading the file was successful.
     * @throws FileNotFoundException if the file does not exist
     */
    @Override
    public boolean play(Fi file) throws FileNotFoundException {
        return false;
    }

    /**
     * This function needs to be called every frame, so that the player can update all the buffers and you can draw the frame.
     * Normal use case is to start rendering after {@link #isBuffered()} returns true.
     *
     * @return if a new frame is available as texture
     */
    @Override
    public boolean update() {
        return false;
    }

    /**
     * 渲染视频帧
     */
    @Override
    public void renderVideoFrame() {

    }

    /**
     * @return The current video frame. Null if video was not started yet
     */
    @Override
    public Texture getTexture() {
        return null;
    }

    /**
     * Whether the buffer containing the video is completely filled. The size of the buffer is platform specific, and cannot
     * necessarily be depended upon. Review the documentation per platform for specifics.
     *
     * @return buffer completely filled or not.
     */
    @Override
    public boolean isBuffered() {
        return false;
    }

    /**
     * This pauses the video, and should be called when the app is paused, to prevent the video from playing while being swapped
     * away.
     */
    @Override
    public void pause() {

    }

    /**
     * This resumes the video after it is paused.
     */
    @Override
    public void resume() {

    }

    /**
     * This will stop playing the file, and implicitly clears all buffers and invalidate resources used.
     */
    @Override
    public void stop() {

    }

    /**
     * This will set a listener for whenever the video size of a file is known (after calling play). This is needed since the size
     * of the video is not directly known after using the play method.
     * <p>
     * This may be called on any thread, so it is not safe to call other VideoPlayer functions directly.
     *
     * @param listener The listener to set
     */
    @Override
    public void setOnVideoSizeListener(VideoSizeListener listener) {

    }

    /**
     * This will set a listener for when the video is done playing. The listener will be called every time a video is done
     * playing.
     * <p>
     * This may be called on any thread, so it is not safe to call other VideoPlayer functions directly.
     *
     * @param listener The listener to set
     */
    @Override
    public void setOnCompletionListener(VideoCompletionListener listener) {

    }

    /**
     * This will return the width of the currently playing video.
     * <p>
     * This function cannot be called until the {@link com.badlogic.gdx.video.VideoPlayer.VideoSizeListener} has been called for the currently playing video. If this
     * callback has not been set, a good alternative is to wait until the {@link #isBuffered} function returns true, which
     * guarantees the availability of the videoSize.
     *
     * @return the width of the video
     */
    @Override
    public int getVideoWidth() {
        return 0;
    }

    /**
     * This will return the height of the currently playing video.
     * <p>
     * This function cannot be called until the {@link com.badlogic.gdx.video.VideoPlayer.VideoSizeListener} has been called for the currently playing video. If this
     * callback has not been set, a good alternative is to wait until the {@link #isBuffered} function returns true, which
     * guarantees the availability of the videoSize.
     *
     * @return the height of the video
     */
    @Override
    public int getVideoHeight() {
        return 0;
    }

    /**
     * Whether the video is playing or not.
     *
     * @return whether the video is still playing
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * This will return the the time passed.
     *
     * @return the time elapsed in milliseconds
     */
    @Override
    public int getCurrentTimestamp() {
        return 0;
    }

    /**
     * Disposes the VideoPlayer and ensures all buffers and resources are invalidated and disposed.
     */
    @Override
    public void dispose() {

    }

    /**
     * This will update the volume of the audio associated with the currently playing video.
     *
     * @param volume The new volume value in range from 0.0 (mute) to 1.0 (maximum)
     */
    @Override
    public void setVolume(float volume) {

    }

    /**
     * This will return the volume of the audio associated with the currently playing video.
     *
     * @return The volume of the audio in range from 0.0 (mute) to 1.0 (maximum)
     */
    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public void setLooping(boolean looping) {

    }

    @Override
    public boolean isLooping() {
        return false;
    }

    /**
     * This sets the texture filtering used for displaying the video on screen.
     *
     * @param minFilter min
     * @param magFilter mag
     * @see Texture#setFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter)
     */
    @Override
    public void setFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {

    }
}
