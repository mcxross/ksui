/*
 * Copyright 2025 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.android.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import xyz.mcxross.ksui.android.R

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
  return if (showShimmer) {
    val shimmerColors =
      listOf(
        Color.Gray.copy(alpha = 0.1f),
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.1f),
      )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation =
      transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing)),
        label = "shimmer",
      )
    Brush.linearGradient(
      colors = shimmerColors,
      start = Offset.Zero,
      end = Offset(x = translateAnimation.value, y = translateAnimation.value),
    )
  } else {
    SolidColor(Color.Transparent)
  }
}

@Composable
fun DatingProfileScreen() {
  var isLoading by remember { mutableStateOf(true) }

  LaunchedEffect(key1 = true) {
    delay(1500)
    isLoading = false
  }

  Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
    if (isLoading) {
      DatingProfileShimmer()
    } else {
      DatingProfileContent()
    }
  }
}

@Composable
fun DatingProfileShimmer() {
  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(bottomStart = 55.dp, bottomEnd = 55.dp))
          .background(shimmerBrush())
    ) {
      Column(
        modifier =
          Modifier.align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
      ) {
        Spacer(
          modifier =
            Modifier.height(32.dp)
              .fillMaxWidth(0.6f)
              .clip(RoundedCornerShape(8.dp))
              .background(shimmerBrush())
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Spacer(
            modifier =
              Modifier.height(20.dp)
                .fillMaxWidth(0.2f)
                .clip(RoundedCornerShape(50.dp))
                .background(shimmerBrush())
          )
          Spacer(
            modifier =
              Modifier.height(20.dp)
                .fillMaxWidth(0.2f)
                .clip(RoundedCornerShape(50.dp))
                .background(shimmerBrush())
          )
          Spacer(
            modifier =
              Modifier.height(20.dp)
                .fillMaxWidth(0.2f)
                .clip(RoundedCornerShape(50.dp))
                .background(shimmerBrush())
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Spacer(
          modifier =
            Modifier.height(18.dp)
              .fillMaxWidth(0.4f)
              .clip(RoundedCornerShape(8.dp))
              .background(shimmerBrush())
        )
      }
    }

    Row(
      modifier =
        Modifier.fillMaxWidth().systemBarsPadding().padding(vertical = 16.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(modifier = Modifier.size(48.dp).clip(CircleShape).background(shimmerBrush()))
      Spacer(modifier = Modifier.size(64.dp).clip(CircleShape).background(shimmerBrush()))
      Spacer(modifier = Modifier.size(48.dp).clip(CircleShape).background(shimmerBrush()))
      Spacer(modifier = Modifier.size(64.dp).clip(CircleShape).background(shimmerBrush()))
      Spacer(modifier = Modifier.size(48.dp).clip(CircleShape).background(shimmerBrush()))
    }
  }
}

@Composable
fun DatingProfileContent() {
  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(bottomStart = 55.dp, bottomEnd = 55.dp))
    ) {
      Image(
        painter = painterResource(id = R.drawable.adeniyi),
        contentDescription = "Profile Picture",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(200.dp)
            .align(Alignment.BottomCenter)
            .background(
              brush =
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
            )
      )

      Column(
        modifier =
          Modifier.align(Alignment.BottomStart)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Adeniyi 60",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
          )
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Color.White,
            modifier =
              Modifier.size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .padding(4.dp),
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ProfileTag("Travel", Color(0xFFFF4081))
          ProfileTag("Reading", Color(0xFFE040FB))
          ProfileTag("Running", Color(0xFF651FFF))
          ProfileTag("Sui", Color(0xFF0042B4))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Free Alpha", color = Color.White, fontSize = 18.sp)
      }
    }

    Row(
      modifier =
        Modifier.fillMaxWidth().systemBarsPadding().padding(vertical = 16.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ActionButton(
        Icons.Default.Refresh,
        Color.Transparent,
        Color.Gray,
        2.dp,
        iconTint = Color.Gray,
      )
      ActionButton(
        icon = Icons.Default.Close,
        backgroundColor = Color.Black,
        borderColor = Color.DarkGray,
        borderWidth = 2.dp,
        iconTint = Color.White,
        modifier = Modifier.size(64.dp),
      )
      ActionButton(Icons.Default.Star, Color.White, iconTint = Color(0xFF651FFF))
      ActionButton(
        icon = Icons.Default.Favorite,
        backgroundColor = Color(0xFF00FFC2),
        iconTint = Color.White,
        modifier = Modifier.size(64.dp),
      )
      ActionButton(
        Icons.Default.ThumbUp,
        Color.Transparent,
        Color.White,
        2.dp,
        iconTint = Color(0xFF651FFF),
      )
    }
  }
}

@Composable
fun ProfileTag(text: String, backgroundColor: Color) {
  Text(
    text = text,
    color = Color.White,
    fontSize = 12.sp,
    modifier =
      Modifier.clip(RoundedCornerShape(50))
        .background(backgroundColor)
        .padding(horizontal = 8.dp, vertical = 4.dp),
  )
}

@Composable
fun ActionButton(
  icon: ImageVector,
  backgroundColor: Color,
  borderColor: Color = Color.Transparent,
  borderWidth: Dp = 0.dp,
  iconTint: Color = Color.Unspecified,
  modifier: Modifier = Modifier.size(48.dp),
) {
  Box(
    modifier =
      modifier
        .clip(CircleShape)
        .background(backgroundColor)
        .then(
          if (borderColor != Color.Transparent)
            Modifier.border(borderWidth, borderColor, CircleShape)
          else Modifier
        ),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(if (modifier == Modifier.size(64.dp)) 36.dp else 28.dp),
    )
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewDatingProfileScreen() {
  Surface(color = Color.Black) { MaterialTheme { DatingProfileScreen() } }
}
