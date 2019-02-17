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
import android.util.Log
import android.view.MenuItem;
import kotlin.math.log
//Silvio Orozco 18282
//Main Activity ejecucion principal del programa
class MainActivity : AppCompatActivity(),MediaPlayerControl{
    //Variables para utilizar nuestro reproductor
    private lateinit var  controller: MusicController
    private var musicSrv: MusicService = MusicService();
    private   var  playIntent: Intent? = null
    private var musicBound = false
     var songList: ArrayList<Song> =  ArrayList<Song>()
    lateinit var  songAdt:SongAdapter
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

        controller.setPrevNextListeners(
            { playNext() },
            { playPrev() })

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
        controller.show()




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
        val READ_STORAGE_PERMISSION_REQUEST_CODE =1;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            musicSrv.playSong()
        }

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
    fun requestPermission(){
        val READ_STORAGE_PERMISSION_REQUEST_CODE =1;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )






        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getSongLing()
                songAdt.notifyDataSetChanged()
                Log.v("Me","songlist")

            }
        }

    }
    fun getSongLing(){
        //Acceder a las canciones Contest Resolver
        val musicResolver = contentResolver





            val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Arreglo de canciones y un list view

        controller = MusicController(this@MainActivity)
        songView= findViewById(R.id.song_list)



         songAdt = SongAdapter(this, songList)
        songView.adapter = songAdt

        requestPermission()
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getSongLing()
            Log.v("", "Permission is granted");
            songAdt.notifyDataSetChanged()
        }
        Collections.sort(
            songList
        ) { a, b -> a.getTitle().compareTo(b.getTitle()) }

    }

}
