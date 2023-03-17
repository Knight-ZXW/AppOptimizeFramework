package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knightboost.appoptimizeframework.gsonopttest.First;

import java.io.IOException;

public class FirstAdapter extends TypeAdapter<First> {

    private final Gson gson;

    private volatile TypeAdapter<String> _stringAdapter;

    public FirstAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter jsonWriter, First object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
        }
    }

    @Override
    public First read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            jsonReader.beginObject();
            First first = new First();
            TypeAdapter<String> stringAdapter = null;
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "name":
                        stringAdapter = _stringAdapter;
                        if (stringAdapter == null) {
                            stringAdapter = this.gson.getAdapter(String.class);
                            this._stringAdapter = stringAdapter;
                        }
                        first.setName(stringAdapter.read(jsonReader));
                        break;
                    case "fillPoint":
                        stringAdapter = _stringAdapter;
                        if (stringAdapter == null) {
                            stringAdapter = this.gson.getAdapter(String.class);
                            this._stringAdapter = stringAdapter;
                        }
                        first.setFillPoint(stringAdapter.read(jsonReader));
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return first;
        }
    }
}
