package com.pymu.arc.video.basic;

import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;

public abstract class AbstractVideoVideoPlayer implements VideoPlayerInterface {
    protected TextureFilter minFilter = TextureFilter.linear;
    protected TextureFilter magFilter = TextureFilter.linear;


    @Override
    public void setFilter(TextureFilter minFilter, TextureFilter magFilter) {
        if (this.minFilter == minFilter && this.magFilter == magFilter) return;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        Texture texture = getTexture();
        if (texture == null) return;
        texture.setFilter(minFilter, magFilter);
    }
}
