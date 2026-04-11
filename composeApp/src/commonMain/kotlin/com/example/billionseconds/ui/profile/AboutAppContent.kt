package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.LegalLinkType
import com.example.billionseconds.mvi.ProfileUiState

@Composable
fun AboutAppContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))

        SectionHeader(title = "О приложении")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Версия", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = uiState.appVersion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        SectionHeader(title = "Юридическая информация")

        NavigationRow(
            emoji = "🔒",
            title = "Политика конфиденциальности",
            onClick = { onIntent(AppIntent.LegalLinkClicked(LegalLinkType.PrivacyPolicy)) }
        )

        NavigationRow(
            emoji = "📋",
            title = "Условия использования",
            onClick = { onIntent(AppIntent.LegalLinkClicked(LegalLinkType.TermsOfUse)) }
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { onIntent(AppIntent.ProfileSubScreenDismissed) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Назад")
        }

        Spacer(Modifier.height(16.dp))
    }
}
