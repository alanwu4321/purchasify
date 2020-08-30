package com.dgs.v1.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public abstract class Delegate<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Delegate.class);

    final Class<T> typeParameterClass;
    Entity entity;

    protected Delegate(Class<T> typeParameterClass) {
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

    public List<T> getFromList(List<HashMap<String, Object>> list) {
        return entity.getFromList(typeParameterClass, list);
    }

    public List<T> getByRange(String from, String to, Integer limit) {
        LOGGER.error("not overridden");
        return null;
    }

    public List<T> getBySelected(String[] selected, Integer limit) {
        LOGGER.error("not overridden");
        return null;
    }

    public List<T> getById(String id) {
        LOGGER.error("not overridden");
        return null;
    }

    public List<T> getByColumn(String column, String value, Integer limit) {
        LOGGER.error("not overridden");
        return null;
    }

    public abstract List<T> getByPrefix(String prefix, Integer limit);
}
