package mp.amir.ir.wewi.utils.general
import androidx.lifecycle.LiveData

abstract class RunOnceLiveData<T> : LiveData<T>() {
    abstract fun onActiveRunOnce()

    private var isFirstTime = true
    override fun onActive() {
        if (isFirstTime) {
            isFirstTime = false
            onActiveRunOnce()
        }
    }

    fun activateAgain() {
        isFirstTime = true
    }
}