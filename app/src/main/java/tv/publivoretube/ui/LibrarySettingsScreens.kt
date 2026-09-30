package tv.publivoretube.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import tv.publivoretube.data.GoogleAccount
import tv.publivoretube.data.Subscription
import tv.publivoretube.data.Video

private val Bg = Color(0xFF08070D)
private val Panel = Color(0xFF171521)
private val PanelStrong = Color(0xFF211B2C)
private val Accent = Color(0xFFE7DAFF)
private val AccentStrong = Color(0xFFC59BFF)
private val Muted = Color(0xFFB9B2C5)
private val Pill = RoundedCornerShape(50.dp)

@Composable
fun HistoryScreen(
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(42.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.History, null, tint = AccentStrong, modifier = Modifier.size(30.dp))
            Text(
                "History",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.padding(start = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            if (videos.isNotEmpty()) {
                TvActionButton(
                    icon = Icons.Rounded.Delete,
                    text = "Clear history",
                    onClick = onClear,
                )
            }
        }

        if (videos.isEmpty()) {
            EmptyPanel(
                title = "Your watch history is empty",
                message = "Videos you open in PublivoreTube will appear here.",
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 36.dp),
            ) {
                items(videos, key = { "history-" + it.id }) { video ->
                    HistoryRow(video, onVideoSelected)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(video: Video, onVideoSelected: (Video) -> Unit) {
    Button(
        onClick = { onVideoSelected(video) },
        shape = ButtonDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ButtonDefaults.colors(
            containerColor = Panel,
            contentColor = Color.White,
            focusedContainerColor = AccentStrong,
            focusedContentColor = Bg,
        ),
        contentPadding = PaddingValues(10.dp),
        modifier = Modifier.fillMaxWidth().height(116.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                modifier = Modifier.size(width = 170.dp, height = 96.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(video.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(video.channel, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
fun SubscriptionsScreen(
    subscriptions: List<Subscription>,
    signedIn: Boolean,
    account: GoogleAccount?,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(42.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Subscriptions, null, tint = AccentStrong, modifier = Modifier.size(30.dp))
            Text(
                "Subscriptions",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        if (!signedIn) {
            SignInPanel(
                title = "Sign in to see your subscriptions",
                message = "PublivoreTube needs your YouTube account to load your subscribed channels.",
                onSignIn = onSignIn,
            )
        } else if (subscriptions.isEmpty()) {
            EmptyPanel(
                title = "No subscriptions found",
                message = account?.email?.let { "Signed in as $it." } ?: "Your account is signed in.",
            )
        } else {
            account?.email?.let {
                Text("Signed in as $it", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 36.dp),
            ) {
                items(subscriptions, key = { it.channelId }) { subscription ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(86.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = SurfaceDefaults.colors(containerColor = Panel),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            AsyncImage(
                                model = subscription.thumbnail,
                                contentDescription = null,
                                modifier = Modifier.size(62.dp),
                            )
                            Text(
                                subscription.channelTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    signedIn: Boolean,
    account: GoogleAccount?,
    oauthConfigured: Boolean,
    authBusy: Boolean,
    authCode: String?,
    authUrl: String?,
    autoplay: Boolean,
    captions: Boolean,
    rememberPosition: Boolean,
    showThumbnails: Boolean,
    reduceAnimations: Boolean,
    highContrast: Boolean,
    quality: String,
    onAutoplayChange: (Boolean) -> Unit,
    onCaptionsChange: (Boolean) -> Unit,
    onRememberPositionChange: (Boolean) -> Unit,
    onThumbnailsChange: (Boolean) -> Unit,
    onReduceAnimationsChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onQualityChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onClearHistory: () -> Unit,
) {
    val settingsFocus = FocusRequester()

    LaunchedOnceFocus(settingsFocus)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(42.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.displaySmall, color = Color.White)
        }

        item {
            SettingsSectionHeader(Icons.Rounded.AccountCircle, "Account")
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = SurfaceDefaults.colors(containerColor = Panel),
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = if (signedIn) "Google / YouTube account connected" else "No Google account connected",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Text(
                        text = account?.email ?: "Sign in to load subscriptions, account data, ratings and playlists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )

                    if (!signedIn) {
                        TvActionButton(
                            modifier = Modifier.focusRequester(settingsFocus),
                            icon = Icons.Rounded.AccountCircle,
                            text = if (!oauthConfigured) "Configure Google sign-in" else "Sign in with Google",
                            onClick = onSignIn,
                        )
                    } else {
                        TvActionButton(
                            modifier = Modifier.focusRequester(settingsFocus),
                            icon = Icons.Rounded.Lock,
                            text = "Sign out",
                            onClick = onSignOut,
                        )
                    }

                    if (authCode != null) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            colors = SurfaceDefaults.colors(containerColor = PanelStrong),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Use another device to approve this TV",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                )
                                Text(
                                    "Code: " + authCode,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Accent,
                                )
                                Text(
                                    "Open: " + (authUrl ?: "https://www.google.com/device"),
                                    color = Muted,
                                )
                                Text(
                                    if (authBusy) "Waiting for Google approval…" else "Authorization finished.",
                                    color = Muted,
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SettingsSectionHeader(Icons.Rounded.PlayCircle, "Playback") }
        item { ToggleRow("Autoplay", "Start videos automatically.", autoplay, onAutoplayChange) }
        item { ToggleRow("Captions", "Prefer captions when available.", captions, onCaptionsChange) }
        item { SettingChoiceRow("Video quality", "Preferred starting quality.", quality, listOf("Auto", "720p", "1080p"), onQualityChange) }
        item { ToggleRow("Remember position", "Keep local watch progress when supported.", rememberPosition, onRememberPositionChange) }

        item { SettingsSectionHeader(Icons.Rounded.Tune, "Interface") }
        item { ToggleRow("Show thumbnails", "Display thumbnails in feeds and history.", showThumbnails, onThumbnailsChange) }
        item { ToggleRow("Reduce animations", "Reduce UI motion and transitions.", reduceAnimations, onReduceAnimationsChange) }
        item { ToggleRow("High contrast", "Increase contrast of secondary surfaces.", highContrast, onHighContrastChange) }

        item { SettingsSectionHeader(Icons.Rounded.Security, "Privacy & data") }
        item {
            TvActionButton(
                icon = Icons.Rounded.Delete,
                text = "Clear watch history",
                onClick = onClearHistory,
            )
        }
        item {
            Text(
                "History is stored locally on this device. Google account features use Google's OAuth flow; PublivoreTube does not ask for your Google password.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }

        item { SettingsSectionHeader(Icons.Rounded.Info, "About") }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30283D), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = SurfaceDefaults.colors(containerColor = Bg),
            ) {
                Text(
                    "PublivoreTube 0.1 • Android TV • YouTube Data API",
                    modifier = Modifier.padding(18.dp),
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    TvActionButton(
        icon = if (checked) Icons.Rounded.Security else Icons.Rounded.Settings,
        text = title + if (checked) "  • On" else "  • Off",
        onClick = { onChange(!checked) },
    )
}

@Composable
private fun SettingChoiceRow(
    title: String,
    subtitle: String,
    value: String,
    values: List<String>,
    onChange: (String) -> Unit,
) {
    val next = values[(values.indexOf(value) + 1) % values.size]
    TvActionButton(
        icon = Icons.Rounded.Tune,
        text = title + ": " + value,
        onClick = { onChange(next) },
    )
}

@Composable
private fun SettingsSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier.padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = AccentStrong, modifier = Modifier.size(22.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun SignInPanel(
    title: String,
    message: String,
    onSignIn: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = SurfaceDefaults.colors(containerColor = Panel),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Muted)
            TvActionButton(Icons.Rounded.AccountCircle, "Sign in with Google", onSignIn)
        }
    }
}

@Composable
private fun EmptyPanel(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = SurfaceDefaults.colors(containerColor = Panel),
    ) {
        Column(modifier = Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
private fun TvActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = ButtonDefaults.shape(Pill),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF25202F),
            contentColor = Color.White,
            focusedContainerColor = AccentStrong,
            focusedContentColor = Bg,
        ),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, null, modifier = Modifier.size(21.dp))
            Text(text)
        }
    }
}

@Composable
private fun LaunchedOnceFocus(requester: FocusRequester) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        requester.requestFocus()
    }
}
