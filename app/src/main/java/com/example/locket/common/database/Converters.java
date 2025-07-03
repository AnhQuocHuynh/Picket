package com.example.locket.common.database;

import androidx.room.TypeConverter;

import com.example.locket.common.database.entities.MomentEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromOverlayList(List<MomentEntity.Overlay> overlays) {
        if (overlays == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(overlays);
    }

    @TypeConverter
    public static List<MomentEntity.Overlay> toOverlayList(String overlaysString) {
        if (overlaysString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MomentEntity.Overlay>>() {}.getType();
        return gson.fromJson(overlaysString, listType);
    }

    @TypeConverter
    public static String fromReactionList(List<MomentEntity.Reaction> reactions) {
        if (reactions == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(reactions);
    }

    @TypeConverter
    public static List<MomentEntity.Reaction> toReactionList(String reactionsString) {
        if (reactionsString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MomentEntity.Reaction>>() {}.getType();
        return gson.fromJson(reactionsString, listType);
    }
}
