package uz.pdp.musicplayer.preference

import android.content.Context
import android.content.SharedPreferences

object MyShared {

    val myShared = MyShared
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    fun getInstance(context: Context): MyShared {
        if (sharedPreferences == null) {
            context.getSharedPreferences("file", Context.MODE_PRIVATE).also { sharedPreferences = it }
        }
        return myShared
    }

    fun getList(str: String): String = sharedPreferences?.getString(str, "")!!

    fun isStart(str: String): Boolean = sharedPreferences?.getBoolean(str, false)!!

    fun setList(text: String, str: String) {
        editor = sharedPreferences?.edit()
        editor?.putString(str, text)?.commit()
    }

    fun setStart(a: Boolean, str: String) {
        editor = sharedPreferences?.edit()
        editor?.putBoolean(str, a)?.commit()
    }
}