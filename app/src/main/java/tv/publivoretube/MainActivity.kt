package tv.publivoretube

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import tv.publivoretube.data.DemoVideoRepository
import tv.publivoretube.ui.HomeScreen
import tv.publivoretube.ui.theme.PublivoreTubeTheme

class MainActivity : ComponentActivity() {
    private val repository = DemoVideoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PublivoreTubeTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        videos = repository.homeStatic(),
                        onVideoSelected = { video ->
                            Toast.makeText(
                                this@MainActivity,
                                "Selected: ${video.title}",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    )
                }
            }
        }
    }
}
