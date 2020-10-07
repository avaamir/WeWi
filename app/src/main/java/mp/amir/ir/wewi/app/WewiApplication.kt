package mp.amir.ir.wewi.app

import android.app.Application
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import mp.amir.ir.wewi.R

class WewiApplication : Application() {

    //Typefaces
    val iransans: Typeface by lazy {
        ResourcesCompat.getFont(this,
            R.font.iransans
        )!!
    }
    val iransansMedium: Typeface by lazy {
        ResourcesCompat.getFont(this,
            R.font.iransans_medium
        )!!
    }
    val iransansLight: Typeface by lazy {
        ResourcesCompat.getFont(this,
            R.font.iransans_light
        )!!
    }
    val belham: Typeface by lazy {
        ResourcesCompat.getFont(this, R.font.belham)!!
    }
}