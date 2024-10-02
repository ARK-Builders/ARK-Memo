package dev.arkbuilders.arkmemo.ui.views.presentation.edit.blur

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager

@Composable
fun BlurIntensityPopup(
    editManager: EditManager
) {
    if (editManager.isBlurMode.value) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Text(stringResource(R.string.blur_intensity))
                Slider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = editManager.blurIntensity.value,
                    onValueChange = {
                        editManager.blurIntensity.value = it
                    },
                    valueRange = 0f..25f,
                )
            }
            Column {
                Text(stringResource(R.string.blur_size))
                Slider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = editManager.blurOperation.blurSize.value,
                    onValueChange = {
                        editManager.blurOperation.blurSize.value = it
                        editManager.blurOperation.resize()
                    },
                    valueRange = 100f..500f,
                )
            }
        }
    }
}
