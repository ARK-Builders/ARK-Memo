package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import dev.arkbuilders.arkmemo.R

@Composable
fun ColorPickerDialog(
    isVisible: MutableState<Boolean>,
    initialColor: Color,
    usedColors: List<Color> = listOf(),
    enableEyeDropper: Boolean,
    onToggleEyeDropper: () -> Unit,
    onColorChanged: (Color) -> Unit,
) {
    if (!isVisible.value) return

    var currentColor by remember {
        mutableStateOf(HsvColor.from(initialColor))
    }

    val finish = {
        onColorChanged(currentColor.toColor())
        isVisible.value = false
    }

    Dialog(
        onDismissRequest = {
            isVisible.value = false
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(5))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (usedColors.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                ) {
                    val state = rememberLazyListState()

                    LazyRow(
                        Modifier
                            .align(Alignment.Center),
                        state = state
                    ) {
                        items(usedColors) { color ->
                            Box(
                                Modifier
                                    .padding(
                                        start = 5.dp,
                                        end = 5.dp,
                                        top = 12.dp,
                                        bottom = 12.dp
                                    )
                                    .size(25.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        currentColor = HsvColor.from(color)
                                        finish()
                                    }
                            )
                        }
                    }
                    LaunchedEffect(state) {
                        scrollToEnd(state, this)
                    }
                    UsedColorsFlowHint(
                        { enableScroll(state) },
                        { checkScroll(state).first },
                        { checkScroll(state).second }
                    )
                }
            }
            ClassicColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                color = currentColor.toColor(),
                onColorChanged = {
                    currentColor = it
                }
            )
            if (enableEyeDropper) {
                Box(Modifier.padding(8.dp)) {
                    Box(
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable {
                                onToggleEyeDropper()
                                isVisible.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_eyedropper),
                            "",
                            Modifier.size(25.dp)
                        )
                    }
                }
            }
            TextButton(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                onClick = finish
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(50.dp)
                            .border(
                                2.dp,
                                Color.LightGray,
                                CircleShape
                            )
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(color = currentColor.toColor())
                    )
                    Text(text = "Pick", fontSize = 18.sp)
                }
            }
        }
    }
}

fun scrollToEnd(state: LazyListState, scope: CoroutineScope) {
    scope.launch {
        if (enableScroll(state)) {
            val lastIndex = state.layoutInfo.totalItemsCount - 1
            state.scrollToItem(lastIndex, 0)
        }
    }
}

fun enableScroll(state: LazyListState): Boolean {
    return state.layoutInfo.totalItemsCount !=
        state.layoutInfo.visibleItemsInfo.size
}

fun checkScroll(state: LazyListState): Pair<Boolean, Boolean> {
    var scrollIsAtStart = true
    var scrollIsAtEnd = false
    if (enableScroll(state)) {
        val totalItems = state.layoutInfo.totalItemsCount
        val visibleItems = state.layoutInfo.visibleItemsInfo.size
        val itemSize =
            state.layoutInfo.visibleItemsInfo.firstOrNull()?.size
                ?: 0
        val rowSize = itemSize * totalItems
        val visibleRowSize = itemSize * visibleItems
        val scrollValue = state.firstVisibleItemIndex * itemSize
        val maxScrollValue = rowSize - visibleRowSize
        scrollIsAtStart = scrollValue == 0
        scrollIsAtEnd = scrollValue == maxScrollValue
    }
    return scrollIsAtStart to scrollIsAtEnd
}

@Composable
fun BoxScope.UsedColorsFlowHint(
    scrollIsEnabled: () -> Boolean,
    scrollIsAtStart: () -> Boolean,
    scrollIsAtEnd: () -> Boolean
) {
    AnimatedVisibility(
        visible = scrollIsEnabled() && (
            scrollIsAtEnd() || (!scrollIsAtStart() && !scrollIsAtEnd())
            ),
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
        modifier = Modifier
            .background(Color.White)
            .align(Alignment.CenterStart)
    ) {
        Icon(
            Icons.Filled.KeyboardArrowLeft,
            contentDescription = null,
            Modifier.size(32.dp)
        )
    }
    AnimatedVisibility(
        visible = scrollIsEnabled() && (
            scrollIsAtStart() || (!scrollIsAtStart() && !scrollIsAtEnd())
            ),
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
        modifier = Modifier
            .background(Color.White)
            .align(Alignment.CenterEnd)
    ) {
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            Modifier.size(32.dp)
        )
    }
}
