package com.dcgames.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
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
    private TextView heroTitle;
    private TextView heroSubtitle;
    private TextView heroDescription;
    private ImageView heroIcon;
    private final Handler handler = new Handler();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
    private SharedPreferences prefs;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(timeFormat.format(new Date()).toUpperCase(Locale.US));
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
    }

    private void showHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(48), dp(20), dp(48), dp(28));
        root.setBackground(makeGradient(Color.rgb(4, 10, 18), Color.rgb(10, 36, 64), 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        try {
            byte[] decoded = Base64.decode(LogoData.JPG_BASE64, Base64.DEFAULT);
            logo.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        } catch (Exception ignored) {}
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        top.addView(logo, new LinearLayout.LayoutParams(dp(250), dp(128)));

        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        TextView brand = text("DC GAMES TV", 26, Color.WHITE, true);
        TextView by = text("BY 3L Donat0", 16, Color.rgb(255, 194, 64), true);
        brandText.addView(brand);
        brandText.addView(by);
        top.addView(brandText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        clock = text(timeFormat.format(new Date()).toUpperCase(Locale.US), 48, Color.WHITE, true);
        clock.setGravity(Gravity.END);
        top.addView(clock, new LinearLayout.LayoutParams(dp(270), ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(top);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.HORIZONTAL);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(24), dp(14), dp(24), dp(14));
        hero.setBackground(makeGradient(Color.rgb(19, 44, 72), Color.rgb(10, 22, 38), dp(24)));

        LinearLayout heroWords = new LinearLayout(this);
        heroWords.setOrientation(LinearLayout.VERTICAL);
        heroTitle = text("YOUTUBE", 48, Color.WHITE, true);
        heroSubtitle = text("VIDEOS", 19, Color.rgb(102, 196, 255), true);
        heroDescription = text("Tus videos y canales favoritos desde una pantalla limpia y rápida.", 17, Color.rgb(210, 220, 232), false);
        heroDescription.setMaxLines(2);
        heroDescription.setPadding(0, dp(8), dp(18), 0);
        heroWords.addView(heroTitle);
        heroWords.addView(heroSubtitle);
        heroWords.addView(heroDescription);
        hero.addView(heroWords, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        heroIcon = new ImageView(this);
        heroIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        hero.addView(heroIcon, new LinearLayout.LayoutParams(dp(150), dp(110)));
        root.addView(hero, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(170)));

        TextView welcome = text("Bienvenido a DC GAMES TV by El Donat0", 16, Color.rgb(164, 184, 205), false);
        welcome.setPadding(dp(4), dp(12), 0, dp(4));
        root.addView(welcome);

        LinearLayout cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);
        cards.setGravity(Gravity.CENTER_VERTICAL);

        addCard(cards, "YOUTUBE", "Videos",
                "Tus videos y canales favoritos desde una pantalla limpia y rápida.",
                new String[]{"com.amazon.firetv.youtube", "com.google.android.youtube.tv", "com.google.android.youtube"},
                () -> launchAny("com.amazon.firetv.youtube", "com.google.android.youtube.tv", "com.google.android.youtube"));
        addCard(cards, "SPOTIFY", "Música",
                "Música, playlists y podcasts en tu TV.",
                new String[]{"com.spotify.tv.android", "com.spotify.music"},
                () -> launchAny("com.spotify.tv.android", "com.spotify.music"));
        addCard(cards, "TIKTOK", "Videos cortos",
                "TikTok en pantalla grande cuando la app esté instalada.",
                new String[]{"com.zhiliaoapp.musically", "com.ss.android.ugc.trill"},
                () -> launchAny("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"));
        addCard(cards, "NETFLIX", "Streaming",
                "Películas, series y documentales de Netflix.",
                new String[]{"com.netflix.ninja", "com.netflix.mediaclient"},
                () -> launchAny("com.netflix.ninja", "com.netflix.mediaclient"));
        addCard(cards, "PRIME VIDEO", "Streaming",
                "Películas y series de Prime Video.",
                new String[]{"com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"},
                () -> launchAny("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"));
        addCard(cards, "IPTV", "TV en vivo",
                "Tu IPTV dentro de DC GAMES TV. Agrega tu URL M3U y carga tus canales.",
                null, this::showIptvSetup);
        addCard(cards, "APPS", "Todas",
                "Abre el resto de las aplicaciones instaladas sin llenar el inicio.",
                null, this::openAppDrawer);

        root.addView(cards, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(138)));

        TextView hint = text("Muévete con el control · OK para abrir", 14, Color.rgb(142, 162, 184), false);
        hint.setPadding(dp(4), dp(8), 0, 0);
        root.addView(hint);

        setContentView(root);
        if (cards.getChildCount() > 0) cards.getChildAt(0).requestFocus();
    }

    private void addCard(LinearLayout parent, String title, String subtitle, String description,
                         String[] packages, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(10), dp(8), dp(10));
        card.setFocusable(true);
        card.setClickable(true);
        card.setBackground(cardBg(false));

        TextView t = text(title, 17, Color.WHITE, true);
        t.setGravity(Gravity.CENTER);
        TextView s = text(subtitle, 12, Color.rgb(175, 191, 210), false);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, dp(4), 0, 0);
        card.addView(t);
        card.addView(s);

        card.setOnFocusChangeListener((v, focused) -> {
            v.animate().scaleX(focused ? 1.08f : 1f).scaleY(focused ? 1.08f : 1f).setDuration(120).start();
            v.setBackground(cardBg(focused));
            if (focused) updateHero(title, subtitle, description, packages);
        });
        card.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(108), 1);
        lp.setMargins(dp(6), dp(8), dp(6), dp(8));
        parent.addView(card, lp);
    }

    private void updateHero(String title, String subtitle, String description, String[] packages) {
        heroTitle.setText(title);
        heroSubtitle.setText(subtitle.toUpperCase(Locale.US));
        heroDescription.setText(description);
        heroIcon.setImageDrawable(null);
        if (packages != null) {
            PackageManager pm = getPackageManager();
            for (String pkg : packages) {
                try {
                    Drawable icon = pm.getApplicationIcon(pkg);
                    heroIcon.setImageDrawable(icon);
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    private GradientDrawable cardBg(boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(18));
        g.setColor(focused ? Color.rgb(18, 116, 194) : Color.rgb(23, 37, 54));
        g.setStroke(dp(focused ? 3 : 1), focused ? Color.WHITE : Color.rgb(74, 96, 120));
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
                .setMessage("Esta app no está instalada en el Fire TV. Instálala y DC GAMES TV la detectará automáticamente.")
                .setPositiveButton("Ver Apps", (d, w) -> openAppDrawer())
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

    private void showIptvSetup() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(70), dp(46), dp(70), dp(46));
        page.setBackground(makeGradient(Color.rgb(5, 12, 21), Color.rgb(11, 42, 70), 0));

        TextView title = text("IPTV", 50, Color.WHITE, true);
        TextView sub = text("Configura tu lista M3U", 22, Color.rgb(93, 196, 255), true);
        TextView help = text("Selecciona el campo, presiona OK y escribe la URL con el teclado del Fire TV.", 17, Color.rgb(205, 216, 230), false);
        help.setPadding(0, dp(8), 0, dp(24));
        page.addView(title);
        page.addView(sub);
        page.addView(help);

        EditText urlInput = new EditText(this);
        urlInput.setText(prefs.getString("m3u_url", ""));
        urlInput.setHint("https://servidor.com/lista.m3u");
        urlInput.setSingleLine(true);
        urlInput.setTextSize(19);
        urlInput.setTextColor(Color.WHITE);
        urlInput.setHintTextColor(Color.rgb(130, 150, 170));
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setFocusable(true);
        urlInput.setFocusableInTouchMode(true);
        urlInput.setPadding(dp(18), dp(14), dp(18), dp(14));
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.rgb(19, 35, 53));
        inputBg.setCornerRadius(dp(14));
        inputBg.setStroke(dp(2), Color.rgb(93, 196, 255));
        urlInput.setBackground(inputBg);
        page.addView(urlInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(22), 0, 0);

        Button load = actionButton("GUARDAR Y CARGAR");
        Button keyboard = actionButton("ABRIR TECLADO");
        Button back = actionButton("VOLVER");
        buttons.addView(load, buttonLp());
        buttons.addView(keyboard, buttonLp());
        buttons.addView(back, buttonLp());
        page.addView(buttons);

        TextView note = text("DC GAMES TV guarda esta URL solamente en este dispositivo.", 14, Color.rgb(145, 162, 181), false);
        note.setPadding(0, dp(20), 0, 0);
        page.addView(note);
        setContentView(page);

        Runnable openKeyboard = () -> {
            urlInput.requestFocus();
            urlInput.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(urlInput, InputMethodManager.SHOW_IMPLICIT);
            }, 180);
        };
        urlInput.setOnClickListener(v -> openKeyboard.run());
        urlInput.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                    && event.getAction() == KeyEvent.ACTION_UP) {
                openKeyboard.run();
                return true;
            }
            return false;
        });
        keyboard.setOnClickListener(v -> openKeyboard.run());
        load.setOnClickListener(v -> {
            String value = urlInput.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "Escribe una URL M3U", Toast.LENGTH_LONG).show();
                openKeyboard.run();
                return;
            }
            prefs.edit().putString("m3u_url", value).apply();
            loadM3u(value);
        });
        back.setOnClickListener(v -> showHome());
        urlInput.requestFocus();
    }

    private Button actionButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setFocusable(true);
        return b;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(58), 1);
        lp.setMargins(dp(5), 0, dp(5), 0);
        return lp;
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
                connection.setRequestProperty("User-Agent", "DC-GAMES-TV/3.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                String pendingName = null;
                while ((line = br.readLine()) != null && channels.size() < 500) {
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
            Toast.makeText(this, "Este canal necesita un motor IPTV más avanzado", Toast.LENGTH_LONG).show();
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
