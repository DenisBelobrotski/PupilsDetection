package by.swiftdrachen.pupilsdetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val faceImageView = findViewById<ImageView>(R.id.face_image_view)
        val faceImageBitmap = loadBitmap("test_face.jpg")
        faceImageView.setImageBitmap(faceImageBitmap)
    }

    fun loadBitmap(assetPath: String) : Bitmap {
        val assetInputStream = assets.open(assetPath)
        return BitmapFactory.decodeStream(assetInputStream)
    }
}
