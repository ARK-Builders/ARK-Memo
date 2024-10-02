package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.data.Resolution
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.resize.Hint
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.resize.delayHidingHint
import dev.arkbuilders.arkmemo.ui.views.presentation.theme.Gray

//import dev.arkbuilders.arkmemo.ui.views.presentation.theme.getGray

@Composable
fun NewImageOptionsDialog(
    defaultResolution: Resolution,
    maxResolution: Resolution,
    _backgroundColor: Color,
    navigateBack: () -> Unit,
    editManager: EditManager,
    persistDefaults: (Color, Resolution) -> Unit,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    var backgroundColor by remember {
        mutableStateOf(_backgroundColor)
    }
    val showColorDialog = remember { mutableStateOf(false) }

    ColorPickerDialog(
        isVisible = showColorDialog,
        initialColor = backgroundColor,
        enableEyeDropper = false,
        onToggleEyeDropper = {},
        onColorChanged = {
            backgroundColor = it
        }
    )

    if (isVisible) {
        var width by remember {
            mutableStateOf(defaultResolution.width.toString())
        }
        var height by remember {
            mutableStateOf(defaultResolution.height.toString())
        }
        var widthError by remember {
            mutableStateOf(0.toString())
        }
        var heightError by remember {
            mutableStateOf(0.toString())
        }
        var rememberDefaults by remember { mutableStateOf(false) }
        var showHint by remember { mutableStateOf(false) }
        var hint by remember { mutableStateOf("") }
        val maxHeightHint = stringResource(
            R.string.height_too_large,
            maxResolution.height
        )
        val minHeightHint = stringResource(
            R.string.height_not_accepted,
            heightError
        )
        val maxWidthHint = stringResource(
            R.string.width_too_large,
            maxResolution.width
        )
        val minWidthHint = stringResource(
            R.string.width_not_accepted,
            widthError
        )
        val digitsOnlyHint = stringResource(
            R.string.digits_only
        )
        val widthEmptyHint = stringResource(
            R.string.width_empty
        )
        val heightEmptyHint = stringResource(
            R.string.height_empty
        )

        Dialog(
            onDismissRequest = {
                isVisible = false
                navigateBack()
            }
        ) {
            Column(
                Modifier
                    .background(Color.White, RoundedCornerShape(5))
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Customize new image",
                    Modifier.padding(top = 10.dp)
                )
                Row(
                    Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 20.dp,
                        bottom = 12.dp
                    )
                ) {
                    TextField(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .fillMaxWidth(0.5f),
                        value = width,
                        onValueChange = {
                            if (!it.isDigitsOnly()) {
                                hint = digitsOnlyHint
                                showHint = true
                                return@TextField
                            }
                            if (
                                it.isNotEmpty() && it.isDigitsOnly() &&
                                it.toInt() > maxResolution.width
                            ) {
                                hint = maxWidthHint
                                showHint = true
                                return@TextField
                            }
                            if (
                                it.isNotEmpty() && it.isDigitsOnly() &&
                                it.toInt() <= 0
                            ) {
                                widthError = it
                                hint = minWidthHint
                                showHint = true
                                return@TextField
                            }
                            if (it.isDigitsOnly()) {
                                width = it
                            }
                        },
                        label = {
                            Text(
                                stringResource(R.string.width),
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Center
                            )
                        },
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    TextField(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .fillMaxWidth(),
                        value = height,
                        onValueChange = {
                            if (!it.isDigitsOnly()) {
                                hint = digitsOnlyHint
                                showHint = true
                                return@TextField
                            }
                            if (
                                it.isNotEmpty() && it.isDigitsOnly() &&
                                it.toInt() > maxResolution.height
                            ) {
                                hint = maxHeightHint
                                showHint = true
                                return@TextField
                            }
                            if (
                                it.isNotEmpty() && it.isDigitsOnly() &&
                                it.toInt() <= 0
                            ) {
                                heightError = it
                                hint = minHeightHint
                                showHint = true
                                return@TextField
                            }
                            if (it.isDigitsOnly()) {
                                height = it
                            }
                        },
                        label = {
                            Text(
                                stringResource(R.string.height),
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Center
                            )
                        },
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
                Row(
                    Modifier
                        .background(Gray, RoundedCornerShape(5))
                        .wrapContentHeight()
                        .clickable {
                            showColorDialog.value = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.background),
                        Modifier.padding(8.dp)
                    )
                    Box(
                        Modifier
                            .size(28.dp)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .border(2.dp, Gray, CircleShape)
                            .background(backgroundColor)
                    )
                }
                Row(
                    Modifier
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberDefaults,
                        onCheckedChange = {
                            rememberDefaults = it
                        }
                    )
                    Text("Remember")
                }
                Row(
                    Modifier.align(
                        Alignment.End
                    )
                ) {
                    TextButton(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        onClick = {
                            isVisible = false
                            navigateBack()
                        }
                    ) {
                        Text("Close")
                    }
                    TextButton(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        onClick = {
                            if (width.isEmpty()) {
                                hint = widthEmptyHint
                                showHint = true
                                return@TextButton
                            }
                            if (height.isEmpty()) {
                                hint = heightEmptyHint
                                showHint = true
                                return@TextButton
                            }
                            val resolution = Resolution(
                                width.toInt(),
                                height.toInt()
                            )
                            editManager.setImageResolution(resolution)
                            editManager.setBackgroundColor(backgroundColor)
                            if (rememberDefaults)
                                persistDefaults(backgroundColor, resolution)
                            onConfirm()
                            isVisible = false
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }

            Hint(
                hint
            ) {
                delayHidingHint(it) {
                    showHint = false
                }
                showHint
            }
        }
    }
}
