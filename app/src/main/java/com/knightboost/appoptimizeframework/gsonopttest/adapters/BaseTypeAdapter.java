package com.knightboost.appoptimizeframework.gsonopttest.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public abstract class  BaseTypeAdapter<T>  extends TypeAdapter<T>{
    protected final Gson gson;

    protected volatile TypeAdapter<String> _stringAdapter;
    protected volatile TypeAdapter<Integer> _IntAdapter;
    protected volatile TypeAdapter<Long> _LongAdapter;

    public BaseTypeAdapter(Gson gson) {
        this.gson = gson;
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

    public TypeAdapter<Long> getLongAdapter(){
        if (_LongAdapter == null){
            _LongAdapter = gson.getAdapter(Long.class);
        }
        return _LongAdapter;
    }
}
