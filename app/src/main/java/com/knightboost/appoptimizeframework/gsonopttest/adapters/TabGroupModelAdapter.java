package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knightboost.appoptimizeframework.gsonopttest.Second;
import com.knightboost.appoptimizeframework.gsonopttest.TabGroupModel;

import java.io.IOException;
import java.util.ArrayList;

public class TabGroupModelAdapter extends BaseTypeAdapter<TabGroupModel>{
    public TabGroupModelAdapter(Gson gson) {
        super(gson);
    }

    @Override
    public void write(JsonWriter out, TabGroupModel value) throws IOException {

    }

    @Override
    public TabGroupModel read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            jsonReader.beginObject();
            TabGroupModel model = new TabGroupModel();
            TypeAdapter<String> stringAdapter = null;
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "group":
                        model.setGroup(getStringAdapter().read(jsonReader));
                        break;
                    case "icon":
                        model.setIcon(getStringAdapter().read(jsonReader));
                        break;
                    case "category":
                        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.getParameterized(ArrayList.class, Second.class));
                        model.setCategories((ArrayList<Second>) adapter.read(jsonReader));
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return model;
        }
    }
}
