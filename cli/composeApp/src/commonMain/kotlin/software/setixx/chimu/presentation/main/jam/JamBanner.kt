package software.setixx.chimu.presentation.main.jam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import software.setixx.chimu.api.domain.GameJamStatus

@Composable
fun JamBanner(
    status: GameJamStatus,
    bannerUrl: String?,
    name: String,
    theme: String
){
    val localizedTheme = if (theme == "unknown") "Будет объявлена" else theme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
            bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
            bottomStart = MaterialTheme.shapes.extraLarge.bottomEnd
        )
    ) {
        if (bannerUrl != null) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(bannerUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Баннер джема",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ){
                    StatusChip(status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Тема: $localizedTheme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(280.dp)
            ){
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Баннер еще не загружен"
                )
            }
        }
    }
}