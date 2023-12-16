package com.app.studiomusic.MessageTransfer;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class MessageTransfer {

    private static Map<String,Function> map = new HashMap<>();

    public static void register(String fragment_tag, Function func) {
        if (map == null) return;
        if (map.containsKey(fragment_tag)) return;
        map.put(fragment_tag, func);
    };

    public static void sendData(String fragment_tag, Bundle bundle) {
        if (map == null);
        if (bundle == null) return;
        if (!map.containsKey(fragment_tag)) return;
        Function func = map.get(fragment_tag);
        if (func == null) return;
        func.call(bundle);
    };

    public static void initialise() {
        map = new HashMap<>();
    };

};
