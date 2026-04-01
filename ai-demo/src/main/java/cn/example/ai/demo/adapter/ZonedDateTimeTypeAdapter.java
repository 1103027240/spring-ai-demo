package cn.example.ai.demo.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String str = in.nextString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(str, formatter);
    }

}
