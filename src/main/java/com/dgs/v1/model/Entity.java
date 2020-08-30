package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class Entity<T> {

    //TODO <T>Delegate = new Delegate<T>
    public abstract Delegate<T> getDelegate(Context ctx);

    public List<T> getFromList(Class<T> c, List<HashMap<String, Object>> list) {
        final AtomicReference<ObjectMapper> mapper = new AtomicReference<>(new ObjectMapper());
        List<T> newList = list.stream().map(
                map -> mapper.get().convertValue(map, c)
        ).collect(Collectors.toList());
        return newList;
    }
}
