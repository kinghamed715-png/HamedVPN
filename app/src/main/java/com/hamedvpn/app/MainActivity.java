package com.hamedvpn.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.rgb(16, 27, 53));

        TextView title = new TextView(this);
        title.setText("HAMED VPN");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("هنوز به سرور متصل نشده‌اید");
        status.setTextSize(17);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);

        Button connect = new Button(this);
        connect.setText("اتصال");
        connect.setOnClickListener(v ->
            status.setText("برای اتصال، ابتدا سرور VPN باید تنظیم شود.")
        );

        layout.addView(title);
        layout.addView(status);
        layout.addView(connect);

        setContentView(layout);
    }
}
