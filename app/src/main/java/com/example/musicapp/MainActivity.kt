package com.example.musicapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor;
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ListView;
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.widget.MediaController.MediaPlayerControl;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import com.example.musicapp.MusicService.MusicBinder;

import android.content.ServiceConnection;
import android.view.MenuItem;


class MainActivity : AppCompatActivity(),MediaPlayerControl{
    private lateinit var  controller: MusicController
    private var musicSrv: MusicService = MusicService();
    private   var  playIntent: Intent? = null
    private var musicBound = false
     var songList: ArrayList<Song> =  ArrayList<Song>()
   lateinit var songView:ListView
     var paused = false
    var playbackPaused = false
    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound==true ){
        return musicSrv.isPng();    }else return false}

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        if(musicSrv!=null && musicBound==true && (musicSrv.isPng())){
        return musicSrv.getDur();}else return 0
    }
    override fun onPause() {
        super.onPause();
        paused=true;    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller.hide()
        super.onStop()
    }


    override fun pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();    }

    override fun getBufferPercentage(): Int {
        return 0;
    }

    override fun seekTo(pos: Int) {
        musicSrv.seek(pos);    }

    override fun getCurrentPosition(): Int {
        if(musicSrv!=null && musicBound==true && (musicSrv.isPng())){
        return musicSrv.getPosn();}else return 0;
    }

    override fun canSeekBackward(): Boolean {
    return true;
    }

    override fun start() {
        musicSrv.go();    }

    override fun getAudioSessionId(): Int {
        return 0;
    }

    override fun canPause(): Boolean {
        return true    }

     fun setController() {
        //set the controller up
        controller = MusicController(this@MainActivity)
        controller.setPrevNextListeners(
            { playNext() },
            { playPrev() })
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);



    }

    private fun playNext() {
        musicSrv.playNext()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller.show(0)
    }

    override fun onDestroy() {
        stopService(playIntent)
        super.onDestroy()
    }
    private fun playPrev() {
        musicSrv.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller.show(0)
    }

    fun songPicked(view: View) {
        musicSrv.setSong(Integer.parseInt(view.tag.toString()))
        musicSrv.playSong()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Arreglo de canciones y un list view


        songView= findViewById(R.id.song_list)

        fun getSongLing(){
            //Acceder a las canciones
            val musicResolver = contentResolver

            val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val READ_STORAGE_PERMISSION_REQUEST_CODE =1;
            ActivityCompat.requestPermissions(
                this as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )

            val musicCursor = musicResolver.query(musicUri, null, null, null, null)
            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                //add songs to list
                do {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    songList.add(Song(thisId, thisTitle, thisArtist))
                } while (musicCursor.moveToNext())
            }

        }

        getSongLing()

        val songAdt = SongAdapter(this, songList)
        songView.adapter = songAdt
        Collections.sort(
            songList
        ) { a, b -> a.getTitle().compareTo(b.getTitle()) }
        setController()

    }
    //connect to the service
    val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.getService()
            //pass list
            musicSrv!!.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }
    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }
}
