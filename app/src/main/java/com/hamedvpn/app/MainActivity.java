
package com.hamedvpn.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.VpnService;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private final int bg = Color.rgb(11, 8, 32);
    private final int card = Color.rgb(22, 18, 46);
    private final int purple = Color.rgb(139, 92, 246);
    private final int green = Color.rgb(34, 197, 94);
    private final int dim = Color.rgb(156, 163, 199);

    private static final String POWER = "\u23FB";
    private static final int REQ_VPN = 100;

    private static final String[] SERVER_IDS = {
            "turkey", "germany", "netherlands"
    };

    private static final String[] SERVER_LABELS = {
            "🇹🇷  ترکیه",
            "🇩🇪  آلمان",
            "🇳🇱  هلند"
    };

    private TextView status;
    private TextView connect;
    private boolean connected = false;
    private boolean busy = false;
    private VpnController vpn;

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable gradient(int c1, int c2, int radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{c1, c2});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size,
                          int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create(
                "sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout.LayoutParams params(int w, int h) {
        return new LinearLayout.LayoutParams(
                w < 0 ? w : dp(w),
                h < 0 ? h : dp(h));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        vpn = new VpnController(this);

        SharedPreferences prefs =
                getSharedPreferences("vpn", MODE_PRIVATE);

        // انتقال کانفیگ نسخه قبلی به ترکیه
        if (prefs.contains("conf") &&
                !prefs.contains("conf_turkey")) {
            prefs.edit()
                    .putString("conf_turkey",
                            prefs.getString("conf", ""))
                    .remove("conf")
                    .apply();
        }

        if (!prefs.contains("selected_server")) {
            prefs.edit()
                    .putString("selected_server", "turkey")
                    .apply();
        }

        LinearLayout root = column();
        root.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(16));
        root.setBackgroundColor(bg);

        // Header
        LinearLayout header = row();

        TextView menu = text("☰", 25, purple, true);
        header.addView(menu, params(40, 45));

        TextView brand = text("HAMED VPN", 22, Color.WHITE, true);
        header.addView(brand,
                new LinearLayout.LayoutParams(0, dp(45), 1));

        TextView crown = text("♛", 27,
                Color.rgb(251, 191, 36), true);
        header.addView(crown, params(40, 45));

        root.addView(header, params(-1, 48));

        TextView subtitle = text(
                "اتصال امن، سریع و بدون محدودیت",
                13, dim, false);
        root.addView(subtitle, params(-1, 30));

        // Status card
        LinearLayout statusCard = row();
        statusCard.setBackground(shape(card, 30));
        statusCard.setPadding(dp(15), dp(10), dp(15), dp(10));

        View dot = new View(this);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(dim);
        dot.setBackground(dotBg);
        statusCard.addView(dot, params(9, 9));

        status = text("وضعیت: آماده اتصال", 13,
                Color.WHITE, true);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(-2, dp(30));
        statusParams.leftMargin = dp(8);
        statusCard.addView(status, statusParams);

        LinearLayout.LayoutParams statusCardParams =
                params(-2, 48);
        statusCardParams.topMargin = dp(18);
        root.addView(statusCard, statusCardParams);

        // Power button
        FrameLayout powerFrame = new FrameLayout(this);

        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setColor(card);
        ring.setStroke(dp(6), purple);

        powerFrame.setBackground(ring);
        powerFrame.setElevation(dp(18));

        connect = text(POWER + "\nاتصال", 23,
                Color.WHITE, true);

        connect.setBackground(gradient(
                purple, Color.rgb(91, 33, 182), 100));

        FrameLayout.LayoutParams powerParams =
                new FrameLayout.LayoutParams(
                        dp(150), dp(150), Gravity.CENTER);

        powerFrame.addView(connect, powerParams);

        connect.setOnClickListener(v -> {
            if (busy) return;

            if (connected) {
                busy = true;
                status.setText("وضعیت: در حال قطع...");
                vpn.disconnect(
                        (on, err) -> finishAction(on, err));
            } else {
                startVpn();
            }
        });

        LinearLayout.LayoutParams powerFrameParams =
                params(190, 190);
        powerFrameParams.topMargin = dp(35);
        root.addView(powerFrame, powerFrameParams);

        TextView hint = text(
                "برای شروع، دکمه اتصال را لمس کنید",
                13, dim, false);

        LinearLayout.LayoutParams hintParams = params(-1, 40);
        hintParams.topMargin = dp(8);
        root.addView(hint, hintParams);

        // Statistics
        LinearLayout stats = row();
        stats.setBackground(shape(card, 20));
        stats.setPadding(dp(5), dp(14), dp(5), dp(14));

        addStat(stats, "⏱", "00:00:00", "مدت اتصال");
        addStat(stats, "↓", "0.0", "دانلود KB/s");
        addStat(stats, "↑", "0.0", "آپلود KB/s");

        LinearLayout.LayoutParams statsParams = params(-1, 100);
        statsParams.topMargin = dp(18);
        root.addView(stats, statsParams);

        // Server card
        LinearLayout server = row();
        server.setBackground(shape(card, 20));
        server.setPadding(dp(15), dp(12), dp(15), dp(12));

        TextView flag = text("🌍", 28, Color.WHITE, false);
        server.addView(flag, params(48, 48));

        LinearLayout serverInfo = column();
        serverInfo.setGravity(Gravity.CENTER_VERTICAL);

        TextView serverTitle = text(
                "سرور انتخابی", 12, dim, false);

        final TextView serverName = text(
                getSelectedServerLabel(),
                14, Color.WHITE, true);

        serverInfo.addView(serverTitle);
        serverInfo.addView(serverName);

        server.addView(serverInfo,
                new LinearLayout.LayoutParams(0, dp(55), 1));

        TextView arrow = text("‹", 30, dim, true);
        server.addView(arrow, params(30, 48));

        LinearLayout.LayoutParams serverParams = params(-1, 78);
        serverParams.topMargin = dp(15);
        root.addView(server, serverParams);

        server.setOnClickListener(
                v -> showServerDialog(serverName));

        TextView footer = text(
                "HAMED VPN  •  اتصال امن شما",
                11, dim, false);

        LinearLayout.LayoutParams footerParams = params(-1, 35);
        footerParams.topMargin = dp(10);
        root.addView(footer, footerParams);

        setContentView(root);
    }

    private void addStat(LinearLayout parent,
                         String icon, String value, String label) {
        LinearLayout item = column();

        item.addView(text(icon, 22, purple, true));
        item.addView(text(value, 14, Color.WHITE, true));
        item.addView(text(label, 10, dim, false));

        parent.addView(item,
                new LinearLayout.LayoutParams(0, -1, 1));
    }

    private String getSelectedServerId() {
        return getSharedPreferences("vpn", MODE_PRIVATE)
                .getString("selected_server", "turkey");
    }

    private String getSelectedServerLabel() {
        String id = getSelectedServerId();

        for (int i = 0; i < SERVER_IDS.length; i++) {
            if (SERVER_IDS[i].equals(id)) {

                String conf = getSharedPreferences(
                        "vpn", MODE_PRIVATE)
                        .getString("conf_" + id, null);

                return SERVER_LABELS[i] +
                        (conf == null || conf.trim().isEmpty()
                                ? " • نیازمند کانفیگ"
                                : " • آماده");
            }
        }

        return "سروری انتخاب نشده";
    }

    private void applyUi(boolean on) {
        connected = on;

        if (on) {
            status.setText("وضعیت: متصل");
            connect.setText(POWER + "\nقطع اتصال");
            connect.setBackground(gradient(
                    green, Color.rgb(5, 150, 105), 100));
        } else {
            status.setText("وضعیت: آماده اتصال");
            connect.setText(POWER + "\nاتصال");
            connect.setBackground(gradient(
                    purple, Color.rgb(91, 33, 182), 100));
        }
    }

    private void finishAction(boolean on, String err) {
        busy = false;
        applyUi(on);

        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        }
    }

    private void startVpn() {
        String conf = getSharedPreferences("vpn", MODE_PRIVATE)
                .getString("conf_" + getSelectedServerId(), null);

        if (conf == null || conf.trim().isEmpty()) {
            Toast.makeText(this,
                    "برای این سرور هنوز کانفیگ وارد نشده است",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent prepare = VpnService.prepare(this);

        if (prepare != null) {
            startActivityForResult(prepare, REQ_VPN);
        } else {
            doConnect();
        }
    }

    private void doConnect() {
        String conf = getSharedPreferences("vpn", MODE_PRIVATE)
                .getString("conf_" + getSelectedServerId(), null);

        if (conf == null || conf.trim().isEmpty()) return;

        busy = true;
        status.setText("وضعیت: در حال اتصال...");

        vpn.connect(conf,
                (on, err) -> finishAction(on, err));
    }

    @Override
    protected void onActivityResult(
            int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == REQ_VPN && res == RESULT_OK) {
            doConnect();
        }
    }

    private void showServerDialog(final TextView nameView) {
        SharedPreferences prefs =
                getSharedPreferences("vpn", MODE_PRIVATE);

        String[] items = new String[SERVER_IDS.length];

        for (int i = 0; i < SERVER_IDS.length; i++) {
            String id = SERVER_IDS[i];

            String conf = prefs.getString("conf_" + id, null);

            items[i] = SERVER_LABELS[i] +
                    (conf == null || conf.trim().isEmpty()
                            ? "   |   نیازمند کانفیگ"
                            : "   |   کانفیگ ذخیره شده");
        }

        new AlertDialog.Builder(this)
                .setTitle("انتخاب سرور")
                .setItems(items, (dialog, which) -> {

                    String id = SERVER_IDS[which];

                    prefs.edit()
                            .putString("selected_server", id)
                            .apply();

                    nameView.setText(getSelectedServerLabel());

                    String conf = prefs.getString(
                            "conf_" + id, null);

                    if (conf == null || conf.trim().isEmpty()) {

                        new AlertDialog.Builder(this)
                                .setTitle("تنظیم " +
                                        SERVER_LABELS[which])
                                .setMessage(
                                        "برای اتصال به این سرور، " +
                                        "کانفیگ واقعی WireGuard مربوط " +
                                        "به همین کشور را وارد کنید.")
                                .setPositiveButton(
                                        "وارد کردن کانفیگ",
                                        (d, w) -> showConfigDialog(
                                                nameView, id, which))
                                .setNegativeButton("بعداً", null)
                                .show();
                    }
                })
                .setNegativeButton("بستن", null)
                .show();
    }

    private void showConfigDialog(
            final TextView nameView,
            final String serverId,
            final int serverIndex) {

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        input.setMinLines(8);
        input.setGravity(Gravity.TOP | Gravity.START);

        new AlertDialog.Builder(this)
                .setTitle("کانفیگ " + SERVER_LABELS[serverIndex])
                .setMessage(
                        "محتوای فایل .conf مربوط به همین سرور را paste کنید")
                .setView(input)
                .setPositiveButton("ذخیره", (d, w) -> {

                    String conf = input.getText().toString().trim();

                    if (conf.isEmpty()) {
                        Toast.makeText(this,
                                "کانفیگ خالی است",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getSharedPreferences("vpn", MODE_PRIVATE)
                            .edit()
                            .putString("conf_" + serverId, conf)
                            .apply();

                    nameView.setText(getSelectedServerLabel());

                    Toast.makeText(this,
                            "کانفیگ این سرور ذخیره شد",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("لغو", null)
                .show();
    }
}
