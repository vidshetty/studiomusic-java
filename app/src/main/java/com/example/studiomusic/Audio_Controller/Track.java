package com.example.studiomusic.Audio_Controller;

public class Track {

//    {
//        _albumId: "63da57d81362e104b166e277",
//                _trackId: "63da57d81362e104b166e277",
//            Album: "Tere Pyaar Mein (From 'Tu Jhoothi Main Makkar')",
//            AlbumArtist: "Pritam, Arijit Singh, Nikhita Gandhi",
//            Type: "Single",
//            Year: "2023",
//            Color: "rgba(32,56,176,1)",
//            releaseDate: date("01-02-2023"),
//            Thumbnail: "https://lh3.googleusercontent.com/GbmvJFem2bpVzhhk1yoTrLMj3AAwpf0eif4exW1_nGPX7nb4zfLv1arbAHXEYPFJm6DmPZCk6teF1A1e=w544-h544-l90-rj",
//            Artist: "Pritam, Arijit Singh, Nikhita Gandhi",
//            Duration: "4: 25",
//            url: `${server[2]}/listen/Tere Pyaar Mein - Tu Jhoothi Main Makkar`
//    },

    public String album = null;
    public String artist = null;
    public String albumArtist = null;
    public String thumbnail = null;
    public String url = null;

    private Track() {
        this.album = "Tere Pyaar Mein (From 'Tu Jhoothi Main Makkar')";
        this.artist = "Pritam, Arijit Singh, Nikhita Gandhi";
        this.albumArtist = "Pritam, Arijit Singh, Nikhita Gandhi";
        this.thumbnail = "https://lh3.googleusercontent.com/GbmvJFem2bpVzhhk1yoTrLMj3AAwpf0eif4exW1_nGPX7nb4zfLv1arbAHXEYPFJm6DmPZCk6teF1A1e=w544-h544-l90-rj";
        this.url = "https://studiomusic.app/listen/Tere Pyaar Mein - Tu Jhoothi Main Makkar";
    }

    public static Track getDetails() {
        return new Track();
    }

}
