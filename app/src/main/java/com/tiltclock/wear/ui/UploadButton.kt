package com.tiltclock.wear.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import com.tiltclock.wear.R

/** ellipse(90% 100% at 50% 0%) intersected with its button bounds. */
object CurvedUploadShape: Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val oval=Path().apply { addOval(Rect(-size.width*.4f,-size.height,size.width*1.4f,size.height)) }
        val bounds=Path().apply { addRect(Rect(0f,0f,size.width,size.height)) }
        return Outline.Generic(Path.combine(PathOperation.Intersect,oval,bounds))
    }
}
@Composable
fun UploadButton(scale: Float, click: () -> Unit) {
    Box(Modifier.height(48.dp).width((140*scale).dp).clickable(onClick=click),contentAlignment=Alignment.BottomCenter) {
        Box(Modifier.fillMaxWidth().height((26*scale).dp).clip(CurvedUploadShape)
            .background(Color(0xFF06B6D4)).padding(top=3.dp),contentAlignment=Alignment.TopCenter) {
            androidx.compose.foundation.text.BasicText(stringResource(R.string.upload_avatar),style=androidx.compose.ui.text.TextStyle(
                color=Color.White,fontFamily=Inter,fontSize=(10*scale).sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold))
        }
    }
}
