package com.tiltclock.wear.ui

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.tiltclock.wear.render.FilamentRenderer

/** TextureView respects Compose circular clipping and alpha, unlike an on-top SurfaceView. */
@Composable
fun AvatarSurface(renderer: FilamentRenderer, modifier: Modifier = Modifier) {
    AndroidView(modifier=modifier,factory={ context ->
        TextureView(context).apply {
            isOpaque = false
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(texture: SurfaceTexture,w: Int,h: Int) {
                    renderer.attach(Surface(texture),w,h)
                }
                override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture,w: Int,h: Int) { renderer.resize(w,h) }
                override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                    // Let the render thread release its swapchain before releasing the texture.
                    renderer.detach { texture.release() }; return false
                }
            }
        }
    })
}
