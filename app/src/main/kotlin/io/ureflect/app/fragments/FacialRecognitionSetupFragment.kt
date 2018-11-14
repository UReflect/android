package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_facial_setup.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ValidFragment")
class FacialRecognitionSetupFragment(var next: () -> Unit) : Fragment() {
    val CAMERA_REQUEST_CODE = 0
    lateinit var imageFilePath: String
    lateinit var that: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_facial_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        this.context?.let { context ->
            that = context
            setupCamera()
        } ?: run {
            Snackbar.make(root, getString(R.string.facial_no_camera_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show() //TODO : real error
        }
    }

    private fun checkPrerequisites(): Boolean {
        that.packageManager?.let {
            if (it.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return true
            }
        }
        return false
    }

    private fun setupCamera() {
        if (!checkPrerequisites()) {
            Snackbar.make(root, getString(R.string.facial_no_camera_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
            return
        }

        ivPreview.setOnClickListener {
            try {
                val imageFile = createImageFile()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(that.packageManager) != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(that, "io.ureflect.app" + ".fileprovider", imageFile))
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    return@setOnClickListener
                }
            } catch (e: IOException) {

            }
            Snackbar.make(root, getString(R.string.facial_no_camera_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show() //TODO : other error
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    ivPreview.setImageBitmap(setScaledBitmap())
                }
            }
            else -> Snackbar.make(root, getString(R.string.facial_no_camera_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show() //TODO : other error
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        val storageDir: File = that.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    private fun setScaledBitmap(): Bitmap {
        val imageViewWidth = ivPreview.width
        val imageViewHeight = ivPreview.height
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight
        val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        return BitmapFactory.decodeFile(imageFilePath, bmOptions)
    }

    private fun setupUI() {
        btnNext.transformationMethod = null
        btnNext.setOnClickListener {
            next()
        }
        root.post {
            val side = (root.measuredWidth / 2)
            ivPreview.layoutParams = ivPreview.layoutParams
                    .apply { height = side }
                    .apply { width = side }
        }
    }
}