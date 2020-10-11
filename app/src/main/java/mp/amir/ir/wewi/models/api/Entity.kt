package mp.amir.ir.wewi.models.api

import com.google.gson.annotations.SerializedName

class Entity<T> (
    @SerializedName("entity")
    val entity: T,
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val message: String,
    @SerializedName("stat")
    val status: String,
    /*@SerializedName("meta")
    val meta:*/
) {
    val isSucceed: Boolean get() = code == 0
}