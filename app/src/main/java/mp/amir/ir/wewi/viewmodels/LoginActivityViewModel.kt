package mp.amir.ir.wewi.viewmodels

import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import mp.amir.ir.wewi.models.User
import mp.amir.ir.wewi.models.api.Entity
import mp.amir.ir.wewi.respository.RemoteRepo

class LoginActivityViewModel : ViewModel() {

    private var username: String? = null
    private var password: String? = null

    private val _loginResponse = MutableLiveData<Entity<User?>?>()
    val loginResponse: LiveData<Entity<User?>?> = _loginResponse


    fun login(username: String, password: String) {
        this.username = username
        this.password = password
        RemoteRepo.login(username, password) {
            _loginResponse.value = it
        }
    }

    fun loginAgain() {
        login(username!!, password!!)
    }

}