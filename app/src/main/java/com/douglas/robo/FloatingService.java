package com.douglas.robo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class FloatingService extends Service {
    private WindowManager wm;
    private WebView web;
    private WindowManager.LayoutParams lp;
    private boolean comFundo = true;
    private boolean pequeno = true;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (web == null) criar();
        String acao = intent == null ? null : intent.getAction();
        if ("FUNDO".equals(acao)) {
            comFundo = !comFundo;
            web.evaluateJavascript("fundo(" + comFundo + ")", null);
        } else if ("TAMANHO".equals(acao)) {
            pequeno = !pequeno;
            lp.width = dp(pequeno ? 160 : 300);
            lp.height = dp(pequeno ? 100 : 190);
            wm.updateViewLayout(web, lp);
        }
        return START_STICKY;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    private void criar() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        web = new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setAllowFileAccess(true);
        web.loadUrl("file:///android_asset/face.html");

        int tipo = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
        lp = new WindowManager.LayoutParams(dp(160), dp(100), tipo,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 40; lp.y = 200;

        web.setOnTouchListener(new View.OnTouchListener() {
            int ix, iy; float tx, ty; boolean moveu;
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ix = lp.x; iy = lp.y; tx = e.getRawX(); ty = e.getRawY(); moveu = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - tx), dy = (int) (e.getRawY() - ty);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moveu = true;
                        lp.x = ix + dx; lp.y = iy + dy;
                        wm.updateViewLayout(web, lp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moveu) web.evaluateJavascript("carinho()", null);
                        return true;
                }
                return false;
            }
        });
        wm.addView(web, lp);
    }

    @Override
    public void onDestroy() {
        if (web != null) { wm.removeView(web); web = null; }
        super.onDestroy();
    }
}
