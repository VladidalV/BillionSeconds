package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent

@Composable
fun ActionsBlock(onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная кнопка — Share
        Button(
            onClick = { onIntent(AppIntent.ShareClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Поделиться")
        }

        // Вторичные кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onIntent(AppIntent.CreateVideoClicked) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Видео", maxLines = 1)
            }
            OutlinedButton(
                onClick = { onIntent(AppIntent.WriteLetterClicked) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Письмо", maxLines = 1)
            }
        }

        OutlinedButton(
            onClick = { onIntent(AppIntent.AddFamilyClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить семью")
        }

        // Смена даты рождения
        TextButton(
            onClick = { onIntent(AppIntent.ClearClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Изменить дату рождения")
        }
    }
}
