package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val ProcessedVideosFolderName = "processed_videos"
private const val FileNameCommonPart = "processed_video_file"
private const val FileNameDateFormat = "yyyy-MM-dd_HH:mm:ss:SSS"

class VideoDetectorActivity : DetectorActivity() {
    private val videoFileChooser by lazy { FileChooser(this, "video", "*") }
    private val videoView by lazy { findViewById<ImageView>(R.id.video_detector_image_view) }
    private val chooseVideoButton by lazy { findViewById<Button>(R.id.choose_video_button) }
    private val processVideoButton by lazy { findViewById<Button>(R.id.process_video_button) }
    private val fileNameDateFormatter by lazy { SimpleDateFormat(FileNameDateFormat) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_detector)

        chooseVideoButton.setOnClickListener {
            videoFileChooser.choose()
        }

        processVideoButton.setOnClickListener {
            processVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        videoFileChooser.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processVideo() {
        val userFileUri = videoFileChooser.lastChosenFileUri.value

        if (userFileUri == null) {
            Toast.makeText(this, "Video file wasn't chosen.", Toast.LENGTH_LONG).show()
            return
        }

        val userFileInputStream = FileSystemUtils.openUserFileInputStream(this, userFileUri)

        val inputFileName = FileSystemUtils.getUserFileName(this, userFileUri) ?: userFileUri.toString()
        val fileExtension = inputFileName.substring(inputFileName.lastIndexOf(".") + 1);

        val outputFile = createVideoFile(fileExtension)

        userFileInputStream?.use {
            val videoFaceDetector = VideoFaceDetector(userFileInputStream, outputFile, faceDetector, false)
            videoFaceDetector.use {
                videoFaceDetector.detectFaces()
            }
        }

        Toast.makeText(this, "Processed file saved as\n" +
                "\"${outputFile.absolutePath}\"", Toast.LENGTH_LONG).show()
    }

    private fun createVideoFile(fileExtension: String): File {
        val fileName = generateVideoFileName(fileExtension)
        val filesDirectory = getExternalFilesDir(null)

        val videoFilesDirectory = File(filesDirectory, ProcessedVideosFolderName)
        if (!videoFilesDirectory.exists()) {
            videoFilesDirectory.mkdir()
        }

        val file = File(videoFilesDirectory, fileName)

        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        return file
    }

    private fun generateVideoFileName(fileExtension: String): String {
        val currentTime = Calendar.getInstance().time
        val formattedDate = fileNameDateFormatter.format(currentTime)
        return "${FileNameCommonPart}_${formattedDate}.${fileExtension}"
    }
}
