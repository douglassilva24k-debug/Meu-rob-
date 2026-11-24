package com.douglas.robo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
    private WebView web;
    private boolean telaCheia = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    public void run() { request.grant(request.getResources()); }
                });
            }
        });
        web.addJavascriptInterface(new Ponte(), "Android");
        web.loadUrl("file:///android_asset/painel.html");
        pedirPermissoes();
    }

    private void pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void mostrarRosto() {
        telaCheia = true;
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        web.loadUrl("file:///android_asset/face.html");
    }

    @Override
    public void onBackPressed() {
        if (telaCheia) {
            telaCheia = false;
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            web.loadUrl("file:///android_asset/painel.html");
        } else {
            super.onBackPressed();
        }
    }

    public class Ponte {
        @JavascriptInterface
        public void abrirTelaCheia() {
            runOnUiThread(new Runnable() { public void run() { mostrarRosto(); } });
        }

        @JavascriptInterface
        public void ativarFlutuante() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(MainActivity.this)) {
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                        return;
                    }
                    startService(new Intent(MainActivity.this, FloatingService.class));
                    moveTaskToBack(true);
                }
            });
        }

        @JavascriptInterface
        public void pararFlutuante() {
            runOnUiThread(new Runnable() {
                public void run() { stopService(new Intent(MainActivity.this, FloatingService.class)); }
            });
        }

        @JavascriptInterface
        public void alternarFundo() {
            runOnUiThread(new Runnable() {
                public void run() {
                    Intent i = new Intent(MainActivity.this, FloatingService.class);
                    i.setAction("FUNDO");
                    startService(i);
                }
            });
        }

        @JavascriptInterface
        public void alternarTamanho() {
            runOnUiThread(new Runnable() {
                public void run() {
                    Intent i = new Intent(MainActivity.this, FloatingService.class);
                    i.setAction("TAMANHO");
                    startService(i);
                }
            });
        }

        @JavascriptInterface
        public boolean abrirApp(String pacote) {
            Intent i = getPackageManager().getLaunchIntentForPackage(pacote);
            if (i == null) return false;
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }
    }
}
