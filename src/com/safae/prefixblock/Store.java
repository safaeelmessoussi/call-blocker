package com.safae.prefixblock;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/** Settings and the blocked-calls log, kept in SharedPreferences. */
public final class Store {
    private static final String PREFS = "prefixblock";
    private static final String K_ENABLED = "enabled";
    private static final String K_PREFIXES = "prefixes";
    private static final String K_HIDDEN = "block_hidden";
    private static final String K_CALLLOG = "show_in_call_log";
    private static final String K_LOG = "log";
    private static final int MAX_LOG = 100;

    private final SharedPreferences sp;

    public Store(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() { return sp.getBoolean(K_ENABLED, true); }
    public String getPrefixesText() { return sp.getString(K_PREFIXES, "+49"); }
    public List<String> getPrefixes() { return Rules.parsePrefixes(getPrefixesText()); }
    public boolean blockHidden() { return sp.getBoolean(K_HIDDEN, false); }
    public boolean showInCallLog() { return sp.getBoolean(K_CALLLOG, false); }

    public void save(boolean enabled, String prefixesText, boolean hidden, boolean callLog) {
        sp.edit()
                .putBoolean(K_ENABLED, enabled)
                .putString(K_PREFIXES, Rules.join(Rules.parsePrefixes(prefixesText)))
                .putBoolean(K_HIDDEN, hidden)
                .putBoolean(K_CALLLOG, callLog)
                .apply();
    }

    public synchronized void addLog(String line) {
        String old = sp.getString(K_LOG, "");
        String all = old.length() == 0 ? line : line + "\n" + old;
        String[] lines = all.split("\n");
        if (lines.length > MAX_LOG) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < MAX_LOG; i++) {
                if (i > 0) sb.append('\n');
                sb.append(lines[i]);
            }
            all = sb.toString();
        }
        sp.edit().putString(K_LOG, all).apply();
    }

    public String getLog() { return sp.getString(K_LOG, ""); }
    public void clearLog() { sp.edit().putString(K_LOG, "").apply(); }
}
