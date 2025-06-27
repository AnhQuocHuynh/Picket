package com.example.locket.common.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.locket.common.models.moment.Overlay;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {

    @TypeConverter
    public static List<Overlay> fromString(String value) {
        Type listType = new TypeToken<List<Overlay>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<Overlay> list) {
        return new Gson().toJson(list);
    }
}
