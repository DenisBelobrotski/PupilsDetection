package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class ImageDetectorActivity : DetectorActivity() {
    private val imageFileChooser by lazy { FileChooser(this, "image", "*") }
    private val imageView by lazy { findViewById<ImageView>(R.id.image_detector_image_view) }
    private val chooseImageButton by lazy { findViewById<Button>(R.id.choose_image_button) }
    private val processImageButton by lazy { findViewById<Button>(R.id.process_image_button) }

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
            Toast.makeText(this, "Bad image uri.", Toast.LENGTH_LONG).show();
            return
        }

        val chosenBitmap = FileSystemUtils.loadUserBitmap(this, imageUri)

        if (chosenBitmap == null) {
            Toast.makeText(this, "Bad loaded bitmap.", Toast.LENGTH_LONG).show();
            return
        }

        faceDetector.processBitmap(chosenBitmap)

        imageView.setImageBitmap(chosenBitmap)
    }
}
