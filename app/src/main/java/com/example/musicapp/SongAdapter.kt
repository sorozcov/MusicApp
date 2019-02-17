package com.example.musicapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.R.layout
import android.widget.TextView
import android.widget.LinearLayout



//Nuestro song adapter para mostrar nombre de la cancion y demas.

class SongAdapter(c:Context, mySongs:ArrayList<Song>): BaseAdapter() {
    private val songs: ArrayList<Song> = mySongs
    private val songInf: LayoutInflater = LayoutInflater.from(c);
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //map to song layout
        val songLay = songInf.inflate(R.layout.listviews, parent, false) as LinearLayout
        //get title and artist views
        val songView = songLay.findViewById(R.id.song_title) as TextView
        val artistView = songLay.findViewById(R.id.song_artist) as TextView
        //get song using position
        val currSong = songs[position]
        //get title and artist strings
        songView.text = currSong.getTitle()
        artistView.text = currSong.getArtist()
        //set position as tag
        songLay.tag = position
        return songLay
    }

    override fun getItem(position: Int): Any {
        return 0;
    }

    override fun getItemId(position: Int): Long {
        return 0;
    }

    override fun getCount(): Int {
        return songs.size
    }
}