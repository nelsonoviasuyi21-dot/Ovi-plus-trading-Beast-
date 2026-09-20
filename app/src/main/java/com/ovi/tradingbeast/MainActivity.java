package com.ovi.tradingbeast;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends Activity {

    private final Handler handler = new Handler();

    private LinearLayout root;
    private TextView status;
    private TextView health;
    private TextView connection;
    private TextView portfolio;
    private TextView scanner;
    private TextView position;
    private TextView performance;
    private TextView activity;
    private TextView signal;
    private TextView rules;
    private TextView lastUpdate;

    private Button startButton;
    private Button stopButton;
    private Button resumeButton;
    private Button emergencyButton;
    private Button scanButton;

    private boolean loading = false;

    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, CloudConfig.POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(Color.rgb(8,12,22));
        getWindow().setNavigationBarColor(Color.rgb(5,8,16));

        buildInterface();

        refresh();

        handler.postDelayed(poller, CloudConfig.POLL_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size) {

        TextView t = new TextView(this);

        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));

        return t;
    }

    private LinearLayout card() {

        LinearLayout box = new LinearLayout(this);

        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawableHelper.background(
                box,
                Color.rgb(12,18,31),
                dp(14)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, dp(7), 0, dp(7));

        box.setLayoutParams(p);

        return box;
    }

    private TextView title(String s) {

        TextView t = text(s, 17);

        t.setTypeface(
                Typeface.create(
                        "sans-serif-black",
                        Typeface.BOLD
                )
        );

        t.setTextColor(Color.WHITE);
        t.setLetterSpacing(.04f);

        return t;
    }

    private void buildInterface() {

        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(5,8,16));

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(15), dp(18), dp(15), dp(25));

        scroll.addView(root);

        setContentView(scroll);

        TextView brand = text("OVI PLUS", 31);

        brand.setGravity(Gravity.CENTER);
        brand.setTypeface(
                Typeface.create(
                        "sans-serif-black",
                        Typeface.BOLD
                )
        );
        brand.setLetterSpacing(.09f);

        root.addView(brand);

        TextView beast = text("TRADING BEAST", 21);

        beast.setGravity(Gravity.CENTER);
        beast.setTypeface(
                Typeface.create(
                        "sans-serif-black",
                        Typeface.BOLD
                )
        );
        beast.setLetterSpacing(.12f);
        beast.setTextColor(Color.rgb(170,190,255));

        root.addView(beast);

        connection = text("● CONNECTING TO CLOUD...", 13);
        connection.setGravity(Gravity.CENTER);
        root.addView(connection);

        LinearLayout account = card();
        account.addView(title("ACCOUNT & ENGINE"));

        status = text("STATUS: STARTING", 16);
        health = text("ENGINE: CHECKING", 15);

        account.addView(status);
        account.addView(health);

        root.addView(account);

        LinearLayout port = card();
        port.addView(title("PORTFOLIO OVERVIEW"));

        portfolio = text(
                "Balance: --\n" +
                "Active Position: --\n" +
                "Entries: --\n" +
                "Take Profits: --\n" +
                "Rejected: --",
                15
        );

        port.addView(portfolio);
        root.addView(port);

        LinearLayout plan = card();
        plan.addView(title("TRADING PLAN"));

        rules = text(
                "ENTRY SCORE       ≥ 55 / 100\n" +
                "BUYER STRENGTH    ≥ 70%\n" +
                "ENTRY MODE        ONE ENTRY / NO RE-ENTRY\n" +
                "EXECUTION         PAPER TRADING\n" +
                "TARGET ENGINE     ACTIVE",
                14
        );

        rules.setTypeface(Typeface.DEFAULT_BOLD);

        plan.addView(rules);
        root.addView(plan);

        LinearLayout scan = card();
        scan.addView(title("SCANNER INTELLIGENCE"));

        scanner = text(
                "Scanner waiting for live data...",
                15
        );

        scan.addView(scanner);
        root.addView(scan);

        LinearLayout sig = card();
        sig.addView(title("LATEST SIGNAL"));

        signal = text(
                "No signal received.",
                15
        );

        sig.addView(signal);
        root.addView(sig);

        LinearLayout pos = card();
        pos.addView(title("ACTIVE POSITION"));

        position = text(
                "No active position.",
                15
        );

        pos.addView(position);
        root.addView(pos);

        LinearLayout perf = card();
        perf.addView(title("PERFORMANCE"));

        performance = text(
                "Entries: --\nTake Profits: --\nRejected: --",
                15
        );

        perf.addView(performance);
        root.addView(perf);

        LinearLayout live = card();
        live.addView(title("LIVE ACTIVITY"));

        activity = text(
                "LIVE • waiting for cloud events...",
                14
        );

        activity.setTypeface(Typeface.DEFAULT_BOLD);

        live.addView(activity);

        lastUpdate = text("Last update: --", 12);
        lastUpdate.setTextColor(Color.LTGRAY);

        live.addView(lastUpdate);

        root.addView(live);

        LinearLayout controls = card();
        controls.addView(title("BOT CONTROLS"));

        startButton = button("START BOT");
        stopButton = button("STOP BOT");
        resumeButton = button("RESUME");
        emergencyButton = button("EMERGENCY STOP");
        scanButton = button("SCAN NOW");

        controls.addView(startButton);
        controls.addView(stopButton);
        controls.addView(resumeButton);
        controls.addView(emergencyButton);
        controls.addView(scanButton);

        root.addView(controls);

        startButton.setOnClickListener(v ->
                control("/control/start"));

        stopButton.setOnClickListener(v ->
                control("/control/stop"));

        resumeButton.setOnClickListener(v ->
                control("/control/resume"));

        emergencyButton.setOnClickListener(v ->
                control("/control/emergency-stop"));

        scanButton.setOnClickListener(v ->
                control("/control/scan"));
    }

    private Button button(String s) {

        Button b = new Button(this);

        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        p.setMargins(0, dp(5), 0, dp(5));

        b.setLayoutParams(p);

        GradientDrawableHelper.background(
                b,
                Color.rgb(20,29,48),
                dp(12)
        );

        return b;
    }

    private void refresh() {

        if (loading) return;

        loading = true;

        new Thread(() -> {

            try {

                String raw = get("/status");

                runOnUiThread(() -> {

                    try {

                        JSONObject j =
                                new JSONObject(raw);

                        JSONObject settings =
                                j.optJSONObject("settings");

                        JSONObject state =
                                j.optJSONObject("state");

                        if (state == null)
                            state = new JSONObject();

                        String s =
                                state.optString(
                                        "status",
                                        "UNKNOWN"
                                );

                        String h =
                                state.optString(
                                        "health",
                                        "UNKNOWN"
                                );

                        status.setText(
                                "STATUS: " + s
                        );

                        health.setText(
                                "ENGINE: " + h
                        );

                        connection.setText(
                                "● CLOUD CONNECTED"
                        );

                        connection.setTextColor(
                                Color.rgb(80,220,145)
                        );

                        JSONObject stats =
                                state.optJSONObject("stats");

                        if (stats != null) {

                            int entries =
                                    stats.optInt(
                                            "entries",
                                            0
                                    );

                            int tp =
                                    stats.optInt(
                                            "take_profits",
                                            0
                                    );

                            int rejected =
                                    stats.optInt(
                                            "rejected",
                                            0
                                    );

                            performance.setText(
                                    "Entries: " + entries +
                                    "\nTake Profits: " + tp +
                                    "\nRejected: " + rejected
                            );
                        }

                        JSONArray positions =
                                state.optJSONArray(
                                        "positions"
                                );

                        if (positions != null &&
                                positions.length() > 0) {

                            JSONObject p =
                                    positions.getJSONObject(0);

                            position.setText(
                                    formatPosition(p)
                            );

                            portfolio.setText(
                                    "Active Position: " +
                                    p.optString(
                                            "symbol",
                                            "--"
                                    ) +
                                    "\nEntry: " +
                                    p.optString(
                                            "entry_price",
                                            "--"
                                    ) +
                                    "\nCurrent: " +
                                    p.optString(
                                            "current_price",
                                            "--"
                                    ) +
                                    "\nTarget: " +
                                    p.optString(
                                            "target_price",
                                            "--"
                                    ) +
                                    "\nP/L: " +
                                    p.optString(
                                            "pnl",
                                            "--"
                                    )
                            );

                        } else {

                            position.setText(
                                    "No active position."
                            );
                        }

                        JSONObject sig =
                                state.optJSONObject(
                                        "latest_signal"
                                );

                        if (sig != null) {

                            signal.setText(
                                    "TOKEN: " +
                                    sig.optString(
                                            "symbol",
                                            "--"
                                    ) +
                                    "\nSCORE: " +
                                    sig.optString(
                                            "score",
                                            "--"
                                    ) +
                                    "\nBUYER STRENGTH: " +
                                    sig.optString(
                                            "buy_ratio",
                                            "--"
                                    ) +
                                    "\nPRICE: " +
                                    sig.optString(
                                            "price_usd",
                                            "--"
                                    )
                            );

                            scanner.setText(
                                    "Latest candidate: " +
                                    sig.optString(
                                            "symbol",
                                            "--"
                                    ) +
                                    "\nScore: " +
                                    sig.optString(
                                            "score",
                                            "--"
                                    ) +
                                    "\nBuyer strength: " +
                                    sig.optString(
                                            "buy_ratio",
                                            "--"
                                    )
                            );

                        } else {

                            signal.setText(
                                    "No signal received."
                            );

                            scanner.setText(
                                    "Scanner active • awaiting qualified candidate."
                            );
                        }

                        long updated =
                                state.optLong(
                                        "updated_at",
                                        0
                                );

                        lastUpdate.setText(
                                "Last cloud update: " +
                                String.valueOf(updated)
                        );

                        loadEvents();

                    } catch(Exception e) {

                        connection.setText(
                                "● DATA ERROR"
                        );

                        connection.setTextColor(
                                Color.rgb(255,160,80)
                        );
                    }

                    loading = false;
                });

            } catch(Exception e) {

                runOnUiThread(() -> {

                    connection.setText(
                            "● CLOUD OFFLINE"
                    );

                    connection.setTextColor(
                            Color.rgb(255,90,90)
                    );

                    status.setText(
                            "STATUS: CONNECTION ERROR"
                    );

                    loading = false;
                });
            }

        }).start();
    }

    private String formatPosition(JSONObject p) {

        return
                "Symbol: " +
                p.optString("symbol","--") +
                "\nEntry: " +
                p.optString("entry_price","--") +
                "\nCurrent: " +
                p.optString("current_price","--") +
                "\nTarget: " +
                p.optString("target_price","--") +
                "\nP/L: " +
                p.optString("pnl","--");
    }

    private void loadEvents() {

        new Thread(() -> {

            try {

                String raw =
                        get("/events");

                JSONArray arr =
                        new JSONArray(raw);

                StringBuilder out =
                        new StringBuilder();

                int start =
                        Math.max(
                                0,
                                arr.length() - 18
                        );

                for(int i=start;i<arr.length();i++) {

                    JSONObject e =
                            arr.getJSONObject(i);

                    String kind =
                            e.optString(
                                    "kind",
                                    "EVENT"
                            );

                    String message =
                            e.optString(
                                    "message",
                                    ""
                            );

                    String time =
                            e.optString(
                                    "timestamp",
                                    ""
                            );

                    out.append("• ")
                       .append(kind)
                       .append("\n")
                       .append(message)
                       .append("\n");

                    if(!time.isEmpty()) {

                        out.append(time)
                           .append("\n");
                    }

                    out.append("\n");
                }

                String result =
                        out.length() == 0 ?
                        "LIVE • No events yet." :
                        out.toString();

                runOnUiThread(() ->
                        activity.setText(result));

            } catch(Exception ignored) {

            }

        }).start();
    }

    private void control(String path) {

        new Thread(() -> {

            try {

                get(path);

                runOnUiThread(() ->
                        refresh());

            } catch(Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Cloud command failed",
                                Toast.LENGTH_SHORT
                        ).show());
            }

        }).start();
    }

    private String get(String path)
            throws Exception {

        URL url =
                new URL(
                        CloudConfig.BASE_URL + path
                );

        HttpURLConnection c =
                (HttpURLConnection)
                url.openConnection();

        c.setRequestMethod("GET");
        c.setConnectTimeout(15000);
        c.setReadTimeout(20000);

        InputStream in =
                c.getResponseCode() >= 400 ?
                c.getErrorStream() :
                c.getInputStream();

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(in)
                );

        StringBuilder sb =
                new StringBuilder();

        String line;

        while((line = br.readLine()) != null) {

            sb.append(line);
        }

        br.close();
        c.disconnect();

        return sb.toString();
    }

    public static class GradientDrawableHelper {

        public static void background(
                View view,
                int color,
                int radius) {

            android.graphics.drawable.GradientDrawable g =
                    new android.graphics.drawable.GradientDrawable();

            g.setColor(color);
            g.setCornerRadius(radius);

            view.setBackground(g);
        }
    }
}
