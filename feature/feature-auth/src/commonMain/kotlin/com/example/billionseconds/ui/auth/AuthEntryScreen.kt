package com.example.billionseconds.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun AuthEntryScreen(
    uiState: AuthUiState,
    onAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Сохрани свой прогресс",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Войди в аккаунт, чтобы синхронизировать данные\nна всех устройствах",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            AuthButton(
                text = "Продолжить с Google",
                isLoading = uiState.isGoogleLoading,
                enabled = !uiState.isGoogleLoading && !uiState.isAppleLoading,
                onClick = { onAction(AuthAction.SignInWithGoogleClicked) },
            )

            AuthButton(
                text = "Продолжить с Apple",
                isLoading = uiState.isAppleLoading,
                enabled = !uiState.isGoogleLoading && !uiState.isAppleLoading,
                onClick = { onAction(AuthAction.SignInWithAppleClicked) },
            )

            uiState.error?.let { error ->
                Text(
                    text = when (error) {
                        is com.example.billionseconds.domain.auth.AuthErrorType.NetworkError ->
                            "Нет соединения. Попробуйте ещё раз."
                        is com.example.billionseconds.domain.auth.AuthErrorType.Cancelled ->
                            ""
                        else -> "Ошибка входа. Попробуйте ещё раз."
                    },
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }

            TextButton(onClick = { onAction(AuthAction.ContinueAsGuestClicked) }) {
                Text(
                    text = "Продолжить без аккаунта",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
