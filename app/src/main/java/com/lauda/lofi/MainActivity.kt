package com.lauda.lofi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lauda.lofi.data.LocalMusicRepository
import com.lauda.lofi.data.Track
import com.lauda.lofi.player.PlayerController
import com.lauda.lofi.ui.LofiTheme
import com.lauda.lofi.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LofiTheme { LofiApp() } }
    }
}

@Composable
private fun LofiApp() {
    val context = LocalContext.current
    val player = remember { PlayerController(context) }
    var selected by remember { mutableStateOf("home") }
    var current by remember { mutableStateOf<Track?>(null) }
    var localTracks by remember { mutableStateOf(emptyList<Track>()) }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) localTracks = LocalMusicRepository(context.contentResolver).scan()
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val track = Track(
            id = "picked:$uri",
            title = uri.lastPathSegment ?: "Selected audio",
            artist = "Local file",
            localUri = uri.toString(),
            streamUrl = uri.toString()
        )
        current = track
        player.play(track)
    }

    DisposableEffect(Unit) { onDispose { player.release() } }

    Scaffold(
        containerColor = Color(0xFFFFFBF5),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFFFFBF5)) {
                NavigationBarItem(selected == "home", { selected = "home" }, icon = { Icon(Icons.Rounded.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected == "search", { selected = "search" }, icon = { Icon(Icons.Rounded.Search, null) }, label = { Text("Search") })
                NavigationBarItem(selected == "library", { selected = "library" }, icon = { Icon(Icons.Rounded.LibraryMusic, null) }, label = { Text("Library") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                "home" -> HomeScreen(
                    current = current,
                    onSearch = { selected = "search" },
                    onPick = { picker.launch(arrayOf("audio/*")) },
                    onPlay = { current = it; player.play(it) }
                )
                "search" -> SearchScreen(
                    onPlay = { current = it; player.play(it) }
                )
                "library" -> LibraryScreen(
                    tracks = localTracks,
                    onScan = {
                        val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                        else Manifest.permission.READ_EXTERNAL_STORAGE
                        permission.launch(p)
                    },
                    onPlay = { current = it; player.play(it) }
                )
            }
            current?.let {
                MiniPlayer(it, onClick = {}, onPause = { player.player.pause() })
            }
        }
    }
}

@Composable
private fun HomeScreen(
    current: Track?,
    onSearch: () -> Unit,
    onPick: () -> Unit,
    onPlay: (Track) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Lofi", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("A softer place to land", color = Color(0xFF777067))
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onSearch, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Search, null)
            Spacer(Modifier.width(8.dp))
            Text("Search music")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.AudioFile, null)
            Spacer(Modifier.width(8.dp))
            Text("Choose an audio file")
        }
        Spacer(Modifier.height(24.dp))
        Text("Lofi Sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Your music, your atmosphere.")
                Text(
                    "Search online tracks or open music stored on your device.",
                    color = Color(0xFF777067)
                )
            }
        }
    }
}

@Composable
private fun SearchScreen(onPlay: (Track) -> Unit) {
    val vm: SearchViewModel = viewModel()
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Search", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Song, artist or album") },
            trailingIcon = {
                IconButton(onClick = vm::search) { Icon(Icons.Rounded.Search, null) }
            }
        )
        Spacer(Modifier.height(16.dp))
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.results, key = { it.id }) { track ->
                TrackRow(track, onPlay)
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<Track>,
    onScan: () -> Unit,
    onPlay: (Track) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            FilledTonalButton(onClick = onScan) { Text("Scan") }
        }
        Spacer(Modifier.height(12.dp))
        if (tracks.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("No local songs indexed yet.", fontWeight = FontWeight.SemiBold)
                    Text("Tap Scan to read music from your device.", color = Color(0xFF777067))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tracks, key = { it.id }) { TrackRow(it, onPlay) }
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onPlay: (Track) -> Unit) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay(track) },
        headlineContent = { Text(track.title, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF4EEE5),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote, null, tint = Color(0xFF8B5E34))
                }
            }
        },
        trailingContent = {
            IconButton(onClick = { onPlay(track) }) {
                Icon(Icons.Rounded.PlayArrow, null)
            }
        }
    )
}

@Composable
private fun MiniPlayer(track: Track, onClick: () -> Unit, onPause: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        tonalElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, maxLines = 1, fontWeight = FontWeight.SemiBold)
                Text(track.artist, maxLines = 1, color = Color(0xFF777067))
            }
            IconButton(onClick = onPause) { Icon(Icons.Rounded.Pause, null) }
        }
    }
}
