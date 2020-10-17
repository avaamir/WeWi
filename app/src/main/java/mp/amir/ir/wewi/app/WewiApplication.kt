package mp.amir.ir.wewi.app

import android.app.Application
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.multidex.MultiDexApplication
import mp.amir.ir.wewi.R
import mp.amir.ir.wewi.respository.RemoteRepo
import mp.amir.ir.wewi.respository.apiservice.ApiService
import org.conscrypt.Conscrypt
import java.security.Security

class WewiApplication : MultiDexApplication() {

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

    override fun onCreate() {
        super.onCreate()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        //
        ApiService.setContext(applicationContext)
        RemoteRepo.setContext(applicationContext)

    }

}