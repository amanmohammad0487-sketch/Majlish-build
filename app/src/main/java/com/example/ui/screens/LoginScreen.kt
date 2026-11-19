package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassStatusPill
import com.example.ui.theme.LiquidGlassTextField
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.MajlishUiState

@Composable
fun LoginScreen(
    uiState: MajlishUiState,
    onKeyChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    val clipboard = LocalClipboardManager.current
    var showKey by remember { mutableStateOf(true) }

    val infinite = rememberInfiniteTransition(label = "emblem_float")
    val emblemScale by infinite.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblem_scale"
    )

    val isVerifying = uiState.authUiState is AuthUiState.Verifying

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiquidGlassStatusPill(
                text = "LIQUID ENGINE v3.0 • ENCRYPTED AUTH",
                dotColor = palette.accent
            )

            Spacer(Modifier.height(28.dp))

            // Crystalline Floating Majlish Logo Badge
            Box(
                modifier = Modifier
                    .scale(emblemScale)
                    .size(116.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = palette.accent.copy(alpha = 0.65f),
                        ambientColor = palette.accentSecondary.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF090F1E))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.72f),
                                palette.accent.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.22f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.majlish_logo_1795056643980),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.4.sp
                ),
                color = palette.accent.copy(alpha = 0.90f)
            )

            Spacer(Modifier.height(30.dp))

            // Liquid Glass Auth Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 30.dp,
                elevated = true,
                contentPadding = PaddingValues(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.18f))
                                .border(1.dp, palette.accent.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.auth_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.auth_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    LiquidGlassTextField(
                        value = uiState.keyInput,
                        onValueChange = onKeyChange,
                        placeholder = stringResource(R.string.auth_key_placeholder),
                        leadingIcon = Icons.Filled.Key,
                        monospace = true,
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onVerifyClick() }),
                        modifier = Modifier.testTag("license_key_input"),
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { showKey = !showKey },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = stringResource(R.string.cd_toggle_visibility),
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.accent.copy(alpha = 0.20f))
                                        .border(1.dp, palette.accent.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            clipboard.getText()?.text?.let { pasted ->
                                                onKeyChange(pasted.trim())
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                        .testTag("paste_key_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentPaste,
                                            contentDescription = stringResource(R.string.cd_paste_key),
                                            tint = palette.accent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.auth_paste),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = palette.accent
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Auth feedback banner (Denied / Granted)
                    AnimatedVisibility(
                        visible = uiState.authUiState is AuthUiState.Denied || uiState.authUiState is AuthUiState.Granted,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val isGranted = uiState.authUiState is AuthUiState.Granted
                        val bannerColor = if (isGranted) palette.success else palette.error
                        val title = if (isGranted) stringResource(R.string.auth_granted) else stringResource(R.string.auth_denied)
                        val detail = when (val st = uiState.authUiState) {
                            is AuthUiState.Granted -> st.message
                            is AuthUiState.Denied -> st.reason
                            else -> ""
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bannerColor.copy(alpha = 0.16f))
                                .border(1.dp, bannerColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = bannerColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = bannerColor
                                )
                                if (detail.isNotBlank()) {
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textPrimary.copy(alpha = 0.88f)
                                    )
                                }
                            }
                        }
                    }

                    LiquidGlassButton(
                        text = if (isVerifying) stringResource(R.string.auth_verifying) else stringResource(R.string.auth_verify_cta),
                        onClick = onVerifyClick,
                        icon = Icons.Filled.LockOpen,
                        loading = isVerifying,
                        enabled = uiState.keyInput.isNotBlank() && !isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_button")
                    )

                    Spacer(Modifier.height(16.dp))

                    // Hardware binding info pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PhoneAndroid,
                                contentDescription = null,
                                tint = palette.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = uiState.deviceName,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        Text(
                            text = uiState.deviceId,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                            color = palette.accent.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Verified live via secure key server • HWID protected",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
