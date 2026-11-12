package com.hamedvpn.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import android.view.View;

public class MainActivity extends Activity {

    private final int navy = Color.rgb(11, 21, 43);
    private final int panel = Color.rgb(22, 37, 67);
    private final int blue = Color.rgb(45, 137, 255);
    private TextView status;

    private int dp(float value) {
        return (int)(value *
        getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{start, end});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView label(String value, int size,
                           int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create("sans-serif",
            bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(8,16,33));
        getWindow().setNavigationBarColor(Color.rgb(8,16,33));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24),dp(24),dp(24),dp(22));
        root.setBackground(gradient(
            Color.rgb(17,34,65), navy, 0));

        TextView brand = label("HAMED VPN",29,Color.WHITE,true);
        brand.setLetterSpacing(0.08f);

        LinearLayout.LayoutParams bp =
            new LinearLayout.LayoutParams(-1,dp(48));
        bp.topMargin = dp(14);
        root.addView(brand,bp);

        TextView tagline = label(
            "اتصال امن، ساده و سریع",
            14,Color.rgb(160,184,220),false);
        root.addView(tagline,
            new LinearLayout.LayoutParams(-1,dp(30)));

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setGravity(Gravity.CENTER);
        statusCard.setPadding(dp(16),dp(14),dp(16),dp(14));
        statusCard.setBackground(rounded(panel,18));

        TextView heading = label(
            "وضعیت اتصال",13,Color.rgb(145,169,205),false);

        status = label(
            "هنوز متصل نشده‌اید",17,Color.WHITE,true);

        status.setPadding(0,dp(5),0,0);
        statusCard.addView(heading);
        statusCard.addView(status);

        LinearLayout.LayoutParams sp =
            new LinearLayout.LayoutParams(-1,dp(88));
        sp.topMargin = dp(34);
        root.addView(statusCard,sp);

        FrameLayout buttonFrame = new FrameLayout(this);

        GradientDrawable outer = new GradientDrawable();
        outer.setShape(GradientDrawable.OVAL);
        outer.setColor(Color.rgb(24,59,105));
        outer.setStroke(dp(2),Color.rgb(53,126,215));

        buttonFrame.setBackground(outer);
        buttonFrame.setElevation(dp(12));

        TextView connect = label("اتصال",23,Color.WHITE,true);
        connect.setBackground(gradient(
            Color.rgb(48,151,255),
            Color.rgb(24,93,196),100));

        FrameLayout.LayoutParams cp =
            new FrameLayout.LayoutParams(
                dp(148),dp(148),Gravity.CENTER);

        buttonFrame.addView(connect,cp);

        connect.setOnClickListener(v ->
            status.setText(
                "برای اتصال، ابتدا سرور VPN باید تنظیم شود.")
        );

        LinearLayout.LayoutParams btnp =
            new LinearLayout.LayoutParams(dp(184),dp(184));
        btnp.gravity = Gravity.CENTER_HORIZONTAL;
        btnp.topMargin = dp(44);
        root.addView(buttonFrame,btnp);

        TextView hint = label(
            "برای شروع، دکمه اتصال را لمس کنید",
            13,Color.rgb(151,174,209),false);

        LinearLayout.LayoutParams hp =
            new LinearLayout.LayoutParams(-1,dp(42));
        hp.topMargin = dp(10);
        root.addView(hint,hp);

        LinearLayout serverCard = new LinearLayout(this);
        serverCard.setOrientation(LinearLayout.VERTICAL);
        serverCard.setGravity(Gravity.CENTER);
        serverCard.setPadding(dp(16),dp(13),dp(16),dp(13));
        serverCard.setBackground(rounded(panel,18));

        TextView serverTitle = label(
            "سرور انتخابی",13,Color.rgb(145,169,205),false);

        TextView serverValue = label(
            "هنوز سروری تنظیم نشده",15,Color.WHITE,true);

        serverValue.setPadding(0,dp(5),0,0);
        serverCard.addView(serverTitle);
        serverCard.addView(serverValue);

        LinearLayout.LayoutParams serverParams =
            new LinearLayout.LayoutParams(-1,dp(82));
        serverParams.topMargin = dp(28);
        root.addView(serverCard,serverParams);

        TextView footer = label(
            "HAMED VPN  •  اتصال امن شما",
            11,Color.rgb(105,130,169),false);

        LinearLayout.LayoutParams fp =
            new LinearLayout.LayoutParams(-1,dp(42));
        fp.topMargin = dp(18);
        root.addView(footer,fp);

        setContentView(root);
    }
}
