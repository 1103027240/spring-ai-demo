package cn.example.ai.demo.factory;

import cn.example.ai.demo.adapter.InstantTypeAdapter;
import cn.example.ai.demo.adapter.LocalDateTimeTypeAdapter;
import cn.example.ai.demo.adapter.LocalDateTypeAdapter;
import cn.example.ai.demo.adapter.ZonedDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class GsonFactory {

    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()  // 格式化输出
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .serializeNulls()  // 序列化null值
                .create();
    }

    public static Gson createCompactGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
    }

    // 用于序列化/反序列化列表
    public static Type getListType(Class<?> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }

    // 用于序列化/反序列化Map
    public static Type getMapType(Class<?> keyClass, Class<?> valueClass) {
        return TypeToken.getParameterized(Map.class, keyClass, valueClass).getType();
    }

}
