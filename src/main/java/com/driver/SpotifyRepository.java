package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        boolean artistFound = false;
        Album album = new Album(title);
        albums.add(album);
        for(Artist artist : artists) {
            if(artist.getName().equals(artistName)) {
                artistFound = true;    //artist exists in list
                List<Album> albumsOfArtist = artistAlbumMap.get(artist);
                if(albumsOfArtist == null){
                    albumsOfArtist = new ArrayList<>();
                }
                albumsOfArtist.add(album);
                artistAlbumMap.put(artist, albumsOfArtist);
            }
        }
        if(artistFound == false){    //artist does not exist in list
            Artist artist = new Artist(artistName);  //creating artist
            artists.add(artist);
            List<Album> albumsOfArtist =  new ArrayList<>();
            albumsOfArtist.add(album);
            artistAlbumMap.put(artist,albumsOfArtist);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album


       boolean albumFound = false;
        Song song = null;
       for(Album album : albums){
           if(album.getTitle().equals(albumName)){
               albumFound = true;   //album exists in the list
               song = new Song(title, length);
               songs.add(song);
               List<Song> songsOfAlbum = albumSongMap.get(album);
               if(songsOfAlbum == null){
                   songsOfAlbum = new ArrayList<>();
               }
               songsOfAlbum.add(song);
               albumSongMap.put(album, songsOfAlbum);
           }
       }
       if(albumFound == false){   //album does not exist
           throw new Exception("Album does not exist");
       }
       else{
           return song;
       }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        boolean userFound = false;
        Playlist playlist = null;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userFound = true;

                playlist = new Playlist(title);
                playlists.add(playlist);  //adding playlist to the list

                List<Song> songsInPlaylist = new ArrayList<>();
                for(Song song : songs){
                    if(song.getLength() == length){
                        songsInPlaylist.add(song);
                    }
                }

                playlistSongMap.put(playlist, songsInPlaylist);
                creatorPlaylistMap.put(user, playlist);
                List<User> listeners = new ArrayList<>();
                listeners.add(user);
                playlistListenerMap.put(playlist, listeners);
                List<Playlist> playlistList = userPlaylistMap.get(user);
                if(playlistList == null){
                    playlistList = new ArrayList<>();
                }
                playlistList.add(playlist);
                userPlaylistMap.put(user, playlistList);
            }
        }
        if(userFound == false){
            throw new Exception("User does not exist");
        }
        else{
            return playlist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        Playlist playlist = null;
        boolean userFound = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userFound = true;   //user exists in the list

                playlist = new Playlist(title);
                playlists.add(playlist);
                List<Song> songsInPlaylist = new ArrayList<>();
                for(Song song : songs){
                    String songName = song.getTitle();
                    if(songTitles.contains(songName)){
                        songsInPlaylist.add(song);
                    }
                }
                playlistSongMap.put(playlist, songsInPlaylist);

                creatorPlaylistMap.put(user, playlist);   //user is the creator
                List<User> listeners = new ArrayList<>();
                listeners.add(user);   //user is the first listener
                playlistListenerMap.put(playlist, listeners);
                List<Playlist> playlistList = userPlaylistMap.get(user);
                if(playlistList == null){
                    playlistList = new ArrayList<>();
                }
                playlistList.add(playlist);
                userPlaylistMap.put(user, playlistList);
            }
        }
        if(userFound == false){
            throw new Exception("User does not exist");
        }
        else{
            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        boolean playlistFound = false;
        boolean userFound = false;
        boolean userIsCreator = false;
        boolean userIsListener = false;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                playlistFound = true;   //playlist exists in the list
                for(User user : users){
                    if(user.getMobile().equals(mobile)){
                        userFound = true;   //user exists in the list
                        if(creatorPlaylistMap.containsKey(user)){ //checking if user is the creator
                            userIsCreator = true;
                            return playlist;
                        }
                        List<User> usersOfPlaylist = playlistListenerMap.get(playlist);
                        if(usersOfPlaylist!=null && usersOfPlaylist.contains(user)){  //checking if user is already the listener
                            userIsListener = true;
                            return playlist;
                        }
                  if(userIsCreator == false && userIsListener == false){
                      List<User> listeners = playlistListenerMap.get(playlist);
                      if(listeners == null){
                          listeners = new ArrayList<>();
                      }
                      listeners.add(user);
                      playlistListenerMap.put(playlist, listeners);

                      List<Playlist> playlists = userPlaylistMap.get(user);
                      if(playlists == null){
                          playlists = new ArrayList<>();
                      }
                      playlists.add(playlist);
                      userPlaylistMap.put(user, playlists);
                  }
                  return playlist;
                    }
                }
                if(userFound == false){
                    throw new Exception("User does not exist");
                }
            }
        }
        if(playlistFound == false){
            throw new Exception("Playlist does not exist");
        }
        return null;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }

        Song song = null;
        for(Song song1 : songs){
            if(song1.getTitle().equals(songTitle)){
                song = song1;
                break;
            }
        }
        if(song == null){
            throw new Exception("Song does not exist");
        }

        List<User> songLikedByUsers = songLikeMap.get(song);
        if(songLikedByUsers == null){
            songLikedByUsers = new ArrayList<>();
        }
        if(songLikedByUsers.contains(user)){
            return song;
        }
        else{
            songLikedByUsers.add(user);
            songLikeMap.put(song, songLikedByUsers);
            song.setLikes(song.getLikes()+1);

            Album album = null;
            for(Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()){
                List<Song> songsInTheAlbum = entry.getValue();
                if(songsInTheAlbum.contains(song)){
                    album = entry.getKey();
                    break;
                }
            }

            Artist artist = null;
            for(Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()){
                List<Album> albumsByArtist = entry.getValue();
                if(albumsByArtist.contains(album)){
                    artist = entry.getKey();
                    artist.setLikes(artist.getLikes()+1);
                    break;
                }
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        int max = 0;
        String artistName = null;
        for(Artist artist : artists){
            if(artist.getLikes() > max){
                max = artist.getLikes();
                artistName = artist.getName();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {
        int max = 0;
        String songName = null;
        for(Song song : songs){
            if(song.getLikes() > max){
                max = song.getLikes();
                songName = song.getTitle();
            }
        }
        return songName;
    }
}
