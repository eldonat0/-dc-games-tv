package com.dcgames.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView clock;
    private final Handler handler = new Handler();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SharedPreferences prefs;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(timeFormat.format(new Date()));
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs = getSharedPreferences("dc_games_tv", MODE_PRIVATE);
        showHome();
        handler.post(clockTick);
        handler.postDelayed(() -> Toast.makeText(this,
                "Bienvenido a DC GAMES TV by El Donat0", Toast.LENGTH_LONG).show(), 450);
    }

    private void showHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(52), dp(24), dp(52), dp(30));
        root.setBackground(makeGradient(Color.rgb(5, 10, 18), Color.rgb(18, 31, 49), 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout brandBox = new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.HORIZONTAL);
        brandBox.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        byte[] decoded = Base64.decode(LogoData.JPG_BASE64, Base64.DEFAULT);
        logo.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        brandBox.addView(logo, new LinearLayout.LayoutParams(dp(230), dp(150)));

        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(16), 0, 0, 0);
        TextView brand = text("DC GAMES TV", 30, Color.WHITE, true);
        TextView by = text("BY 3L Donat0", 18, Color.rgb(255, 196, 64), false);
        brandText.addView(brand);
        brandText.addView(by);
        brandBox.addView(brandText);
        top.addView(brandBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        clock = text(timeFormat.format(new Date()), 46, Color.WHITE, true);
        clock.setGravity(Gravity.END);
        top.addView(clock, new LinearLayout.LayoutParams(dp(240), ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(top);

        TextView welcome = text("Bienvenido a DC GAMES TV by El Donat0", 18, Color.rgb(185, 198, 216), false);
        welcome.setPadding(0, dp(2), 0, dp(18));
        root.addView(welcome);

        LinearLayout cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);
        cards.setGravity(Gravity.CENTER);

        addCard(cards, "YOUTUBE", "Videos", () -> launchAny(
                "com.amazon.firetv.youtube", "com.google.android.youtube.tv", "com.google.android.youtube"));
        addCard(cards, "SPOTIFY", "Música", () -> launchAny("com.spotify.tv.android", "com.spotify.music"));
        addCard(cards, "TIKTOK", "Videos cortos", () -> launchAny("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"));
        addCard(cards, "NETFLIX", "Streaming", () -> launchAny("com.netflix.ninja", "com.netflix.mediaclient"));
        addCard(cards, "PRIME VIDEO", "Streaming", () -> launchAny("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"));
        addCard(cards, "IPTV", "TV en vivo", this::openIptv);
        addCard(cards, "APPS", "Todas", this::openAppDrawer);

        root.addView(cards, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));

        TextView hint = text("Usa el control para moverte · OK para abrir", 15, Color.rgb(145, 158, 177), false);
        hint.setPadding(0, dp(16), 0, 0);
        root.addView(hint);

        setContentView(root);
        if (cards.getChildCount() > 0) cards.getChildAt(0).requestFocus();
    }

    private void addCard(LinearLayout parent, String title, String subtitle, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(12), dp(10), dp(12));
        card.setFocusable(true);
        card.setClickable(true);
        card.setBackground(cardBg(false));

        TextView t = text(title, 18, Color.WHITE, true);
        t.setGravity(Gravity.CENTER);
        TextView s = text(subtitle, 13, Color.rgb(178, 190, 207), false);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, dp(5), 0, 0);
        card.addView(t);
        card.addView(s);

        card.setOnFocusChangeListener((v, focused) -> {
            v.animate().scaleX(focused ? 1.08f : 1f).scaleY(focused ? 1.08f : 1f).setDuration(120).start();
            v.setBackground(cardBg(focused));
        });
        card.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(118), 1);
        lp.setMargins(dp(6), dp(8), dp(6), dp(8));
        parent.addView(card, lp);
    }

    private GradientDrawable cardBg(boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(16));
        g.setColor(focused ? Color.rgb(37, 94, 153) : Color.rgb(26, 34, 46));
        g.setStroke(dp(focused ? 3 : 1), focused ? Color.WHITE : Color.rgb(58, 69, 84));
        return g;
    }

    private GradientDrawable makeGradient(int start, int end, float radius) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        g.setCornerRadius(radius);
        return g;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return v;
    }

    private void launchAny(String... packages) {
        PackageManager pm = getPackageManager();
        for (String pkg : packages) {
            Intent i = pm.getLaunchIntentForPackage(pkg);
            if (i != null) {
                startActivity(i);
                return;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("App no instalada")
                .setMessage("No encontré esta app instalada en el Fire TV. Puedes instalarla y después aparecerá aquí automáticamente.")
                .setPositiveButton("Abrir Apps", (d, w) -> openAppDrawer())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void openAppDrawer() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Intent leanback = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(leanback, 0)) {
            if (!r.activityInfo.packageName.equals(getPackageName()) && seen.add(r.activityInfo.packageName)) apps.add(r);
        }
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(launcher, 0)) {
            if (!r.activityInfo.packageName.equals(getPackageName()) && seen.add(r.activityInfo.packageName)) apps.add(r);
        }

        Collections.sort(apps, (a, b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        String[] names = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) names[i] = apps.get(i).loadLabel(pm).toString();

        new AlertDialog.Builder(this)
                .setTitle("Apps instaladas")
                .setItems(names, (dialog, which) -> {
                    Intent launch = pm.getLaunchIntentForPackage(apps.get(which).activityInfo.packageName);
                    if (launch != null) startActivity(launch);
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void openIptv() {
        String saved = prefs.getString("m3u_url", "");
        String[] choices = saved.isEmpty()
                ? new String[]{"Agregar lista M3U", "Abrir app IPTV instalada"}
                : new String[]{"Cargar mi TV", "Cambiar lista M3U", "Abrir app IPTV instalada"};

        new AlertDialog.Builder(this)
                .setTitle("IPTV · DC GAMES TV")
                .setItems(choices, (d, which) -> {
                    if (saved.isEmpty()) {
                        if (which == 0) askM3u(); else launchAny("ar.tvplayer.tv", "com.nst.iptvsmarterstvbox", "com.ottplay.ottplay");
                    } else {
                        if (which == 0) loadM3u(saved);
                        else if (which == 1) askM3u();
                        else launchAny("ar.tvplayer.tv", "com.nst.iptvsmarterstvbox", "com.ottplay.ottplay");
                    }
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void askM3u() {
        final EditText input = new EditText(this);
        input.setHint("https://servidor/lista.m3u");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(prefs.getString("m3u_url", ""));
        int pad = dp(24);
        FrameLayout wrap = new FrameLayout(this);
        wrap.setPadding(pad, 0, pad, 0);
        wrap.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Lista M3U")
                .setMessage("Pega la URL de tu proveedor autorizado. DC GAMES TV guardará esta dirección en el dispositivo.")
                .setView(wrap)
                .setPositiveButton("Guardar y cargar", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        prefs.edit().putString("m3u_url", url).apply();
                        loadM3u(url);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadM3u(String source) {
        Toast.makeText(this, "Cargando canales…", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            List<Channel> channels = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(source);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "DC-GAMES-TV/2.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                String pendingName = null;
                while ((line = br.readLine()) != null && channels.size() < 250) {
                    line = line.trim();
                    if (line.startsWith("#EXTINF")) {
                        int comma = line.lastIndexOf(',');
                        pendingName = comma >= 0 ? line.substring(comma + 1).trim() : "Canal";
                    } else if (!line.isEmpty() && !line.startsWith("#") && pendingName != null) {
                        channels.add(new Channel(pendingName, line));
                        pendingName = null;
                    }
                }
                br.close();
                runOnUiThread(() -> showChannels(channels));
            } catch (Exception e) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("No se pudo cargar IPTV")
                        .setMessage("Revisa la URL M3U y tu conexión a internet.\n\n" + e.getMessage())
                        .setPositiveButton("Aceptar", null).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void showChannels(List<Channel> channels) {
        if (channels.isEmpty()) {
            Toast.makeText(this, "La lista no contiene canales compatibles", Toast.LENGTH_LONG).show();
            return;
        }
        String[] labels = new String[channels.size()];
        for (int i = 0; i < channels.size(); i++) labels[i] = channels.get(i).name;
        new AlertDialog.Builder(this)
                .setTitle("TV en vivo · " + channels.size() + " canales")
                .setItems(labels, (d, which) -> playChannel(channels.get(which)))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void playChannel(Channel channel) {
        VideoView video = new VideoView(this);
        video.setBackgroundColor(Color.BLACK);
        MediaController controls = new MediaController(this);
        controls.setAnchorView(video);
        video.setMediaController(controls);
        video.setVideoURI(Uri.parse(channel.url));
        video.setOnPreparedListener(mp -> {
            mp.setScreenOnWhilePlaying(true);
            video.start();
        });
        video.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Este canal no pudo reproducirse con el motor básico de v2", Toast.LENGTH_LONG).show();
            return true;
        });
        setContentView(video);
        video.requestFocus();
    }

    @Override public void onBackPressed() {
        showHome();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(clockTick);
        executor.shutdownNow();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Channel {
        final String name;
        final String url;
        Channel(String name, String url) { this.name = name; this.url = url; }
    }
}
