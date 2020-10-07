package mp.amir.ir.wewi.respository

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mp.amir.ir.wewi.models.api.Entity
import mp.amir.ir.wewi.respository.apiservice.ApiService
import mp.amir.ir.wewi.utils.general.RunOnceLiveData
import mp.amir.ir.wewi.utils.general.launchApi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

    fun firstLogin(username: String, password: String, onResponse: (Entity<Unit>?) -> Unit) {
        if (!RemoteRepo::serverJobs.isInitialized || !serverJobs.isActive) serverJobs = Job()
        CoroutineScope(IO + serverJobs).launch {
            val response = ApiService.client.login(
                username = username,    //"09131566906",
                password = password,    //"762121",
                action = "btnLogin",
                location = "loginFirst",
                usernameType = "num"
            )
           //TODO onResponse(response.body() + response.headers()["HotToken"])
        }
    }

    fun secondLogin(username: String, password: String, onResponse: (String?) -> Unit) {
        val client = OkHttpClient()

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("password", password)
            .build()
        val request: Request = Request.Builder()
            .url("http://dc.wewi.ir/login.html")
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