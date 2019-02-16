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

import android.content.ContentUris
import android.util.Log











class MusicService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {
    private val musicBind: IBinder = MusicBinder();
    private  var  player:MediaPlayer = MediaPlayer()
    private lateinit var songs: ArrayList<Song>;
    private var songPosn: Int = 0
    private  var songTitle:String="";
    private val NOTIFY_ID:Int =1;
    override fun onPrepared(mp: MediaPlayer?) {
        //start playback
        mp!!.start();
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

    override fun onBind(intent: Intent): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset();
        return false;
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(player.getCurrentPosition()>0){
            mp!!.reset();
            playNext();
        }
    }



     override fun onCreate() {
        super.onCreate()
         songPosn=0;

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
        player.reset();
        //get song
        val playSong = songs[songPosn]
//get id
        val currSong = playSong.getID()
//set uri
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong
        )
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync();
        songTitle=playSong.getTitle();
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
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