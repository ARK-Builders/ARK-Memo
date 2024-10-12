package dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.aspectRatios
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isChanged
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isCropFree
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isCropSquare
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isCrop_9_16
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isCrop_2_3
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.crop.AspectRatio.isCrop_4_5

@Composable
fun CropAspectRatiosMenu(
    isVisible: Boolean = false,
    cropWindow: CropWindow
) {
    if (isVisible) {
        if (isChanged.value) {
            cropWindow.resize()
            isChanged.value = false
        }
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        switchAspectRatio(isCropFree)
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, bottom = 5.dp)
                        .align(Alignment.CenterHorizontally)
                        .size(30.dp),
                    imageVector =
                    ImageVector.vectorResource(id = R.drawable.ic_crop),
                    contentDescription =
                    stringResource(id = R.string.ark_retouch_crop_free),
                    tint = if (isCropFree.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
                Text(
                    text = stringResource(R.string.ark_retouch_crop_free),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    color = if (isCropFree.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
            }
            Column(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        switchAspectRatio(isCropSquare)
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, bottom = 5.dp)
                        .align(Alignment.CenterHorizontally)
                        .size(30.dp),
                    imageVector =
                    ImageVector.vectorResource(id = R.drawable.ic_crop_square),
                    contentDescription =
                    stringResource(id = R.string.ark_retouch_crop_square),
                    tint = if (isCropSquare.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
                Text(
                    stringResource(R.string.ark_retouch_crop_square),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (isCropSquare.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
            }
            Column(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        switchAspectRatio(isCrop_4_5)
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(
                            start = 12.dp, end = 12.dp,
                            top = 5.dp, bottom = 5.dp
                        )
                        .rotate(90f)
                        .align(Alignment.CenterHorizontally)
                        .size(30.dp),
                    imageVector =
                    ImageVector.vectorResource(id = R.drawable.ic_crop_5_4),
                    contentDescription =
                    stringResource(id = R.string.ark_retouch_crop_4_5),
                    tint = if (isCrop_4_5.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
                Text(
                    stringResource(R.string.ark_retouch_crop_4_5),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (isCrop_4_5.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
            }
            Column(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        switchAspectRatio(isCrop_9_16)
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(
                            start = 12.dp, end = 12.dp,
                            top = 5.dp, bottom = 5.dp
                        )
                        .rotate(90f)
                        .align(Alignment.CenterHorizontally)
                        .size(30.dp),
                    imageVector =
                    ImageVector.vectorResource(id = R.drawable.ic_crop_16_9),
                    contentDescription =
                    stringResource(id = R.string.ark_retouch_crop_9_16),
                    tint = if (isCrop_9_16.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
                Text(
                    stringResource(R.string.ark_retouch_crop_9_16),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (isCrop_9_16.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
            }
            Column(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        switchAspectRatio(isCrop_2_3)
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(
                            start = 12.dp, end = 12.dp,
                            top = 5.dp, bottom = 5.dp
                        )
                        .rotate(90f)
                        .align(Alignment.CenterHorizontally)
                        .size(30.dp),
                    imageVector =
                    ImageVector.vectorResource(id = R.drawable.ic_crop_3_2),
                    contentDescription =
                    stringResource(id = R.string.ark_retouch_crop_2_3),
                    tint = if (isCrop_2_3.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
                Text(
                    stringResource(R.string.ark_retouch_crop_2_3),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (isCrop_2_3.value)
                        MaterialTheme.colors.primary
                    else Color.Black
                )
            }
        }
    } else switchAspectRatio(isCropFree)
}

internal fun switchAspectRatio(selected: MutableState<Boolean>) {
    selected.value = true
    aspectRatios.filter {
        it != selected
    }.forEach {
        it.value = false
    }
    isChanged.value = true
}

internal object AspectRatio {
    val isCropFree = mutableStateOf(false)
    val isCropSquare = mutableStateOf(false)
    val isCrop_4_5 = mutableStateOf(false)
    val isCrop_9_16 = mutableStateOf(false)
    val isCrop_2_3 = mutableStateOf(false)
    val isChanged = mutableStateOf(false)

    val aspectRatios = listOf(
        isCropFree,
        isCropSquare,
        isCrop_4_5,
        isCrop_9_16,
        isCrop_2_3
    )

    val CROP_FREE = Offset(0f, 0f)
    val CROP_SQUARE = Offset(1f, 1f)
    val CROP_4_5 = Offset(4f, 5f)
    val CROP_9_16 = Offset(9f, 16f)
    val CROP_2_3 = Offset(2f, 3f)
}
