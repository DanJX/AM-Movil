package com.example.appcamara

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private var capturedImage: Bitmap? = null
    private val REQUEST_IMAGE_CAPTURE = 1

    private val defaultImage: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.imgpreview)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonTakePicture = findViewById<Button>(R.id.takePicture)
        saveButton = findViewById(R.id.savePicture)
        imageView = findViewById(R.id.showPicture)
        resetButton = findViewById(R.id.resetImage)

        imageView.setImageBitmap(defaultImage)

        saveButton.isEnabled = false

        botonTakePicture.setOnClickListener {
            dispatchTakePictureIntent()
        }

        saveButton.setOnClickListener {
            capturedImage?.let {
                saveImageToGallery(it)
            }
        }

        resetButton.setOnClickListener {
            resetImage()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            capturedImage = imageBitmap
            imageView.setImageBitmap(imageBitmap)
            saveButton.isEnabled = true // Habilitar el botón de guardar despues
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Usar MediaStore para Android 10+ (API 29+)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        } else {
            // Método para versiones anteriores
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
    }

    private fun resetImage() {
        imageView.setImageBitmap(defaultImage)
        capturedImage = null
        saveButton.isEnabled = false
    }
}
