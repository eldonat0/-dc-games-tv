package com.dcgames.tv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 45, 60, 45);
        root.setBackgroundColor(Color.rgb(14,18,24));

        TextView title = new TextView(this);
        title.setText("DC GAMES TV\nBY 3L Donat0");
        title.setTextColor(Color.WHITE);
        title.setTextSize(34);
        title.setGravity(Gravity.START);
        root.addView(title, new LinearLayout.LayoutParams(-1, 0, 1));

        String[] items = {"PELÍCULAS", "SERIES", "TV EN VIVO", "JUEGOS", "APPS", "AJUSTES"};
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (String item : items) {
            Button b = new Button(this);
            b.setText(item);
            b.setTextSize(16);
            b.setFocusable(true);
            b.setAllCaps(false);
            b.setOnFocusChangeListener((v, hasFocus) -> v.setScaleX(hasFocus ? 1.08f : 1f));
            b.setOnClickListener(v -> {});
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 120, 1);
            p.setMargins(8,8,8,8);
            row.addView(b,p);
        }
        root.addView(row, new LinearLayout.LayoutParams(-1, 140));

        TextView foot = new TextView(this);
        foot.setText("DC GAMES TV v1 · Fire TV");
        foot.setTextColor(Color.LTGRAY);
        foot.setTextSize(16);
        root.addView(foot);
        setContentView(root);
    }
}
