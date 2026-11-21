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
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.InputType;

public class MainActivity extends Activity {

    private final int bg = Color.rgb(8, 7, 19);
    private final int card = Color.rgb(21, 19, 41);
    private final int purple = Color.rgb(131, 30, 255);
    private final int blue = Color.rgb(0, 200, 255);
    private final int green = Color.rgb(50, 232, 176);
    private final int dim = Color.rgb(153, 153, 170);

    private static final int REQ_VPN = 100;

    private static final String[] IDS = {
        "turkey", "germany", "netherlands", "uk", "usa"
    };

    private static final String[] LABELS = {
        "🇹🇷  ترکیه - استانبول",
        "🇩🇪  آلمان - فرانکفورت",
        "🇳🇱  هلند - آمستردام",
        "🇬🇧  انگلستان - لندن",
        "🇺🇸  آمریکا - نیویورک"
    };

    private VpnController vpn;
    private SharedPreferences prefs;

    private String page = "splash";
    private boolean connected = false;
    private boolean busy = false;

    private TextView status;
    private TextView power;
    private TextView selectedServer;

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

    private GradientDrawable outline(int color, int stroke, int radius) {
        GradientDrawable d = shape(color, radius);
        d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create(
            "sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setPadding(dp(5), dp(5), dp(5), dp(5));
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

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(
            w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    private void gap(LinearLayout parent, int height) {
        parent.addView(new View(this), lp(1, height));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        vpn = new VpnController(this);
        prefs = getSharedPreferences("vpn", MODE_PRIVATE);

        if (prefs.contains("conf") && !prefs.contains("conf_turkey")) {
            prefs.edit()
                .putString("conf_turkey", prefs.getString("conf", ""))
                .remove("conf")
                .apply();
        }

        if (!prefs.contains("selected_server")) {
            prefs.edit().putString("selected_server", "turkey").apply();
        }

        render();
    }

    private String serverId() {
        return prefs.getString("selected_server", "turkey");
    }

    private int serverIndex(String id) {
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i].equals(id)) return i;
        }
        return 0;
    }

    private String serverLabel() {
        int i = serverIndex(serverId());
        String conf = prefs.getString("conf_" + IDS[i], "");
        return LABELS[i] +
            (conf.trim().isEmpty() ? " • نیازمند کانفیگ" : " • آماده");
    }

    private boolean hasConfig(String id) {
        String conf = prefs.getString("conf_" + id, "");
        return conf != null && !conf.trim().isEmpty();
    }

    private void render() {
        LinearLayout root = column();
        root.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = column();
        content.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        if (page.equals("splash")) {
            splash(content);
        } else if (page.equals("home")) {
            home(content);
        } else if (page.equals("servers")) {
            servers(content);
        } else if (page.equals("connected")) {
            connectedPage(content);
        } else if (page.equals("settings")) {
            settings(content);
        } else if (page.equals("profile")) {
            profile(content);
        }

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        if (!page.equals("splash")) {
            bottomNav(root);
        }

        setContentView(root);
    }

    private void header(LinearLayout parent, String subtitle) {
        LinearLayout h = row();

        TextView logo = text("⬡", 30, purple, true);
        h.addView(logo, lp(45, 48));

        LinearLayout names = column();
        names.setGravity(Gravity.CENTER_VERTICAL);
        names.addView(text("HAMED VPN", 20, Color.WHITE, true));
        names.addView(text(subtitle, 11, dim, false));

        h.addView(names, new LinearLayout.LayoutParams(0, dp(52), 1));
        h.addView(text("✦", 25, blue, true), lp(40, 48));

        parent.addView(h, lp(-1, 55));
    }

    private void splash(LinearLayout root) {
        gap(root, 65);

        TextView shield = text("⬡", 100, purple, true);
        shield.setBackground(outline(card, purple, 35));
        root.addView(shield, lp(170, 170));

        gap(root, 24);
        root.addView(text("HAMED VPN", 29, Color.WHITE, true));
        root.addView(text("FAST  •  SECURE  •  PRIVATE", 12, blue, true));

        gap(root, 18);
        root.addView(text(
            "آزادی در اینترنت، حق شماست",
            16, Color.WHITE, true));

        root.addView(text(
            "اتصال امن و ساده با پروتکل WireGuard",
            12, dim, false));

        gap(root, 65);

        Button start = button("شروع استفاده", purple);
        root.addView(start, lp(-1, 54));
        start.setOnClickListener(v -> {
            page = "home";
            render();
        });
    }

    private Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(shape(color, 18));
        return b;
    }

    private void home(LinearLayout root) {
        header(root, "اتصال امن، سریع و خصوصی");

        gap(root, 12);

        LinearLayout statusCard = row();
        statusCard.setBackground(shape(card, 18));
        status = text(
            busy ? "در حال انجام عملیات..." :
            connected ? "وضعیت: متصل" : "وضعیت: آماده اتصال",
            14, connected ? green : Color.WHITE, true);

        statusCard.addView(text(
            connected ? "●" : "●", 13,
            connected ? green : dim, true), lp(25, 45));
        statusCard.addView(status,
            new LinearLayout.LayoutParams(0, dp(45), 1));

        root.addView(statusCard, lp(-1, 52));

        gap(root, 24);

        FrameLayout frame = new FrameLayout(this);
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setColor(card);
        ring.setStroke(dp(5), connected ? green : purple);
        frame.setBackground(ring);
        frame.setElevation(dp(12));

        power = text(
            connected ? "⏻\nقطع اتصال" :
            busy ? "...\nلطفاً صبر کنید" : "⏻\nاتصال",
            24, Color.WHITE, true);

        power.setBackground(shape(
            connected ? Color.rgb(5, 120, 95) : purple, 100));

        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
            dp(145), dp(145), Gravity.CENTER);
        frame.addView(power, pp);

        root.addView(frame, lp(180, 180));

        power.setOnClickListener(v -> toggleVpn());

        gap(root, 12);
        root.addView(text(
            connected ? "برای قطع اتصال لمس کنید" :
            "برای اتصال، دکمه را لمس کنید",
            12, dim, false));

        gap(root, 22);

        LinearLayout server = row();
        server.setBackground(outline(card, purple, 18));
        server.setPadding(dp(12), dp(8), dp(12), dp(8));

        server.addView(text("🌍", 27, Color.WHITE, false), lp(48, 55));

        selectedServer = text(serverLabel(), 13, Color.WHITE, true);
        server.addView(selectedServer,
            new LinearLayout.LayoutParams(0, dp(55), 1));

        server.addView(text("‹", 27, blue, true), lp(30, 55));

        root.addView(server, lp(-1, 75));
        server.setOnClickListener(v -> {
            page = "servers";
            render();
        });

        gap(root, 18);
        root.addView(text(
            "آمار سرعت و مصرف پس از پیاده‌سازی اندازه‌گیری واقعی نمایش داده می‌شود.",
            11, dim, false));
    }

    private void servers(LinearLayout root) {
        header(root, "انتخاب موقعیت اتصال");

        gap(root, 14);
        root.addView(text("انتخاب سرور", 21, Color.WHITE, true));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(dim);
        search.setHint("جست‌وجوی کشور یا شهر");
        search.setBackground(shape(card, 14));
        search.setPadding(dp(14), 0, dp(14), 0);
        root.addView(search, lp(-1, 50));

        LinearLayout list = column();
        list.setGravity(Gravity.TOP);
        root.addView(list, lp(-1, -2));

        Runnable refresh = () -> {
            list.removeAllViews();
            String q = search.getText().toString().trim().toLowerCase();

            for (int i = 0; i < IDS.length; i++) {
                final int index = i;

                if (!LABELS[i].toLowerCase().contains(q)) continue;

                LinearLayout item = row();
                item.setBackground(outline(
                    card, IDS[i].equals(serverId()) ? purple : card, 16));
                item.setPadding(dp(8), dp(5), dp(8), dp(5));

                item.addView(text("🌐", 23, Color.WHITE, false),
                    lp(48, 58));

                LinearLayout info = column();
                info.setGravity(Gravity.CENTER_VERTICAL);
                info.addView(text(LABELS[i], 13, Color.WHITE, true));

                String detail = hasConfig(IDS[i])
                    ? "کانفیگ ذخیره شده"
                    : "نیازمند کانفیگ واقعی";

                info.addView(text(detail, 10,
                    hasConfig(IDS[i]) ? green : dim, false));

                item.addView(info,
                    new LinearLayout.LayoutParams(0, dp(60), 1));

                item.addView(text("›", 25, blue, true),
                    lp(35, 55));

                list.addView(item, lp(-1, 72));
                gap(list, 9);

                item.setOnClickListener(v -> {
                    if (connected || busy) {
                        Toast.makeText(this,
                            "ابتدا اتصال فعلی را قطع کنید",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prefs.edit()
                        .putString("selected_server", IDS[index])
                        .apply();

                    if (!hasConfig(IDS[index])) {
                        showConfigDialog(index);
                    } else {
                        page = "home";
                        render();
                    }
                });
            }
        };

        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) {
                refresh.run();
            }
            public void afterTextChanged(android.text.Editable e) {}
        });

        refresh.run();

        gap(root, 12);
        Button back = button("بازگشت به خانه", purple);
        root.addView(back, lp(-1, 50));
        back.setOnClickListener(v -> {
            page = "home";
            render();
        });
    }

    private void connectedPage(LinearLayout root) {
        header(root, "وضعیت اتصال");

        gap(root, 45);
        root.addView(text("✓", 65, green, true));
        root.addView(text("اتصال برقرار است", 23, green, true));

        gap(root, 12);
        root.addView(text(
            "سرور: " + serverLabel(),
            14, Color.WHITE, true));

        gap(root, 30);
        root.addView(text(
            "تونل WireGuard فعال است.",
            12, dim, false));

        gap(root, 35);
        Button disconnect = button("قطع اتصال", Color.rgb(160, 35, 65));
        root.addView(disconnect, lp(-1, 54));
        disconnect.setOnClickListener(v -> toggleVpn());

        gap(root, 12);
        Button home = button("بازگشت به خانه", purple);
        root.addView(home, lp(-1, 50));
        home.setOnClickListener(v -> {
            page = "home";
            render();
        });
    }

    private void settings(LinearLayout root) {
        header(root, "تنظیمات برنامه");

        gap(root, 20);
        root.addView(text("تنظیمات عمومی", 19, Color.WHITE, true));

        addSetting(root, "اعلان‌ها", "notifications", true);
        addSetting(root, "حالت تاریک", "dark_mode", true);

        gap(root, 18);
        root.addView(text("تنظیمات امنیتی", 19, Color.WHITE, true));

        LinearLayout kill = row();
        kill.setBackground(shape(card, 15));
        kill.addView(text(
            "Kill Switch", 14, Color.WHITE, true),
            new LinearLayout.LayoutParams(0, dp(60), 1));
        kill.addView(text("در این نسخه پیاده‌سازی نشده",
            10, dim, false), lp(150, 60));
        root.addView(kill, lp(-1, 65));

        addSetting(root, "زبان: فارسی", "language", false);

        gap(root, 18);
        root.addView(text(
            "پروتکل اتصال: WireGuard",
            13, blue, true));
        root.addView(text(
            "نسخه برنامه: 1.1",
            12, dim, false));
    }

    private void addSetting(
        LinearLayout root, String label,
        String key, boolean toggle) {

        LinearLayout item = row();
        item.setBackground(shape(card, 15));
        item.setPadding(dp(12), 0, dp(12), 0);

        item.addView(text(label, 14, Color.WHITE, true),
            new LinearLayout.LayoutParams(0, dp(58), 1));

        if (toggle) {
            Switch sw = new Switch(this);
            sw.setChecked(prefs.getBoolean(key, true));
            sw.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(key, checked).apply());
            item.addView(sw);
        } else {
            item.addView(text("فعال", 12, green, false), lp(55, 58));
        }

        root.addView(item, lp(-1, 65));
        gap(root, 8);
    }

    private void profile(LinearLayout root) {
        header(root, "حساب کاربری");

        gap(root, 30);
        root.addView(text("◉", 65, purple, true));
        root.addView(text("HAMED VPN", 23, Color.WHITE, true));

        gap(root, 20);
        root.addView(text(
            "پروفایل کاربری",
            16, blue, true));

        gap(root, 12);
        root.addView(text(
            "مدیریت حساب و اشتراک در نسخه‌های بعدی اضافه می‌شود.",
            13, dim, false));

        gap(root, 25);
        Button support = button("پشتیبانی", purple);
        root.addView(support, lp(-1, 50));
        support.setOnClickListener(v ->
            Toast.makeText(this,
                "بخش پشتیبانی هنوز راه‌اندازی نشده است",
                Toast.LENGTH_SHORT).show());

        gap(root, 12);
        root.addView(text(
            "اطلاعات مصرف و اشتراک فعلاً در دسترس نیست.",
            11, dim, false));
    }

    private void bottomNav(LinearLayout root) {
        LinearLayout nav = row();
        nav.setBackground(shape(card, 18));
        nav.setPadding(dp(3), dp(5), dp(3), dp(5));

        addNav(nav, "⌂\nخانه", "home");
        addNav(nav, "◎\nسرورها", "servers");
        addNav(nav, "✓\nاتصال", "connected");
        addNav(nav, "⚙\nتنظیمات", "settings");
        addNav(nav, "◉\nپروفایل", "profile");

        root.addView(nav, lp(-1, 62));
    }

    private void addNav(
        LinearLayout nav, String label, String destination) {

        TextView item = text(label, 10,
            page.equals(destination) ? blue : dim, true);

        nav.addView(item,
            new LinearLayout.LayoutParams(0, dp(52), 1));

        item.setOnClickListener(v -> {
            if (destination.equals("connected") && !connected) {
                Toast.makeText(this,
                    "هنوز به VPN متصل نیستید",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            page = destination;
            render();
        });
    }

    private void showConfigDialog(int index) {
        EditText input = new EditText(this);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(dim);
        input.setHint("[Interface] ...");
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(8);
        input.setInputType(
            InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("کانفیگ " + LABELS[index])
            .setMessage(
                "متن کانفیگ واقعی WireGuard همین سرور را وارد کنید.")
            .setView(input)
            .setPositiveButton("ذخیره", null)
            .setNegativeButton("لغو", (d, w) -> {
                page = "servers";
                render();
            })
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String conf = input.getText().toString().trim();

                    if (!conf.contains("[Interface]") ||
                        !conf.contains("[Peer]")) {
                        Toast.makeText(this,
                            "ساختار کانفیگ WireGuard معتبر به نظر نمی‌رسد",
                            Toast.LENGTH_LONG).show();
                        return;
                    }

                    prefs.edit()
                        .putString("conf_" + IDS[index], conf)
                        .putString("selected_server", IDS[index])
                        .apply();

                    dialog.dismiss();
                    page = "home";
                    render();
                    Toast.makeText(this,
                        "کانفیگ ذخیره شد؛ اعتبار اتصال هنوز آزمایش نشده است",
                        Toast.LENGTH_LONG).show();
                });
        });

        dialog.show();
    }

    private void toggleVpn() {
        if (busy) return;

        if (connected) {
            busy = true;
            if (status != null) status.setText("در حال قطع اتصال...");
            render();

            vpn.disconnect((on, error) -> {
                connected = on;
                busy = false;
                page = "home";
                render();

                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        String conf = prefs.getString("conf_" + serverId(), "");

        if (conf.trim().isEmpty()) {
            Toast.makeText(this,
                "برای این سرور ابتدا کانفیگ وارد کنید",
                Toast.LENGTH_LONG).show();
            page = "servers";
            render();
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
        String conf = prefs.getString("conf_" + serverId(), "");

        if (conf.trim().isEmpty()) return;

        busy = true;
        page = "home";
        render();

        vpn.connect(conf, (on, error) -> {
            connected = on;
            busy = false;
            page = on ? "connected" : "home";
            render();

            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(
        int request, int result, Intent data) {

        super.onActivityResult(request, result, data);

        if (request == REQ_VPN) {
            if (result == RESULT_OK) {
                doConnect();
            } else {
                Toast.makeText(this,
                    "اجازه ایجاد اتصال VPN داده نشد",
                    Toast.LENGTH_LONG).show();
                page = "home";
                render();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (page.equals("splash")) {
            super.onBackPressed();
        } else if (page.equals("home")) {
            super.onBackPressed();
        } else {
            page = "home";
            render();
        }
    }
}
