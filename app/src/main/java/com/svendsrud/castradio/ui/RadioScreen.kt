package com.svendsrud.castradio.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.svendsrud.castradio.R
import com.svendsrud.castradio.model.RadioStation
import com.svendsrud.castradio.model.StationRepository
import com.svendsrud.castradio.ui.components.NowPlayingBar
import com.svendsrud.castradio.ui.components.StationCard
import com.svendsrud.castradio.ui.theme.CastRadioTheme

@Composable
fun RadioScreen(viewModel: RadioViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RadioScreenContent(
        state = state,
        onStationClick = viewModel::onStationSelected,
        onStopCasting = viewModel::stopCasting,
        onMessageShown = viewModel::onMessageShown,
        onDevicePickerLaunched = viewModel::onDevicePickerLaunched
    )
}

@Composable
private fun RadioScreenContent(
    state: RadioUiState,
    onStationClick: (RadioStation) -> Unit,
    onStopCasting: () -> Unit,
    onMessageShown: () -> Unit,
    onDevicePickerLaunched: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var mediaRouteButton by remember { mutableStateOf<MediaRouteButton?>(null) }

    state.messageRes?.let { messageRes ->
        LaunchedEffect(messageRes) {
            snackbarHostState.showSnackbar(context.getString(messageRes))
            onMessageShown()
        }
    }

    if (state.devicePickerRequested) {
        LaunchedEffect(Unit) {
            mediaRouteButton?.performClick()
            onDevicePickerLaunched()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        // Toppen håndteres av headeren selv, slik at gradienten tegnes bak statuslinjen.
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        bottomBar = {
            AnimatedVisibility(
                visible = state.activeStation != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                // Behold siste stasjon slik at innholdet ikke blir tomt under exit-animasjonen.
                var displayedStation by remember { mutableStateOf(state.activeStation) }
                if (state.activeStation != null) {
                    displayedStation = state.activeStation
                }
                displayedStation?.let { station ->
                    NowPlayingBar(
                        station = station,
                        isBuffering = state.isBuffering,
                        onStopCasting = onStopCasting
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RadioHeader(
                castAvailable = state.castAvailable,
                onButtonCreated = { mediaRouteButton = it }
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.stations, key = { it.streamUrl }) { station ->
                    StationCard(
                        station = station,
                        isActive = station == state.activeStation,
                        isBuffering = state.isBuffering,
                        onClick = { onStationClick(station) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioHeader(
    castAvailable: Boolean,
    onButtonCreated: (MediaRouteButton) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cast),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 20.dp)
                .alpha(0.14f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            // MediaRouteButton er en View — Cast SDK-et har ingen Compose-variant.
            if (castAvailable && !LocalInspectionMode.current) {
                AndroidView(
                    factory = { viewContext ->
                        MediaRouteButton(viewContext).also { button ->
                            CastButtonFactory.setUpMediaRouteButton(viewContext, button)
                            onButtonCreated(button)
                        }
                    }
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
private fun RadioScreenPreview() {
    CastRadioTheme(dynamicColor = false) {
        RadioScreenContent(
            state = RadioUiState(
                stations = StationRepository.stations,
                activeStation = StationRepository.stations.first(),
                isBuffering = false
            ),
            onStationClick = {},
            onStopCasting = {},
            onMessageShown = {},
            onDevicePickerLaunched = {}
        )
    }
}
