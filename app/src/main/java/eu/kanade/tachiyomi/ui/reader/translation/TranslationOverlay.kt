package eu.kanade.tachiyomi.ui.reader.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MangaPageTranslationOverlay(
    bubbles: List<SpeechBubble>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("translation_overlay_container")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onDismiss()
            }
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        bubbles.forEachIndexed { index, bubble ->
            val bubbleLeft = (bubble.left / 100f) * containerWidth.value
            val bubbleTop = (bubble.top / 100f) * containerHeight.value
            val bubbleWidth = (bubble.width / 100f) * containerWidth.value
            val bubbleHeight = (bubble.height / 100f) * containerHeight.value

            Box(
                modifier = Modifier
                    .offset(x = bubbleLeft.dp, y = bubbleTop.dp)
                    .size(width = bubbleWidth.dp, height = bubbleHeight.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(4.dp)
                    .testTag("translation_bubble_${index}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bubble.translated,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
