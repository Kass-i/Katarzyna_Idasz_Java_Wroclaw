package com.kass.loaders;

import java.util.List;

public interface JsonLoader<T> {
    List<T> load(String pathFile);
}
