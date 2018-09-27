package tools.android.invoker;

import android.compact.utils.StringCompactUtil;

import java.util.ArrayList;

import tools.android.nestediterable.NestedMap;

public class Log {

    static NestedMap<ArrayList<String>> logs = new NestedMap<ArrayList<String>>();

    public static synchronized void save(String from, String to, String msg) {
        if (StringCompactUtil.hasEmptyString(from, to, msg)) {
            return;
        }

        ArrayList<String> msgs = logs.get(from, to);
        if (msgs == null) {
            msgs = new ArrayList<String>();
        }
        msgs.add(msg);
        logs.put(from, to, msgs);
    }

    public static synchronized ArrayList<String> get(String from, String to) {
        if (StringCompactUtil.hasEmptyString(from, to)) {
            return null;
        }
        return logs.get(from, to);
    }
}
