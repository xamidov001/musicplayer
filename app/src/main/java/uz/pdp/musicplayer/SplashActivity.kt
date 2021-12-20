package uz.pdp.musicplayer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.SortedList
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import uz.pdp.musicplayer.adapters.RecAdapter
import uz.pdp.musicplayer.classes.MusicClass
import uz.pdp.musicplayer.databinding.ActivitySplashBinding
import uz.pdp.musicplayer.preference.MyShared

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    lateinit var list: ArrayList<MusicClass>
    private val gson = Gson()
    private var myShader = MyShared
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myShader = MyShared.getInstance(this)
        handler = Handler(Looper.getMainLooper())

        binding.apply {

            handler.postDelayed({permission()}, 500)

        }

    }

    fun permission() {
        askPermission( Manifest.permission.READ_EXTERNAL_STORAGE){
            list = ArrayList()
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val str = arrayOf(MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION)
            val cursor = contentResolver?.query(
                uri,
                str,
                "${MediaStore.Audio.Media.IS_MUSIC}=1",
                null,
                null
            )
            if (cursor!!.moveToFirst() && cursor != null) {
                do {
                    val author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val musicUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val album_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

                    val uripath = Uri.parse("content://media/external/audio/albumart")
                    val image_path = ContentUris.withAppendedId(uripath, album_id)

                    if (musicUri.endsWith(".mp3")) {
                        list.add(
                            MusicClass(
                                title,
                                author,
                                musicUri,
                                image_path.toString(),
                                duration,
                                date = 0
                            )
                        )
                    }
                } while (cursor.moveToNext())
            }

            myShader.setList(gson.toJson(list), "list")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(this)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if(e.hasForeverDenied()) {
                e.goToSettings();
            }
        }
    }
}