package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.presentation.picker.toPx

@Composable
fun MoreOptionsPopup(
    onClearEdits: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            -8.dp.toPx().toInt(),
            8.dp.toPx().toInt()
        ),
        properties = PopupProperties(
            focusable = true
        ),
        onDismissRequest = onDismissClick,
    ) {
        Column(
            Modifier
                .background(
                    Color.LightGray,
                    RoundedCornerShape(8)
                )
                .padding(8.dp)
        ) {
            Row {
                Column(
                    Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5))
                        .clickable {
                            onClearEdits()
                        }
                ) {

                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                        imageVector =
                        ImageVector.vectorResource(R.drawable.ic_clear),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.clear),
                        Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        fontSize = 12.sp
                    )
                }
                Column(
                    Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5))
                        .clickable {
                            onShareClick()
                        }
                ) {

                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                        imageVector = ImageVector
                            .vectorResource(R.drawable.ic_share),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.share),
                        Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        fontSize = 12.sp
                    )
                }
                Column(
                    Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5))
                        .clickable {
                            onSaveClick()
                        }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_save),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.save),
                        Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
