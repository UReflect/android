package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.adapters.ImageAdapter
import io.ureflect.app.fragments.BackPressedFragment.Companion.HANDLED
import io.ureflect.app.fragments.BackPressedFragment.Companion.NOT_HANDLED
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import kotlinx.android.synthetic.main.fragment_facial_setup.*
import kotlinx.android.synthetic.main.view_image.*
import java.io.File
import java.io.IOException
import java.util.*

@SuppressLint("ValidFragment")
class FacialRecognitionSetupFragment(var next: () -> Unit, var upload: (String, () -> Unit) -> Unit) : CoordinatorRootFragment(), BackPressedFragment {
    private lateinit var messages: List<String>
    private var names = Arrays.asList(
//            "smile",
//            "angry",
            "neutral",
            "neutral_left",
            "neutral_right"
    )
    private var images = ArrayList<String>()
    private lateinit var imageFilePath: String
    private lateinit var thatContext: Context
    private lateinit var adapter: ImageAdapter
    private var hasContext = false
    private var step = 0
    private var waitingForNext = false

    companion object {
        private const val CAMERA_REQUEST_CODE = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_facial_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messages = Arrays.asList(
//                getString(R.string.smile_text),
//                getString(R.string.angry_text),
                getString(R.string.neutral_text),
                getString(R.string.neutral_left_text),
                getString(R.string.neutral_right_text)
        )
        setupUI()
        context?.let { context ->
            thatContext = context
            hasContext = true
            setupCamera()
        } ?: run {
            errorSnackbar(getRoot(), getString(R.string.generic_error))
        }
    }

    private fun checkPrerequisites(): Boolean {
        thatContext.packageManager?.let {
            if (it.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return true
            }
        }
        return false
    }

    private fun setupLegend() {
        tvMsg.text = messages[step]
        btnNext.visibility = if (step <= images.size) View.GONE else View.VISIBLE
    }

    private fun setupCamera() {
        if (!checkPrerequisites()) {
            errorSnackbar(getRoot(), getString(R.string.facial_no_camera_error))
            return
        }

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        adapter = ImageAdapter(images, px)
        rvPhotos.adapter = adapter
        rvPhotos.layoutManager = LinearLayoutManager(thatContext, LinearLayoutManager.HORIZONTAL, false)
        rvPhotos.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL, true))
        ivPreview.setOnClickListener {
            try {
                val imageFile = createImageFile()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(thatContext.packageManager) != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(thatContext, "io.ureflect.app" + ".fileprovider", imageFile))
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    return@setOnClickListener
                }
            } catch (e: IOException) {

            }
            errorSnackbar(getRoot(), getString(R.string.facial_no_camera_error))
        }
        setupLegend()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    btnNext.visibility = View.VISIBLE
                    if (step < images.size) {
                        images[step] = imageFilePath
                        adapter.notifyItemChanged(step)
                    } else {
                        images.add(imageFilePath)
                    }
                }
            }
            else -> errorSnackbar(root, getString(R.string.generic_error))
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = thatContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile(names[step], ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    private fun setupUI() {
        btnNext.transformationMethod = null
        btnNext.setOnClickListener {
            if (hasContext) {
                upload(imageFilePath) {
                    if (waitingForNext) {
                        next()
                    }
                }
                step++
                when (step) {
                    messages.size -> {
                        waitingForNext = true
                        step--
                    }
                    else -> setupLegend()
                }
            } else {
                next()
            }
        }

        //Wait for root to have a width
        root.post {
            val side = (root.measuredWidth / 2)
            ivPreview.layoutParams = ivPreview.layoutParams
                    .apply { height = side }
                    .apply { width = side }
        }
    }

    override fun backPressed(): Boolean {
        val stepWhenPressed = step
        if (step != 0) {
            step--
            waitingForNext = false
            setupLegend()
        }
        return when (stepWhenPressed) {
            0 -> NOT_HANDLED
            else -> HANDLED
        }
    }
}