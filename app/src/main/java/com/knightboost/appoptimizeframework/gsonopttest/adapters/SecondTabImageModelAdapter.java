package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knightboost.appoptimizeframework.gsonopttest.SecondTabImageModel;

import java.io.IOException;

public class SecondTabImageModelAdapter extends TypeAdapter<SecondTabImageModel> {

    private final Gson gson;

    private volatile TypeAdapter<String> _stringAdapter;
    private volatile TypeAdapter<Integer> _IntAdapter;

    public SecondTabImageModelAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter jsonWriter, SecondTabImageModel object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
        }
    }

    @Override
    public SecondTabImageModel read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            jsonReader.beginObject();
            SecondTabImageModel model = new SecondTabImageModel();
            TypeAdapter<String> stringAdapter = null;
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "notChosen":
                        model.setNotChosen(getStringAdapter().read(jsonReader));
                        break;
                    case "chosen":
                        model.setChosen(getStringAdapter().read(jsonReader));
                        break;
                    case "notChosenLucency":
                        model.setNotChosenLucency(getStringAdapter().read(jsonReader));
                        break;
                    case "height":
                        model.setHeight(getIntAdapter().read(jsonReader));
                        break;
                    case "width":
                        model.setWidth(getIntAdapter().read(jsonReader));
                        break;
                    case "webUrl":
                        model.setWebpUrl(getStringAdapter().read(jsonReader));
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return model;
        }
    }

    public TypeAdapter<String> getStringAdapter(){
        if (_stringAdapter == null){
            _stringAdapter = gson.getAdapter(String.class);
        }
        return _stringAdapter;
    }

    public TypeAdapter<Integer> getIntAdapter(){
        if (_IntAdapter == null){
            _IntAdapter = gson.getAdapter(Integer.class);
        }
        return _IntAdapter;
    }

}
