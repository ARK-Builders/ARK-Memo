package dev.arkbuilders.arkmemo.ui.views.presentation.edit.resize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager

@Composable
fun ResizeInput(isVisible: Boolean, editManager: EditManager) {
    if (isVisible) {
        var width by rememberSaveable {
            mutableStateOf(
                editManager.imageSize.width.toString()
            )
        }

        var height by rememberSaveable {
            mutableStateOf(
                editManager.imageSize.height.toString()
            )
        }

        val widthHint = stringResource(
            R.string.width_too_large,
            editManager.imageSize.width
        )
        val digitsHint = stringResource(R.string.digits_only)
        val heightHint = stringResource(
            R.string.height_too_large,
            editManager.imageSize.height
        )
        var hint by remember {
            mutableStateOf("")
        }
        var showHint by remember {
            mutableStateOf(false)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Hint(
                hint,
                isVisible = {
                    delayHidingHint(it) {
                        showHint = false
                    }
                    showHint
                }
            )
            Row {
                TextField(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    value = width,
                    onValueChange = {
                        if (
                            it.isNotEmpty() &&
                            it.isDigitsOnly() &&
                            it.toInt() > editManager.imageSize.width
                        ) {
                            hint = widthHint
                            showHint = true
                            return@TextField
                        }
                        if (it.isNotEmpty() && !it.isDigitsOnly()) {
                            hint = digitsHint
                            showHint = true
                            return@TextField
                        }
                        width = it
                        showHint = false
                        if (width.isEmpty()) height = width
                        if (width.isNotEmpty() && width.isDigitsOnly()) {
                            height = editManager.resizeDown(width = width.toInt())
                                .height.toString()
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.width),
                            modifier = Modifier
                                .fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    value = height,
                    onValueChange = {
                        if (
                            it.isNotEmpty() &&
                            it.isDigitsOnly() &&
                            it.toInt() > editManager.imageSize.height
                        ) {
                            hint = heightHint
                            showHint = true
                            return@TextField
                        }
                        if (it.isNotEmpty() && !it.isDigitsOnly()) {
                            hint = digitsHint
                            showHint = true
                            return@TextField
                        }
                        height = it
                        showHint = false
                        if (height.isEmpty()) width = height
                        if (height.isNotEmpty() && height.isDigitsOnly()) {
                            width = editManager.resizeDown(height = height.toInt())
                                .width.toString()
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.height),
                            modifier = Modifier
                                .fillMaxWidth(),
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
        }
    }
}

fun delayHidingHint(scope: CoroutineScope, hide: () -> Unit) {
    scope.launch {
        delay(1000)
        hide()
    }
}

@Composable
fun Hint(text: String, isVisible: (CoroutineScope) -> Boolean) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = isVisible(scope),
        enter = fadeIn(),
        exit = fadeOut(tween(durationMillis = 500, delayMillis = 1000)),
        modifier = Modifier
            .wrapContentSize()
            .background(Color.LightGray, RoundedCornerShape(10))
    ) {
        Text(
            text,
            Modifier
                .padding(12.dp)
        )
    }
}
