package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.tempo.domain.Song;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SaveState {
    private Context context;
    private Activity activity;
    private SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    SaveState(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        sp = context.getSharedPreferences("ChallengeLevel", 0);
        editor = sp.edit();
    }

    public String[] loadChallenge(){
        String[] data = new String[2];
        data[0] = sp.getString("targetLevel", "LEVEL1");
        data[1] = sp.getString("targetStage", "-1");
        return data;
    }
    public void saveChallenge(String targetLevel, String targetStage){
        editor.putString("targetLevel", targetLevel);
        editor.putString("targetStage", targetStage);
        editor.commit();
    }
    public List<Song> loadHistory() {
        List<Song> history = new ArrayList<>();
        Gson gson = new Gson();
        for(int i = 0; i < 3; i++){
            String json = sp.getString("history#"+String.valueOf(i), "");
            if (!json.equals("")){
                history.add(gson.fromJson(json, Song.class));
            }else{
                history.add(null);
            }
        }
        return history;
    }
    public void saveHistory(List<Song> history){
        Gson gson = new Gson();
        for(int i = 0; i < history.size(); i++){
            if (history.get(i) != null) {
                String json = gson.toJson(history.get(i));
                editor.putString("history#" + String.valueOf(i), json);
                editor.commit();
            }
        }
    }
}
