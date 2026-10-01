package com.safae.prefixblock;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String ROLE = "android.app.role.CALL_SCREENING";
    private static final int REQ_ROLE = 1;

    private Store store;
    private TextView status;
    private Button activate;
    private CheckBox enabled, hidden, callLog;
    private EditText prefixes;
    private TextView log;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFitsSystemWindows(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = text("Prefix Call Blocker", 24, true);
        root.addView(title);
        root.addView(text("Rejects calls from numbers starting with the prefixes below, before your phone rings.", 14, false));
        root.addView(space(16));

        status = text("", 16, true);
        status.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(status);
        activate = new Button(this);
        activate.setText("Activate blocking");
        activate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { requestRole(); }
        });
        root.addView(activate);
        root.addView(space(16));

        enabled = new CheckBox(this);
        enabled.setText("Blocking on");
        root.addView(enabled);
        root.addView(space(8));

        root.addView(text("Block numbers starting with (one per line):", 15, true));
        prefixes = new EditText(this);
        prefixes.setInputType(InputType.TYPE_CLASS_PHONE);
        prefixes.setSingleLine(false);
        prefixes.setMinLines(3);
        prefixes.setGravity(Gravity.TOP);
        prefixes.setHint("+49\n+44");
        root.addView(prefixes);
        root.addView(text("Tip: +49 and 0049 are treated the same.", 13, false));
        root.addView(space(8));

        hidden = new CheckBox(this);
        hidden.setText("Also block hidden / private numbers");
        root.addView(hidden);
        callLog = new CheckBox(this);
        callLog.setText("Show blocked calls in the phone's call log");
        root.addView(callLog);

        Button save = new Button(this);
        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.save(enabled.isChecked(), prefixes.getText().toString(),
                        hidden.isChecked(), callLog.isChecked());
                prefixes.setText(store.getPrefixesText());
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(save);
        root.addView(space(24));

        root.addView(text("Blocked calls", 18, true));
        log = text("", 14, false);
        log.setTypeface(Typeface.MONOSPACE);
        root.addView(log);
        Button clear = new Button(this);
        clear.setText("Clear list");
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { store.clearLog(); refresh(); }
        });
        root.addView(clear);
        root.addView(space(24));

        root.addView(text("Note: contacts saved in your phone are never blocked. To make blocked callers hear "
                + "\"number not available\" instead of your voicemail, disable voicemail / call forwarding with your operator.", 13, false));

        setContentView(scroll);

        enabled.setChecked(store.isEnabled());
        prefixes.setText(store.getPrefixesText());
        hidden.setChecked(store.blockHidden());
        callLog.setChecked(store.showInCallLog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        refresh();
    }

    private void requestRole() {
        try {
            RoleManager rm = getSystemService(RoleManager.class);
            if (rm != null && rm.isRoleAvailable(ROLE)) {
                startActivityForResult(rm.createRequestRoleIntent(ROLE), REQ_ROLE);
                return;
            }
        } catch (Throwable ignored) { }
        try {
            startActivity(new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"));
        } catch (Throwable t) {
            Toast.makeText(this, "Open Settings > Apps > Default apps > Caller ID & spam app", Toast.LENGTH_LONG).show();
        }
    }

    private boolean roleHeld() {
        try {
            RoleManager rm = getSystemService(RoleManager.class);
            return rm != null && rm.isRoleHeld(ROLE);
        } catch (Throwable t) {
            return false;
        }
    }

    private void refresh() {
        boolean active = roleHeld();
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        if (active) {
            status.setText("✓ Active: calls are screened before ringing");
            bg.setColor(Color.parseColor("#1E7A3C"));
            activate.setVisibility(View.GONE);
        } else {
            status.setText("✗ Not active: tap the button below and choose this app");
            bg.setColor(Color.parseColor("#B3261E"));
            activate.setVisibility(View.VISIBLE);
        }
        status.setTextColor(Color.WHITE);
        status.setBackground(bg);
        String l = store.getLog();
        log.setText(l.length() == 0 ? "No calls blocked yet." : l);
    }

    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
