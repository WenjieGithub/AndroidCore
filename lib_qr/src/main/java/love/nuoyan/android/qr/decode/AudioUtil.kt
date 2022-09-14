package love.nuoyan.android.qr.decode

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import love.nuoyan.android.qr.R
import java.io.Closeable
import java.io.IOException

object AudioUtil: OnCompletionListener, MediaPlayer.OnErrorListener, Closeable {
    private const val BEEP_VOLUME = 0.10f
    private var mediaPlayer: MediaPlayer? = null
    var audioId = 0

    // 开启响铃和震动
    @Synchronized
    fun playBeepSoundAndVibrate(activity: Activity, audioId: Int = 0) {
        if (mediaPlayer == null || audioId != 0) {
            // 设置 activity 音量控制键控制的音频流
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = buildMediaPlayer(activity)
        }
        mediaPlayer?.start()
    }

    // 创建 MediaPlayer
    private fun buildMediaPlayer(context: Context): MediaPlayer? {
        var afd : AssetFileDescriptor? = null
        val mediaPlayer = MediaPlayer()
        return try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            // 监听是否播放完成
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
            // 配置播放资源
            afd = context.resources.openRawResourceFd(if (audioId == 0) R.raw.lib_qr_beep else audioId)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            // 设置音量
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
            mediaPlayer.prepare()
            mediaPlayer
        } catch (ioe: IOException) {
            mediaPlayer.release()
            null
        } finally {
            afd?.close()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        // When the beep has finished playing, rewind to queue up another one.
        mp?.seekTo(0)
    }

    @Synchronized
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
//        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // we are finished, so put up an appropriate error toast if required and finish
//        } else {
            // possibly media player error, so release and recreate
//        }
        mp?.release()
        mediaPlayer = null
        return true
    }

    @Synchronized
    override fun close() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}