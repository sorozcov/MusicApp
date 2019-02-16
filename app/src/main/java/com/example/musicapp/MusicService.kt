package com.example.musicapp


import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager

import android.app.Notification;
import android.app.PendingIntent;




class MusicService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {
    private lateinit var  player:MediaPlayer;
    private lateinit var songs: ArrayList<Song>;
    private var songPosn: Int = 0
    private  var songTitle:String="";
    private val NOTIFY_ID:Int =1;
    override fun onPrepared(mp: MediaPlayer?) {
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(
            this, 0,
            notIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        var builder:Notification.Builder = Notification.Builder(this)
        builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(songTitle);
        val not = builder.build()
        startForeground(NOTIFY_ID, not);

    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset();
        return false;
    }

    override fun onCompletion(mp: MediaPlayer) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

     override fun onCreate() {
        super.onCreate()
         songPosn=0;
         player= MediaPlayer();
        initMusicPlayer();

    }
    fun initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
            PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    fun setList(songsS:ArrayList<Song>){
        songs=songsS;
    }

    inner class MusicBinder : Binder() {
        fun getService():MusicService{
            return this@MusicService
        }
    }
    fun playSong(){
        songTitle=playSong.getTitle();
    }

    override fun onDestroy() {
        stopForeground(true)
    }
    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosn--
        if(songPosn<0){ songPosn=songs.size-1;}
        playSong();
    }
    fun playNext(){
        songPosn++
        if(songPosn==songs.size){ songPosn=0;}
        playSong();
    }

}