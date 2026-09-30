package tv.publivoretube.player

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class ExtractorDownloader : Downloader() {
    override fun execute(request: Request): Response {
        val connection = (URL(request.url()).openConnection() as HttpURLConnection).apply {
            requestMethod = request.httpMethod()
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 20_000
            useCaches = false
            setRequestProperty("User-Agent", USER_AGENT)

            request.headers().forEach { (name, values) ->
                if (name != null && values.isNotEmpty()) {
                    setRequestProperty(name, values.joinToString(","))
                }
            }

            if (request.httpMethod().uppercase(Locale.US) == "POST") {
                doOutput = true
                request.dataToSend()?.let { body ->
                    outputStream.use { it.write(body) }
                }
            }
        }

        return try {
            val code = connection.responseCode
            val responseStream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            Response(
                code,
                connection.responseMessage.orEmpty(),
                connection.headerFields.filterKeys { it != null } as Map<String, List<String>>,
                body,
                connection.url?.toString(),
            )
        } catch (error: IOException) {
            throw error
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11; TV) AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36"
    }
}
