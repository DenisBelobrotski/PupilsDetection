package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class ImageDetectorActivity : DetectorActivity() {
    private val imageFileChooser by lazy { FileChooser(this, "image", "*") }
    private val chooseImageButton by lazy { findViewById<Button>(R.id.choose_image_button) }
    private val processImageButton by lazy { findViewById<Button>(R.id.process_image_button) }
    private val sessionFileManager by lazy { SessionFileManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_detector)

        chooseImageButton.setOnClickListener {
            imageFileChooser.choose()
        }

        processImageButton.setOnClickListener {
            processImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageFileChooser.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processImage() {
        val imageUri = imageFileChooser.lastChosenFileUri.value

        if (imageUri == null) {
            Toast.makeText(this, "Bad image uri.", Toast.LENGTH_LONG).show()
            return
        }

        val chosenMat = OpenCvUtils.loadUserMat(this, imageUri)

        if (chosenMat == null) {
            Toast.makeText(this, "Bad loaded mat.", Toast.LENGTH_LONG).show()
            return
        }

        sessionFileManager.saveMat(chosenMat, "source_image")

        val faceCascade = OpenCvUtils.loadCascadeFromAssets(this, FACE_CASCADE_PATH)
        faceCascade?.let {
            val faceDetector = FaceDetector(faceCascade)
            val faces = faceDetector.getFaceMats(chosenMat)
            for (index in faces.indices) {
                val face = faces[index]
                sessionFileManager.saveMat(face, "detected_face_$index")
            }
        }
    }
}
