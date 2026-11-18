
package com.hamedvpn.app;

import android.app.Activity;

import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class VpnController {

    public interface Callback {
        void onDone(boolean connected, String error);
    }

    private final Activity activity;
    private final GoBackend backend;

    private Config config;

    private final Tunnel tunnel = new Tunnel() {
        @Override
        public String getName() {
            return "hamedvpn";
        }

        @Override
        public void onStateChange(State newState) {
        }
    };

    public VpnController(Activity activity) {
        this.activity = activity;
        this.backend = new GoBackend(
                activity.getApplicationContext()
        );
    }

    public void connect(final String conf, final Callback cb) {

        new Thread(() -> {
            try {
                if (conf == null || conf.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "کانفیگ WireGuard خالی است"
                    );
                }

                Config newConfig = Config.parse(
                        new ByteArrayInputStream(
                                conf.getBytes(StandardCharsets.UTF_8)
                        )
                );

                backend.setState(
                        tunnel,
                        Tunnel.State.UP,
                        newConfig
                );

                config = newConfig;

                post(cb, true, null);

            } catch (Exception e) {

                post(
                        cb,
                        false,
                        e.getClass().getSimpleName()
                                + ": "
                                + e.getMessage()
                );
            }

        }, "HAMED-VPN-Connect").start();
    }

    public void disconnect(final Callback cb) {

        new Thread(() -> {
            try {

                backend.setState(
                        tunnel,
                        Tunnel.State.DOWN,
                        null
                );

                config = null;

                post(cb, false, null);

            } catch (Exception e) {

                post(
                        cb,
                        true,
                        e.getClass().getSimpleName()
                                + ": "
                                + e.getMessage()
                );
            }

        }, "HAMED-VPN-Disconnect").start();
    }

    private void post(
            final Callback cb,
            final boolean connected,
            final String error
    ) {
        activity.runOnUiThread(() -> {
            if (cb != null) {
                cb.onDone(connected, error);
            }
        });
    }
}
