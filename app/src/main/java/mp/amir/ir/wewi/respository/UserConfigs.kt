package mp.amir.ir.wewi.respository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import mp.amir.ir.wewi.models.User
import mp.amir.ir.wewi.respository.apiservice.ApiService
import mp.amir.ir.wewi.respository.persistance.userdb.UserRepo
import mp.amir.ir.wewi.respository.sharepref.PrefManager

object UserConfigs {
    private val userLive = MutableLiveData<User?>(null)
    val user: LiveData<User?> get() = userLive
    val isLoggedIn get() = user.value != null

    fun init() {
        UserRepo.users.observeForever { users ->
            if (users.isNotEmpty()) {
                val user = users[0]
                println("debug: UserConfigs: $user")
                ApiService.setToken(user.token)
                userLive.value = user
            }
        }
    }

    val isUserLoggedIn get() = PrefManager.isUserLoggedIn


    fun loginUser(user: User, blocking: Boolean = false) {
        PrefManager.userLoggedIn()
        if (!blocking) {
            if (UserConfigs.user.value != user) {
                UserRepo.clearAndInsert(user)
            }
        } else {
            UserRepo.clearAndInsertBlocking(user)
        }
    }

    fun logout() {
        PrefManager.userLoggedOut()
        /*userLive.value?.let {
            PrefsRepo.flush()
            UserRepo.deleteAll()
        }
        userLive.postValue(null)*/
    }


}