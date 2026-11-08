package com.tiltclock.wear.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.tiltclock.wear.R
import com.tiltclock.wear.data.AvatarCatalog

/** Intermediate preset picker. Full themed circular settings are delivered in phase 3. */
@Composable
fun AvatarControls(selected: String?,select: (String) -> Unit,close: () -> Unit) {
    val names = stringArrayResource(R.array.avatar_names)
    Box(Modifier.fillMaxSize().background(Color(0xF0F8FAFC)),contentAlignment=Alignment.Center) {
        Column(Modifier.fillMaxHeight().widthIn(max=126.dp).verticalScroll(rememberScrollState()).padding(vertical=68.dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            BasicText("Avatar",style=TextStyle(fontSize=14.sp,fontWeight=FontWeight.SemiBold))
            AvatarCatalog.ids.forEachIndexed { index,id ->
                Box(Modifier.fillMaxWidth().heightIn(min=48.dp).background(
                    if (id == selected) Color(0x6606B6D4) else Color(0xFFE2E8F0),CircleShape)
                    .clickable { select(id) }.padding(horizontal=8.dp),contentAlignment=Alignment.Center) {
                    BasicText(names[index],style=TextStyle(fontSize=12.sp))
                }
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clickable(onClick=close),contentAlignment=Alignment.Center) {
                BasicText(androidx.compose.ui.res.stringResource(R.string.close_settings))
            }
        }
    }
}
