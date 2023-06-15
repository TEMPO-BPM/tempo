package com.example.tempo.getMusicList;

import android.util.Log;

import com.example.tempo.database.SongDB;
import com.example.tempo.domain.Song;
import com.example.tempo.domain.SongDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GetSpotifyUri {

    private static final String TAG = "GetSpotifyId";

    public void execute(final String url, final OnCompleteListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String spotifyUri = "";

                try {
                    Document document = Jsoup.connect(url).userAgent("PostmanRuntime/7.31.3")
                            .get();

                    Element iframeElement = document.selectFirst("iframe");
                    String srcValue = iframeElement.attr("src");

                    int startIndex = srcValue.lastIndexOf('/') + 1;
                    int endIndex = srcValue.length();
                    spotifyUri = srcValue.substring(startIndex, endIndex);
                } catch (IOException e) {
                    Log.e(TAG, "Error in doInBackground: " + e.getMessage());
                }

                return spotifyUri;
            }
        });

        try {
            String spotifyUri = future.get();
            listener.onComplete(spotifyUri);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    public interface OnCompleteListener {
        void onComplete(String spotifyUri);
    }

    public void getUri(SongDB songDB) {

        GetSongList getSongList = new GetSongList();
        List<SongDTO> songDTOList = getSongList.getSongList();
        Song song = new Song();

        for (int i = 0; i < songDTOList.size(); i++) {
            SongDTO songDTO = songDTOList.get(i);
            song.setSongTitle(songDTO.getSongTitle());
            song.setArtistName(songDTO.getArtistName());
            song.setSongBpm(songDTO.getSongBpm());

            new GetSpotifyUri().execute(songDTOList.get(i).getSongUri(), new OnCompleteListener() {
                @Override
                public void onComplete(String spotifyUri) {
                    song.setSpotifyUri(spotifyUri);
                    System.out.println("bpm: " + String.valueOf(song.getSongBpm()) + ", uri:" + song.getSpotifyUri());
                    songDB.songDao().insertOne(song);
                }

            });

            try {
                Thread.sleep(5000); // 5초 쉬게 하기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("db count: " + songDB.songDao().getAll().size());
    }
}
