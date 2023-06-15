package com.example.tempo.getMusicList;

import android.util.Log;

import com.example.tempo.domain.SongDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetSongList {

    public List<SongDTO> songList = new ArrayList<>();
    int maxBpm = 190;
    int minBpm = 70;

    public List<SongDTO> getSongList() {
        OkHttpClient client = new OkHttpClient();
        CountDownLatch latch = new CountDownLatch(maxBpm - minBpm + 1);

        for (int i = minBpm; i <= maxBpm; i++) {
            String url = "https://api.getsongbpm.com/tempo/?api_key=92adcf5063c89106a1b3f27d171b8606&bpm="+ String.valueOf(i)+"&limit=4";
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.d("Error", "오류: " + e.getMessage());
                    Log.d("GetMusicList", "Request failed: " + e.getMessage());
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject mainObject = new JSONObject(response.body().string());
                            JSONArray tempoArray = mainObject.getJSONArray("tempo");

                            for (int j = 0; j < tempoArray.length(); j++) {
                                JSONObject jsonObject = tempoArray.getJSONObject(j);
                                SongDTO song = SongDTO.fromJson(jsonObject);
                                if (song != null) {
                                    synchronized (songList) {
                                        songList.add(song);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Log.d("GetMusicList", "Request successful. Songs count: " + songList.size());
                    } else {
                        Log.d("Error", "오류: " + response.message());
                        Log.d("GetMusicList", "Request unsuccessful: " + response.message());
                    }
                    latch.countDown();
                }
            });

        }

        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return songList;
    }
}
