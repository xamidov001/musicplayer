package uz.pdp.musicplayer.classes

import android.content.ContentValues
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

class MusicManager(var context: Context) {

    var mediaPlayer = MediaPlayer()

    private val mOnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // resume playback
                    if (mediaPlayer == null) {mediaPlayer.start()} else if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
                    mediaPlayer!!.setVolume(1.0f, 1.0f)
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
//                    mediaPlayer!!.release()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
            }
        }



     fun requestAudioFocusForMyApp(): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Request audio focus for playback
        val result = am.requestAudioFocus(
            mOnAudioFocusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        return if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(ContentValues.TAG, "Audio focus received")
            true
        } else {
            Log.d(ContentValues.TAG, "Audio focus NOT received")
            false
        }
    }

    fun releaseAudioFocusForMyApp() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.abandonAudioFocus(mOnAudioFocusChangeListener)
    }
}