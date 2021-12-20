package uz.pdp.musicplayer.ui

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import rm.com.audiowave.OnProgressListener
import uz.pdp.musicplayer.MainActivity
import uz.pdp.musicplayer.R
import uz.pdp.musicplayer.adapters.SliderAdapter
import uz.pdp.musicplayer.classes.MusicClass
import uz.pdp.musicplayer.classes.MusicManager
import uz.pdp.musicplayer.databinding.FragmentPlayBinding
import uz.pdp.musicplayer.preference.MyShared
import java.io.File
import kotlin.math.abs

class PlayFragment : Fragment(R.layout.fragment_play) {

    private val binding by viewBinding(FragmentPlayBinding::bind)
    lateinit var list: ArrayList<MusicClass>
    private lateinit var myShared: MyShared
    private var index = -1
    private var newIndex = -1
    private var firstIndex = -1
    private var duration = -1
    private var ispalying = false
    private var isFirst = true
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var handler: Handler
    private val gson = Gson()
    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        (activity as MainActivity).onHide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myShared = MyShared.getInstance(requireContext())
        loadList()
        handler = Handler(Looper.getMainLooper())
        loadThings()

        binding.apply {
            mediaPlayer = MediaPlayer()
            musicManager = MusicManager(requireContext())
            sliderAdapter = SliderAdapter(requireContext(), list)
            viewpager2.adapter = sliderAdapter
            binding.viewpager2.currentItem = 0
            viewpager()

            looping.setOnClickListener {
                if (myShared.isStart("looping")) {
                    looping.setImageResource(R.drawable.ic_loop)
                    myShared.setStart(false, "looping")
                    mediaPlayer.isLooping = false
                } else {
                    looping.setImageResource(R.drawable.ic_loop_one)
                    myShared.setStart(true, "looping")
                    mediaPlayer.isLooping = true
                }
            }

            waveformSeekBar.onProgressListener = object : OnProgressListener{
                override fun onProgressChanged(progress: Float, byUser: Boolean) {
                    if (byUser) {
                        mediaPlayer.seekTo((progress * mediaPlayer.duration / 100).toInt())
                    }
                }

                override fun onStartTracking(progress: Float) {

                }

                override fun onStopTracking(progress: Float) {

                }

            }

            backBtn.setOnClickListener {
                if (newIndex != 0) {
                    newIndex--
                    viewpager2.currentItem = newIndex
                }
            }

            nextBtn.setOnClickListener {
                if (newIndex != list.size-1) {
                   newIndex++
                   viewpager2.currentItem = newIndex
                }
            }

            startPause.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    playMusic.setImageResource(R.drawable.ic_play)
                } else {
                    mediaPlayer.start()
                    playMusic.setImageResource(R.drawable.ic_pause)
                }
            }
        }

    }

    private fun loadThings() {
        firstIndex = myShared.getList("index").toInt()
        newIndex = firstIndex
        duration = myShared.getList("duration").toInt()
        ispalying = myShared.isStart("playing")
    }

    private fun musicStart() {
        val musicClass = list[index]
        releaseMp()
        musicManager.releaseAudioFocusForMyApp()
        mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(musicClass.musicUri))
        musicManager.mediaPlayer = mediaPlayer
        if (musicManager.requestAudioFocusForMyApp()) {
            mediaPlayer.start()
            musicDetail()
            binding.playMusic.setImageResource(R.drawable.ic_pause)
            if (isFirst) {
                if (!ispalying) {
                    mediaPlayer.pause()
                    binding.playMusic.setImageResource(R.drawable.ic_play)
                }
                mediaPlayer.seekTo(duration)
                isFirst = false
            }
            setTexts(index)
        }
    }

    private fun musicDetail() {
        mediaPlayer.setOnCompletionListener {
            binding.nextBtn.performClick()
        }
        if (myShared.isStart("looping")) {
            mediaPlayer.isLooping = true
        }
    }

    private fun loadList() {
        list = ArrayList()
        val type = object : TypeToken<List<MusicClass>>() {}.type
        list = gson.fromJson(myShared.getList("list"), type)
    }

    private fun viewpager() {
        binding.apply {
            viewpager2.clipToPadding = false
            viewpager2.clipChildren = false
            viewpager2.offscreenPageLimit = 1
            viewpager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(60))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f+r*0.15f
            }
            viewpager2.setPageTransformer(compositePageTransformer)
            viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    newIndex = position
                    run()
                }
            })

            binding.viewpager2.currentItem = firstIndex
        }
    }

    private fun handlerFun() {

        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, 100)
    }

    private var runnable = object : Runnable{
        override fun run() {
            val sec = mediaPlayer.currentPosition / 1000
            val duration1 = mediaPlayer.duration / 1000
            binding.waveformSeekBar.progress = (100*sec/duration1).toFloat()
            if (sec/60<10) {
                if (sec % 60 < 10) {
                    binding.minSec.text = "0${sec / 60}:0${sec % 60}"
                } else {
                    binding.minSec.text = "0${sec / 60}:${sec % 60}"
                }
            }
            else {
                if (sec % 60 < 10) {
                    binding.minSec.text = "${sec / 60}:0${sec % 60}"
                } else {
                    binding.minSec.text = "${sec / 60}:${sec % 60}"
                }
            }
            handler.postDelayed(this, 100)
        }
    }

    private fun run() {
        index = newIndex
        binding.waveformSeekBar.setRawData(getBytes(list[index].musicUri))
        musicStart()
        handlerFun()
    }

    private fun setTexts(position: Int) {
        binding.apply {
            authorMusic.text = list[position].author
            titleMusic.text = list[position].title
            count.text = "${position+1} из ${list.size}"
            val sec = list[position].duration / 1000
            if (sec/60<10) duration.text = "0${sec/60}:${sec%60}"
            else duration.text = "${sec/60}:${sec%60}"
        }
    }

    private fun getBytes(filename: String): ByteArray {
        val file = File(filename)
        return if (file.exists())
            file.readBytes()
        else byteArrayOf(0)
    }

    private fun releaseMp() {
        try {
            handler.removeCallbacks(runnable)
            mediaPlayer.stop()
//            mediaPlayer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        myShared.setList("$newIndex", "index")
        myShared.setList("${mediaPlayer.currentPosition}", "duration")
        if (mediaPlayer.isPlaying) {
            myShared.setStart(true, "playing")
        } else {
            myShared.setStart(false, "playing")
        }
        releaseMp()
        (activity as MainActivity).onShow()
        super.onDestroy()
    }

}