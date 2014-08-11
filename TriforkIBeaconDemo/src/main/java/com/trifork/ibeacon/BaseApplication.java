package com.trifork.ibeacon;

import android.app.Application;
import android.content.Context;
import dagger.ObjectGraph;

import java.util.Arrays;
import java.util.List;

public class BaseApplication extends Application {

    private static ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ApplicationModule(this));
    }

    public static void inject(Object object) {
        graph.inject(object);
    }

    public static Context getContext() {
        return null;
    }

}
