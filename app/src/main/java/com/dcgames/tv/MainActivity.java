package com.dcgames.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
    private final Handler handler = new Handler();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
    private SharedPreferences prefs;
    private TextView clock, heroTitle, heroSub, heroDesc, urlView;
    private StringBuilder urlBuffer;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(timeFormat.format(new Date()).toUpperCase(Locale.US));
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs = getSharedPreferences("dc_games_tv", MODE_PRIVATE);
        showHome();
        handler.post(clockTick);
    }

    private void showHome() {
        LinearLayout root = column();
        root.setPadding(dp(44), dp(18), dp(44), dp(24));
        root.setBackground(gradient(0xff06101c, 0xff0c2e4d, 0));

        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(makeNativeLogo(), new LinearLayout.LayoutParams(dp(300), dp(128)));
        View spacer = new View(this);
        top.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
        clock = text(timeFormat.format(new Date()).toUpperCase(Locale.US), 48, Color.WHITE, true);
        clock.setGravity(Gravity.END);
        top.addView(clock, new LinearLayout.LayoutParams(dp(260), ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(top);

        LinearLayout hero = row();
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(26), dp(18), dp(26), dp(18));
        hero.setBackground(gradient(0xff163d64, 0xff08131f, dp(24)));
        LinearLayout words = column();
        heroTitle = text("YOUTUBE", 52, Color.WHITE, true);
        heroSub = text("VIDEOS", 20, 0xff5ec4ff, true);
        heroDesc = text("Tus videos y canales favoritos en pantalla grande.", 18, 0xffd4dfeb, false);
        heroDesc.setMaxLines(2);
        heroDesc.setPadding(0, dp(8), 0, 0);
        words.addView(heroTitle); words.addView(heroSub); words.addView(heroDesc);
        hero.addView(words, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(hero, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(176)));

        TextView welcome = text("Bienvenido a DC GAMES TV by El Donat0", 16, 0xffa9bfd2, false);
        welcome.setPadding(dp(4), dp(10), 0, dp(2));
        root.addView(welcome);

        LinearLayout cards = row();
        cards.setGravity(Gravity.CENTER_VERTICAL);
        addCard(cards, "YOUTUBE", "Videos", "Tus videos y canales favoritos en pantalla grande.", () -> launchApp("YouTube", "com.amazon.firetv.youtube", "com.google.android.youtube.tv", "com.google.android.youtube"));
        addCard(cards, "SPOTIFY", "Música", "Música, playlists y podcasts.", () -> launchApp("Spotify", "com.spotify.tv.android", "com.spotify.music"));
        addCard(cards, "TIKTOK", "Videos cortos", "TikTok en pantalla grande.", () -> launchApp("TikTok", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill"));
        addCard(cards, "NETFLIX", "Streaming", "Películas, series y documentales.", () -> launchApp("Netflix", "com.netflix.ninja", "com.netflix.mediaclient"));
        addCard(cards, "PRIME VIDEO", "Streaming", "Películas y series de Prime Video.", () -> launchApp("Prime Video", "com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"));
        addCard(cards, "IPTV", "TV en vivo", "Agrega tu URL M3U con el teclado propio de DC GAMES TV.", this::showIptvSetup);
        addCard(cards, "APPS", "Todas", "Abre las demás aplicaciones instaladas.", this::openAppDrawer);
        root.addView(cards, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(142)));

        TextView hint = text("Muévete con el control · OK para abrir", 14, 0xff90a7bb, false);
        hint.setPadding(dp(4), dp(6), 0, 0);
        root.addView(hint);

        setContentView(root);
        if (cards.getChildCount() > 0) cards.getChildAt(0).requestFocus();
    }

    private View makeNativeLogo() {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(14), dp(8), dp(14), dp(8));
        box.setBackground(gradient(0xff151b2b, 0xff48200b, dp(18)));
        LinearLayout line = row();
        TextView dc = text("DC", 44, 0xff54c8ff, true);
        TextView games = text(" GAMES", 34, 0xffffb72f, true);
        line.addView(dc); line.addView(games);
        TextView by = text("by El Donat0", 17, 0xfffff0cf, true);
        box.addView(line); box.addView(by);
        return box;
    }

    private void addCard(LinearLayout parent, String title, String sub, String desc, Runnable action) {
        LinearLayout card = column();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(10), dp(8), dp(10));
        card.setFocusable(true); card.setClickable(true); card.setBackground(cardBg(false));
        TextView t = text(title, 17, Color.WHITE, true); t.setGravity(Gravity.CENTER);
        TextView s = text(sub, 12, 0xffb8c9da, false); s.setGravity(Gravity.CENTER); s.setPadding(0, dp(4), 0, 0);
        card.addView(t); card.addView(s);
        card.setOnFocusChangeListener((v, f) -> {
            v.animate().scaleX(f ? 1.08f : 1f).scaleY(f ? 1.08f : 1f).setDuration(120).start();
            v.setBackground(cardBg(f));
            if (f) { heroTitle.setText(title); heroSub.setText(sub.toUpperCase(Locale.US)); heroDesc.setText(desc); }
        });
        card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(108), 1);
        lp.setMargins(dp(5), dp(8), dp(5), dp(8));
        parent.addView(card, lp);
    }

    private void launchApp(String label, String... packages) {
        PackageManager pm = getPackageManager();
        for (String p : packages) {
            Intent i = pm.getLaunchIntentForPackage(p);
            if (i != null) { startActivity(i); return; }
        }
        Intent found = findInstalledByLabel(label);
        if (found != null) { startActivity(found); return; }
        new AlertDialog.Builder(this).setTitle("App no instalada")
                .setMessage("No encontré " + label + " entre las apps instaladas en este Fire TV.")
                .setPositiveButton("Ver Apps", (d,w) -> openAppDrawer()).setNegativeButton("Cerrar", null).show();
    }

    private Intent findInstalledByLabel(String wanted) {
        PackageManager pm = getPackageManager();
        String target = wanted.toLowerCase(Locale.US);
        Intent[] intents = {
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        };
        for (Intent base : intents) {
            for (ResolveInfo r : pm.queryIntentActivities(base, 0)) {
                String name = r.loadLabel(pm).toString().toLowerCase(Locale.US);
                if (name.contains(target) || (target.contains("prime") && name.contains("prime"))) {
                    Intent out = new Intent(Intent.ACTION_MAIN);
                    out.addCategory(base.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) ? Intent.CATEGORY_LEANBACK_LAUNCHER : Intent.CATEGORY_LAUNCHER);
                    out.setClassName(r.activityInfo.packageName, r.activityInfo.name);
                    out.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return out;
                }
            }
        }
        return null;
    }

    private void openAppDrawer() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = new ArrayList<>(); Set<String> seen = new HashSet<>();
        Intent[] intents = {
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        };
        for (Intent base : intents) for (ResolveInfo r : pm.queryIntentActivities(base, 0)) {
            String p = r.activityInfo.packageName;
            if (!p.equals(getPackageName()) && seen.add(p)) apps.add(r);
        }
        Collections.sort(apps, (a,b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        String[] labels = new String[apps.size()];
        for (int i=0;i<apps.size();i++) labels[i] = apps.get(i).loadLabel(pm).toString();
        new AlertDialog.Builder(this).setTitle("Apps instaladas").setItems(labels, (d,w) -> {
            ResolveInfo r = apps.get(w);
            Intent i = pm.getLaunchIntentForPackage(r.activityInfo.packageName);
            if (i == null) { i = new Intent(Intent.ACTION_MAIN); i.setClassName(r.activityInfo.packageName, r.activityInfo.name); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); }
            startActivity(i);
        }).setNegativeButton("Cerrar", null).show();
    }

    private void showIptvSetup() {
        LinearLayout page = column();
        page.setPadding(dp(50), dp(30), dp(50), dp(30));
        page.setBackground(gradient(0xff06101c, 0xff0d3558, 0));
        page.addView(text("IPTV", 48, Color.WHITE, true));
        page.addView(text("Escribe tu URL M3U con el control remoto", 20, 0xff63c9ff, true));
        TextView help = text("Este teclado es interno: ya no depende del teclado de Amazon Fire TV.", 15, 0xffc7d5e3, false);
        help.setPadding(0, dp(4), 0, dp(12)); page.addView(help);

        urlBuffer = new StringBuilder(prefs.getString("m3u_url", ""));
        urlView = text(urlBuffer.length() == 0 ? "https://" : urlBuffer.toString(), 18, Color.WHITE, false);
        if (urlBuffer.length() == 0) urlBuffer.append("https://");
        urlView.setPadding(dp(14), dp(12), dp(14), dp(12));
        urlView.setBackground(solid(0xff14273a, dp(12), 0xff63c9ff, 2));
        ScrollView urlScroll = new ScrollView(this); urlScroll.addView(urlView);
        page.addView(urlScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        String[] rows = {"1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm.-_", ":/?&=%+@"};
        for (String chars : rows) {
            LinearLayout r = row(); r.setGravity(Gravity.CENTER);
            for (char c : chars.toCharArray()) addKey(r, String.valueOf(c), () -> appendKey(String.valueOf(c)));
            page.addView(r, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        }
        LinearLayout quick = row(); quick.setGravity(Gravity.CENTER);
        addKey(quick, "https://", () -> appendKey("https://"));
        addKey(quick, ".com/", () -> appendKey(".com/"));
        addKey(quick, "BORRAR", () -> backspace());
        addKey(quick, "LIMPIAR", () -> { urlBuffer.setLength(0); refreshUrl(); });
        page.addView(quick, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        LinearLayout actions = row(); actions.setGravity(Gravity.CENTER);
        addAction(actions, "GUARDAR Y CARGAR", () -> saveAndLoad());
        addAction(actions, "VOLVER", this::showHome);
        page.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));
        setContentView(page);
        if (page.getChildCount() > 4) page.getChildAt(4).requestFocus();
    }

    private void addKey(LinearLayout row, String label, Runnable action) {
        Button b = button(label, 15); b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1); lp.setMargins(dp(3), dp(3), dp(3), dp(3)); row.addView(b, lp);
    }
    private void addAction(LinearLayout row, String label, Runnable action) {
        Button b = button(label, 16); b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(50), 1); lp.setMargins(dp(8), dp(6), dp(8), dp(6)); row.addView(b, lp);
    }
    private Button button(String label, int sp) {
        Button b = new Button(this); b.setText(label); b.setTextSize(sp); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setFocusable(true);
        b.setBackground(solid(0xff1c527d, dp(10), 0xff6fd0ff, 1)); return b;
    }
    private void appendKey(String s) { urlBuffer.append(s); refreshUrl(); }
    private void backspace() { if (urlBuffer.length() > 0) urlBuffer.deleteCharAt(urlBuffer.length()-1); refreshUrl(); }
    private void refreshUrl() { urlView.setText(urlBuffer.length()==0 ? "Escribe una URL" : urlBuffer.toString()); }
    private void saveAndLoad() {
        String u = urlBuffer.toString().trim();
        if (!(u.startsWith("http://") || u.startsWith("https://"))) { Toast.makeText(this, "La URL debe iniciar con http:// o https://", Toast.LENGTH_LONG).show(); return; }
        prefs.edit().putString("m3u_url", u).apply(); loadM3u(u);
    }

    private void loadM3u(String source) {
        Toast.makeText(this, "Cargando canales…", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            List<Channel> channels = new ArrayList<>(); HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(source).openConnection();
                c.setConnectTimeout(12000); c.setReadTimeout(15000); c.setRequestProperty("User-Agent", "DC-GAMES-TV/4.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String line, pending = null;
                while ((line = br.readLine()) != null && channels.size() < 500) {
                    line = line.trim();
                    if (line.startsWith("#EXTINF")) { int comma = line.lastIndexOf(','); pending = comma >= 0 ? line.substring(comma+1).trim() : "Canal"; }
                    else if (!line.isEmpty() && !line.startsWith("#") && pending != null) { channels.add(new Channel(pending, line)); pending = null; }
                }
                br.close(); runOnUiThread(() -> showChannels(channels));
            } catch (Exception e) {
                runOnUiThread(() -> new AlertDialog.Builder(this).setTitle("No se pudo cargar IPTV").setMessage("Revisa la URL y la conexión.\n\n" + e.getMessage()).setPositiveButton("Aceptar", null).show());
            } finally { if (c != null) c.disconnect(); }
        });
    }

    private void showChannels(List<Channel> channels) {
        if (channels.isEmpty()) { Toast.makeText(this, "La lista no contiene canales compatibles", Toast.LENGTH_LONG).show(); return; }
        String[] names = new String[channels.size()]; for (int i=0;i<channels.size();i++) names[i]=channels.get(i).name;
        new AlertDialog.Builder(this).setTitle("TV en vivo · " + channels.size() + " canales").setItems(names, (d,w) -> play(channels.get(w))).setNegativeButton("Cerrar", null).show();
    }
    private void play(Channel ch) {
        VideoView v = new VideoView(this); v.setBackgroundColor(Color.BLACK); MediaController mc = new MediaController(this); mc.setAnchorView(v); v.setMediaController(mc); v.setVideoURI(Uri.parse(ch.url));
        v.setOnPreparedListener(mp -> v.start()); v.setOnErrorListener((mp,a,b) -> { Toast.makeText(this, "Este canal no pudo reproducirse", Toast.LENGTH_LONG).show(); return true; }); setContentView(v); v.requestFocus();
    }

    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private TextView text(String s, int sp, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT_BOLD); return t; }
    private GradientDrawable gradient(int a, int b, float radius) { GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a,b}); g.setCornerRadius(radius); return g; }
    private GradientDrawable solid(int fill, float radius, int stroke, int sw) { GradientDrawable g = new GradientDrawable(); g.setColor(fill); g.setCornerRadius(radius); g.setStroke(dp(sw), stroke); return g; }
    private GradientDrawable cardBg(boolean f) { return solid(f ? 0xff1479bd : 0xff17283a, dp(18), f ? Color.WHITE : 0xff506b83, f ? 3 : 1); }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override public void onBackPressed() { showHome(); }
    @Override protected void onDestroy() { handler.removeCallbacks(clockTick); executor.shutdownNow(); super.onDestroy(); }

    private static class Channel { final String name,url; Channel(String n,String u){name=n;url=u;} }
}
