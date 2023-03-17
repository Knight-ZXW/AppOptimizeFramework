package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knightboost.appoptimizeframework.gsonopttest.Second;
import com.knightboost.appoptimizeframework.gsonopttest.SecondTabImageModel;

import java.io.IOException;

public class SecondTypeAdapter extends BaseTypeAdapter<Second>{

    public SecondTypeAdapter(Gson gson) {
        super(gson);
    }

    @Override
    public void write(JsonWriter out, Second value) throws IOException {

    }

    @Override
    public Second read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            Second model = new Second();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "cId":
                        model.setCId(getStringAdapter().read(jsonReader));
                        break;
                    case "name":
                        model.setName(getStringAdapter().read(jsonReader));
                        break;
                    case "cType":
                        model.setCType(getIntAdapter().read(jsonReader));
                        break;
                    case "showType":
                        model.setShowType(getIntAdapter().read(jsonReader));
                        break;
                    case "imageNameUrls":
                        model.setImageNameUrls(gson.getAdapter(SecondTabImageModel.class).read(jsonReader));
                        break;
                    case "lightImageNameUrls":
                        model.setLightImageNameUrls(gson.getAdapter(SecondTabImageModel.class).read(jsonReader));
                        break;
                    case "contentShowType":
                        model.setContentShowType(getIntAdapter().read(jsonReader));
                        break;
                    case "contentShowDetails":
                        model.setContentShowDetails(getStringAdapter().read(jsonReader));
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
