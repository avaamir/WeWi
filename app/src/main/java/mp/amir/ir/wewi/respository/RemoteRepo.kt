package mp.amir.ir.wewi.respository

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import mp.amir.ir.wewi.models.User
import mp.amir.ir.wewi.models.api.Entity
import mp.amir.ir.wewi.respository.apiservice.ApiService
import mp.amir.ir.wewi.utils.general.RunOnceLiveData
import mp.amir.ir.wewi.utils.general.launchApi
import okhttp3.*
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1


object RemoteRepo {
    private lateinit var serverJobs: CompletableJob

    private fun <ResM, ReqM> apiReq(
        request: ReqM,
        requestFunction: KSuspendFunction1<ReqM, Response<Entity<ResM>>>,
        repoLevelHandler: ((Response<Entity<ResM>>) -> (Unit))? = null
    ): RunOnceLiveData<Entity<ResM>?> {
        if (!RemoteRepo::serverJobs.isInitialized || !serverJobs.isActive) serverJobs = Job()
        return object : RunOnceLiveData<Entity<ResM>?>() {
            override fun onActiveRunOnce() {
                CoroutineScope(IO + serverJobs).launchApi({
                    val response = requestFunction(request)
                    repoLevelHandler?.invoke(response)
                    CoroutineScope(Main).launch {
                        value = response.body()
                    }
                }) {
                    CoroutineScope(Main).launch {
                        value = null
                    }
                }
            }
        }
    }

    private fun <ResM> apiReq(
        requestFunction: KSuspendFunction0<Response<Entity<ResM>>>,
        repoLevelHandler: ((Response<Entity<ResM>>) -> (Unit))? = null  //This will only excute if LiveData has an observer, because it called on active method
    ): RunOnceLiveData<Entity<ResM>?> {
        if (!RemoteRepo::serverJobs.isInitialized || !serverJobs.isActive) serverJobs = Job()
        return object : RunOnceLiveData<Entity<ResM>?>() {
            override fun onActiveRunOnce() {
                CoroutineScope(IO + serverJobs).launchApi({
                    val response = requestFunction()
                    repoLevelHandler?.invoke(response)
                    CoroutineScope(Main).launch {
                        value = response.body()
                    }
                }) {
                    CoroutineScope(Main).launch {
                        value = null
                    }
                }
            }
        }
    }


    fun login(username: String, password: String, onResponse: (Entity<User?>?) -> Unit) {
        if (!RemoteRepo::serverJobs.isInitialized || !serverJobs.isActive) serverJobs = Job()
        CoroutineScope(IO + serverJobs).launchApi({
            val firstResponse = firstLogin(username, password)
            if (firstResponse.isSuccessful) {
                val userEntity = firstResponse.body()
                val hotToken = firstResponse.headers()["HotToken"]
                if (!hotToken.isNullOrBlank()) {
                    val secondResponse = secondLogin(username, hotToken)
                    if (secondResponse.isSuccessful || secondResponse.isRedirect) {
                        //TODO inja check beshavad ke aya response miad ya na
                        //TODO momkene niaz bashe ping bedim be ye addressi ke befahmim net vasl hast ya na
                        withContext(Main) {
                            //TODO login succeed -> onResponse(UserEntity)
                        }
                    } else {
                        withContext(Main) {
                            //TODO onResponse(???)
                        }
                    }
                } else {
                    withContext(Main) {
                        //TODO onResponse(NO_HOT_TOKEN)
                    }
                }

            } else {
                withContext(Main) {
                    //TODO onrResponse(FAILED_FIRST_LOGIN) :: Ex: not authenticated, ..
                }
            }
        }, {
            //TODO onResponse(it.message)
        })
    }

    private suspend fun firstLogin(
        username: String,
        password: String,
    ) = ApiService.client.login(
        username = username,    //"09131566906",
        password = password,    //"762121",
        action = "btnLogin",
        location = "loginFirst",
        usernameType = "num"
    )


    private suspend fun secondLogin(
        username: String,
        hotToken: String,
    ): okhttp3.Response {
        val client = OkHttpClient()

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("password", hotToken)
            .build()
        val request: Request = Request.Builder()
            .url("http://dc.wewi.ir/login.html")
            .method("POST", body)
            .build()

        return client.newCall(request).execute()
    }

    fun logout(onResponse: (String?) -> Unit) {
        val client = OkHttpClient()

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM).build()
        val request: Request = Request.Builder()
            .url("http://dc.wewi.ir/logout.html")
            .method("POST", body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                onResponse("${response.code}")
            }

            override fun onFailure(call: Call, e: IOException) {
                onResponse(e.message)
            }
        })
    }
}