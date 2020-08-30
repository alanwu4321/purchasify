package com.dgs.v1.service;

import com.dgs.v1.model.Delegate;
import com.dgs.v1.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class AbstractService<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBodyService.class);
    final Class<T> typeParameterClass;
    Entity entity;

    protected AbstractService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        try {
            this.entity = (Entity) typeParameterClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public List<T> getByRange(Context ctx, String from,  String to, Integer limit) {
        Delegate<T> delegate= null;
        try {
            delegate = entity.getDelegate(ctx);
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return delegate.getByRange(from, to, limit);
    }

    public List<T> getBySelected(Context ctx, String[] selected, Integer limit) {
        Delegate<T> delegate= null;
        try {
            delegate = entity.getDelegate(ctx);
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return delegate.getBySelected(selected, limit);
    }

    public List<T> getByColumn(Context ctx, String column, String value, Integer limit) {
        Delegate<T> delegate= null;
        try {
            delegate = entity.getDelegate(ctx);
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return delegate.getByColumn(column, value, limit);
    }

    public List<T> getByPrefix(Context ctx, String prefix, Integer limit) {
        Delegate<T> delegate= null;
        try {
            delegate = entity.getDelegate(ctx);
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return delegate.getByPrefix(prefix, limit);
    }
}
