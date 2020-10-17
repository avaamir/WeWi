package mp.amir.ir.wewi.respository.apiservice

import mp.amir.ir.wewi.models.User
import mp.amir.ir.wewi.models.api.Entity
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface WewiClient {
    //"app.wewi.ir"
    @POST(" ")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("loc") location: String,
        @Field("act") action: String,
        @Field("username_type") usernameType: String
    ): Response<Entity<Any>>

}

