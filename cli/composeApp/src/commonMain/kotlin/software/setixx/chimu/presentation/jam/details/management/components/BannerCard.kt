package software.setixx.chimu.presentation.jam.details.management.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BannerCard(
    bannerUrl: String?,
    onOpenBannerPicker: () -> Unit,
    onDeleteBanner: () -> Unit,
    isEditable: Boolean
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(stringResource(Res.string.jam_card_banner_desc), style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (bannerUrl != null) stringResource(Res.string.management_banner_loaded)
                            else stringResource(Res.string.management_banner_not_loaded),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


                if (bannerUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(Res.string.jam_card_banner_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.largeIncreased),
                        contentScale = ContentScale.Crop
                    )
                }

                if (isEditable){
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenBannerPicker) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (bannerUrl != null) stringResource(Res.string.management_banner_replace) else stringResource(Res.string.management_banner_upload))
                        }
                        if (bannerUrl != null) {
                            OutlinedButton(
                                onClick = onDeleteBanner,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(Res.string.delete))
                            }
                        }
                    }
                }
            }
        }
    }
}