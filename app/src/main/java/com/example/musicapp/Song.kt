package com.example.musicapp





class Song (songId: Long, songTitle: String, songArtist: String){
    //Clase de tipo cancion para crear una cancion.
    private var id:Long = songId;
    private var title:String = songTitle;
    private var artist:String = songArtist;
    fun getID(): Long {
        return id
    }

    fun getTitle(): String {
        return title
    }

    fun getArtist(): String {
        return artist
    }
}