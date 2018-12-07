package io.ureflect.app.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.view_image.view.*

class ImageAdapter(val data: List<String>, private val margin: Int) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    /**
     * Number of mirror icon on the screen
     */
    companion object {
        private const val NB = 3
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bind(data[position])

    override fun getItemCount(): Int = data.size

    private fun resize(parent: ViewGroup, v: View) {
        val side = ((parent.measuredWidth - margin * (NB + 1)) / NB)
        v.layoutParams = RecyclerView.LayoutParams(side, side)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder = ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_image, parent, false).apply { resize(parent, this) })

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(path: String) = with(itemView) {
            ivPreview.setImageBitmap(getScaledBitmap(this, path))
        }

        companion object {
            fun getScaledBitmap(v: View, path: String): Bitmap {
                val imageViewWidth = v.layoutParams.width
                val imageViewHeight = v.layoutParams.height
                val bmOptions = BitmapFactory.Options()
                bmOptions.inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, bmOptions)
                val bitmapWidth = bmOptions.outWidth
                val bitmapHeight = bmOptions.outHeight
                val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)
                bmOptions.inJustDecodeBounds = false
                bmOptions.inSampleSize = scaleFactor
                return BitmapFactory.decodeFile(path, bmOptions)
            }
        }
    }
}
