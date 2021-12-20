package uz.pdp.musicplayer.ui

import android.Manifest
import android.content.ContentUris
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import uz.pdp.musicplayer.R
import uz.pdp.musicplayer.adapters.RecAdapter
import uz.pdp.musicplayer.classes.MusicClass
import uz.pdp.musicplayer.classes.MusicManager
import uz.pdp.musicplayer.databinding.FragmentHome2Binding
import uz.pdp.musicplayer.preference.MyShared
import com.google.gson.reflect.TypeToken as TypeToken

class HomeFragment : Fragment(R.layout.fragment_home2) {
    private val binding by viewBinding(FragmentHome2Binding::bind)
    lateinit var list: ArrayList<MusicClass>
    lateinit var recAdapter: RecAdapter
    var myShader = MyShared
    var gson = Gson()
    private var last_index = -1
    private var duration = -1
    private var ispalying = false
    private var last_const : ConstraintLayout? = null
    private var last_image : ImageView? = null
    lateinit var music : MediaPlayer
    private lateinit var musicManager: MusicManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        music = MediaPlayer()
        myShader = MyShared.getInstance(requireContext())

        loadList()


        binding.apply {
            musicManager = MusicManager(requireContext())

            startPause.setOnClickListener {
                rec_pause()
            }

            nextBtn.setOnClickListener {
                if (last_index != list.size-1) {
                    rec_start(last_index+1)
                }
            }

            backBtn.setOnClickListener {
                if (last_index != 0) {
                    rec_start(last_index-1)
                }
            }

            layout.setOnClickListener {
                setData()
                findNavController().navigate(R.id.playFragment)
            }

        }
    }

    private fun setData() {
        myShader.setList("$last_index", "index")
        myShader.setList("${music.currentPosition}", "duration")
        if (music.isPlaying) {
            myShader.setStart(true, "playing")
        } else {
            myShader.setStart(false, "playing")
        }
        myShader.setStart(true, "fragment")
        releaseMp()
    }

    private fun musicDetail() {
        music.setOnCompletionListener {
            binding.nextBtn.performClick()
        }
        if (myShader.isStart("looping")) {
            music.isLooping = true
        }
    }

    fun loadList() {
        binding.author.text = "..."
        binding.musicName.text = "..."
        list = ArrayList()
        val type = object : TypeToken<List<MusicClass>>() {}.type
        list = gson.fromJson(myShader.getList("list"), type)

        recAdapter = RecAdapter(requireContext(), list, object : RecAdapter.OnMyClickListener{

            override fun onClickItem(position: Int, image: ImageView, constlayaut: ConstraintLayout) {

                if (last_index == position) {
                    rec_pause()
                } else {
                   rec_start(position)
                }
            }
        })
        binding.recycle.adapter = recAdapter
    }

    fun rec_pause() {
        if (music.isPlaying) {
            musicPause()
        } else {
            musicPlay()
        }
        recAdapter.notifyItemChanged(last_index)
    }

    fun rec_start(position: Int) {
        binding.layout.visibility = View.VISIBLE
        list[position].isHide = true
        getLast()
        recAdapter.notifyItemChanged(position)
        last_index = position
        musicPlayFirst()
    }

    private fun musicPlay() {
        music.start()
        binding.playMusicBottom.setImageResource(R.drawable.ic_pause)
        list[last_index].date = 0
    }

    private fun musicPlayFirst() {
        musicManager.releaseAudioFocusForMyApp()
        releaseMp()
        val musicClass = list[last_index]
        music = MediaPlayer.create(requireContext(), Uri.parse(musicClass.musicUri))
        musicManager.mediaPlayer = music
        if (musicManager.requestAudioFocusForMyApp()) {
            musicDetail()
            music.start()
            if (myShader.isStart("fragment")) {
                if (!ispalying) {
                    music.pause()
                }
                music.seekTo(duration)
                myShader.setStart(false, "fragment")
            }
            binding.playMusicBottom.setImageResource(R.drawable.ic_pause)
            changeBottomMusic(musicClass)
        }
    }

    fun musicPause() {
        music.pause()
        binding.playMusicBottom.setImageResource(R.drawable.ic_play)
        list[last_index].date = 1
    }

    fun getLast() {
        if (last_index > -1) {
            list[last_index].isHide = false
            recAdapter.notifyItemChanged(last_index)
        }
        if (last_const != null) {
            last_const?.setBackgroundColor(Color.parseColor("#303030"))
            last_image?.setImageResource(0)
        }
    }

    private fun changeBottomMusic(musicClass: MusicClass) {
        val parse = Uri.parse(musicClass.imageUri)
        if (parse != null) {
            Glide.with(requireContext()).load(parse).placeholder(R.drawable.stellio).into(binding.bigImage)
        }
        binding.author.text = musicClass.author
        binding.musicName.text = musicClass.title
    }

    private fun releaseMp() {
        try {
            music.stop()
//            music.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (myShader.isStart("fragment")) {
            duration = myShader.getList("duration").toInt()
            ispalying = myShader.isStart("playing")
            rec_start(myShader.getList("index").toInt())
        }
    }

    override fun onDestroy() {
        setData()
        super.onDestroy()
    }

}