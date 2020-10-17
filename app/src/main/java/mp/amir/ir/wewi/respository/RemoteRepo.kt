package mp.amir.ir.wewi.respository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import mp.amir.ir.wewi.models.User
import mp.amir.ir.wewi.models.api.Entity
import mp.amir.ir.wewi.respository.apiservice.ApiService
import mp.amir.ir.wewi.utils.general.RunOnceLiveData
import mp.amir.ir.wewi.utils.general.launchApi
import okhttp3.*
import okhttp3.FormBody
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1


object RemoteRepo {
    private lateinit var context: Context
    private lateinit var serverJobs: CompletableJob

    fun setContext(context: Context) {
        this.context = context
    }

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
                println("debug: 0 -> HotToken:$hotToken")
                if (!hotToken.isNullOrBlank()) {
                    val secondResponse = secondLogin(username, hotToken)
                    if (secondResponse.isSuccessful || secondResponse.isRedirect) {
                        //TODO inja check beshavad ke aya response miad ya na
                        //TODO momkene niaz bashe ping bedim be ye addressi ke befahmim net vasl hast ya na
                        println("debug:1-> login Succeed -> isSuccessful=${secondResponse.isSuccessful}, isRedirect=${secondResponse.isRedirect}")
                        //println("debug:1-> body: ${secondResponse.body?.string()}")


                        withContext(Main) {
                            Toast.makeText(context, "loginSucceed", Toast.LENGTH_SHORT).show()
                            val url = "http://swiftcard.ir/chkConnected.php"
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            context.startActivity(i)
                            //TODO login succeed -> onResponse(UserEntity)
                        }

                        /*var checkResponse = checkConnection()
                        repeat(10) {
                            println("debug:1-> checkConnection: times=$it")
                            val str = checkResponse.body?.string()
                            println("----------------------------------")
                            print(str)
                            println("----------------------------------")
                            println("debug: ${checkResponse.isRedirect}, ${checkResponse.code}")
                            if (str?.contains("wewiConnected") == true) {
                                withContext(Main) {
                                    Toast.makeText(context, "loginSucceed", Toast.LENGTH_SHORT).show()
                                    val url = "http://www.google.com"
                                    val i = Intent(Intent.ACTION_VIEW)
                                    i.data = Uri.parse(url)
                                    context.startActivity(i)
                                    //TODO login succeed -> onResponse(UserEntity)
                                }
                                println("debug:1-> checkConnection: wewiConnected")
                                return@repeat
                            } else {
                                println("debug:1-> checkConnection: wewi not Connected, req again")
                                checkResponse = checkConnection()
                            }
                        }*/
                    } else {
                        withContext(Main) {
                            println("debug:2-> second login failed")
                            //TODO onResponse(???)
                        }
                    }
                } else {
                    withContext(Main) {
                        println("debug:3 -> HotToken is null")
                        //TODO onResponse(NO_HOT_TOKEN)
                    }
                }

            } else {
                withContext(Main) {
                    println("debug:4-> first login failed")
                    //TODO onrResponse(FAILED_FIRST_LOGIN) :: Ex: not authenticated, ..
                }
            }
        }, {
            println("debug:5-> first login exception->${it.message}")
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

        val body2: RequestBody = FormBody.Builder()
            .add("username", username)
            .add("password", hotToken)
            .build()

        /*val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("password", hotToken)
            .build()*/

        val request: Request = Request.Builder()
            .url("http://dc.wewi.ir/login.html")
            .method("POST", body2)
            .build()

        return client.newCall(request).execute()
    }



    fun checkConnection(): okhttp3.Response {
        val client = OkHttpClient()

        //val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM).build()
        val request: Request = Request.Builder()
            .url("http://swiftcard.ir/chkConnected.php")
            .method("GET", null)
            .build()

        return client.newCall(request).execute()
    }



    fun logout(onResponse: (String?) -> Unit) {
        val client = OkHttpClient()


        val body: RequestBody = FormBody.Builder().build()
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