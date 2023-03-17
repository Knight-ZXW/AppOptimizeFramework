package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.knightboost.appoptimizeframework.gsonopttest.First;
import com.knightboost.appoptimizeframework.gsonopttest.RecommendTabInfo;
import com.knightboost.appoptimizeframework.gsonopttest.Second;
import com.knightboost.appoptimizeframework.gsonopttest.TabGroupModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecommendTabInfoAdapter extends BaseTypeAdapter<RecommendTabInfo>{
    public RecommendTabInfoAdapter(Gson gson) {
        super(gson);
    }

    @Override
    public void write(JsonWriter out, RecommendTabInfo value) throws IOException {

    }

    TypeToken<?> secondAdapter = null;
    TypeToken<?> firstAdapter = null;
    TypeToken<?> secondMoreAdapter = null;
    TypeToken<?> secondMoreV2Adapter = null;
    Map<String,TypeToken<?>> typeTokenCache = new HashMap<>();

    @Override
    public RecommendTabInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            jsonReader.beginObject();
            RecommendTabInfo model = new RecommendTabInfo();
            TypeToken<?> typeToken = null;
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                switch (name) {
                    case "second":
                        typeToken = typeTokenCache.get("second");
                        if (typeToken == null){
                            typeToken = TypeToken.getParameterized(ArrayList.class, Second.class);
                            typeTokenCache.put("second",typeToken);
                        }
                        TypeAdapter<?> secondAdapter = gson.getAdapter(typeToken);
                        model.setSecond((ArrayList<Second>) secondAdapter.read(jsonReader));
                        break;
                    case "first":
                        typeToken = typeTokenCache.get("first");
                        if (typeToken == null){
                            typeToken = TypeToken.getParameterized(ArrayList.class, First.class);
                            typeTokenCache.put("first",typeToken);
                        }
                        TypeAdapter<?> firstAdapter = gson.getAdapter(typeToken);
                        model.setFirst((ArrayList<First>) firstAdapter.read(jsonReader));
                        break;
                    case "secondMore":
                        typeToken = typeTokenCache.get("secondMore");
                        if (typeToken == null){
                            typeToken = TypeToken.getParameterized(ArrayList.class, TabGroupModel.class);
                            typeTokenCache.put("secondMore",typeToken);
                        }
                        TypeAdapter<?> secondMoreAdapter = gson.getAdapter(typeToken);
                        model.setSecondMore((ArrayList<TabGroupModel>) secondMoreAdapter.read(jsonReader));
                        break;
                    case "secondMoreV2":
                        TypeAdapter<?> secondMoreV2Adapter = gson.getAdapter(TypeToken.getParameterized(ArrayList.class, Second.class));
                        model.setSecondMoreV2((ArrayList<Second>) secondMoreV2Adapter.read(jsonReader));
                        break;
                    case "landingChannel":
                        model.setLandingChannel(getStringAdapter().read(jsonReader));
                        break;
                    case "undertakeCallback":
                        model.setUndertakeCallback(getIntAdapter().read(jsonReader));
                        break;
                    case "requestTs":
                        model.setRequestTs(gson.getAdapter(Long.class).read(jsonReader));
                        break;
                    case "landingXlabVideo":
                        model.setLandingXlabVideo(getIntAdapter().read(jsonReader));
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
