import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.zalgneyhmusic.R

object ImageUtils {
    fun loadImage(view: ImageView, url: String?, placeholder: Int = R.mipmap.ic_launcher) {
        // fix in case of undefined url
        val validUrl = if (url != null && !url.contains("undefined")) url else null

        Glide.with(view.context)
            .load(validUrl)
            .placeholder(placeholder)
            .error(placeholder)
            .fallback(placeholder)
            .into(view)
    }

    fun loadImageRounded(
        view: ImageView,
        url: String?,
        placeholder: Int = R.mipmap.ic_launcher_round
    ) {
        // client side fix in case of undefined url
        val validUrl = if (url != null && !url.contains("undefined")) url else null

        Glide.with(view.context)
            .load(validUrl)
            .circleCrop()
            .placeholder(placeholder)
            .error(placeholder)
            .fallback(placeholder)
            .into(view)
    }
}