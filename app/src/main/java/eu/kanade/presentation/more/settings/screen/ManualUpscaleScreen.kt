package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.download.RealCuganUpscaler
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ManualUpscaleScreen : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val downloadManager = remember { Injekt.get<DownloadManager>() }
        
        val downloadedMangaList = remember {
            val baseDir = downloadManager.provider.downloadsDir
            baseDir?.listFiles()?.filter { it.isDirectory } ?: emptyList()
        }

        var processingMangaName by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = { 
                Text("Manual AI Image Upscaling", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp)) 
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(downloadedMangaList) { directory ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(directory.name, modifier = Modifier.weight(1f))
                            
                            Button(
                                enabled = processingMangaName == null,
                                onClick = {
                                    scope.launch {
                                        processingMangaName = directory.name
                                        directory.walkTopDown().filter { it.extension in listOf("jpg", "jpeg", "png") }.forEach { imgFile ->
                                            RealCuganUpscaler.upscaleImageFile(context, imgFile)
                                        }
                                        processingMangaName = null
                                    }
                                }
                            ) {
                                Text(if (processingMangaName == directory.name) "Upscaling..." else "Start AI 2x")
                            }
                        }
                    }
                }
            }
        }
    }
}
