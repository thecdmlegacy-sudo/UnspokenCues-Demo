package com.unspokencues.wear;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {
    private static final String PREFS = "uc_watch";
    private static final long DEBOUNCE_MS = 450;
    private TextView state, connection, lockState, version;
    private Button green, yellow, red, purple, cue, lock, profile, swap, cueOut;
    private String current = "available";
    private boolean cueIn = false;
    private boolean locked = false;
    private long lastTap = 0L;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable connectionCheck = new Runnable() {
        @Override public void run() { ping(); handler.postDelayed(this, 10000); }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        current = prefs.getString("status", "available");
        cueIn = prefs.getBoolean("cueIn", false);
        locked = prefs.getBoolean("locked", false);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(20, 18, 20, 30);
        root.setBackgroundColor(Color.rgb(8,7,11));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = new TextView(this);
        title.setText("UNSPOKEN CUES");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        root.addView(title, full());

        state = new TextView(this);
        state.setTextSize(25);
        state.setTextColor(Color.WHITE);
        state.setGravity(Gravity.CENTER);
        state.setPadding(4,16,4,16);
        root.addView(state, full());

        connection = new TextView(this);
        connection.setText("CHECKING PHONE…");
        connection.setTextColor(Color.LTGRAY);
        connection.setTextSize(12);
        connection.setGravity(Gravity.CENTER);
        root.addView(connection, full());

        lockState = new TextView(this);
        lockState.setTextSize(12);
        lockState.setGravity(Gravity.CENTER);
        lockState.setPadding(0,4,0,8);
        root.addView(lockState, full());

        green = statusButton("GREEN", 0xFF087F3F, Color.WHITE, "available");
        yellow = statusButton("YELLOW", 0xFFF3C623, Color.BLACK, "maybe");
        red = statusButton("RED", 0xFF9E1D28, Color.WHITE, "no");
        purple = statusButton("PURPLE", 0xFF4A1F72, Color.WHITE, "private");
        root.addView(green, statusParams());
        root.addView(yellow, statusParams());
        root.addView(red, statusParams());
        root.addView(purple, statusParams());

        cue = actionButton("CUE-IN");
        cue.setOnClickListener(v -> { if (!acceptTap() || locked) return; cueIn = !cueIn; save(); render(); vibrateShort(); send("/uc/cue", "toggle"); });
        root.addView(cue, actionParams());

        cueOut = actionButton("INSTANT CUE-OUT");
        cueOut.setOnClickListener(v -> { if (!acceptTap()) return; cueIn = false; save(); render(); vibrateLong(); send("/uc/cue", "out"); });
        root.addView(cueOut, actionParams());

        lock = actionButton("SCREEN LOCK");
        lock.setOnClickListener(v -> { if (!acceptTap()) return; locked = !locked; save(); render(); vibrateShort(); });
        root.addView(lock, actionParams());

        profile = actionButton("PROFILE / QR");
        profile.setOnClickListener(v -> { if (!acceptTap() || locked) return; vibrateShort(); send("/uc/profile", "open"); });
        root.addView(profile, actionParams());

        swap = actionButton("S.W.A.P.");
        swap.setOnClickListener(v -> { if (!acceptTap() || locked) return; vibrateShort(); send("/uc/swap", "open"); });
        root.addView(swap, actionParams());

        version = new TextView(this);
        version.setText("WATCH v2 · 2.0.0");
        version.setTextColor(Color.GRAY);
        version.setTextSize(10);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0,12,0,0);
        root.addView(version, full());

        setContentView(scroll);
        render();
        ping();
    }

    private LinearLayout.LayoutParams full(){ return new LinearLayout.LayoutParams(-1,-2); }
    private LinearLayout.LayoutParams statusParams(){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,72); p.setMargins(0,5,0,5); return p; }
    private LinearLayout.LayoutParams actionParams(){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,64); p.setMargins(0,4,0,4); return p; }

    private Button actionButton(String label){
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        return b;
    }

    private Button statusButton(String label, int color, int textColor, String value){
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(18);
        b.setTextColor(textColor);
        b.setBackgroundColor(color);
        b.setOnClickListener(v -> {
            if (!acceptTap() || locked) return;
            current = value;
            save();
            render();
            vibrateShort();
            send("/uc/status", value);
        });
        return b;
    }

    private boolean acceptTap(){
        long now = SystemClock.elapsedRealtime();
        if (now - lastTap < DEBOUNCE_MS) return false;
        lastTap = now;
        return true;
    }

    private void save(){ prefs.edit().putString("status", current).putBoolean("cueIn", cueIn).putBoolean("locked", locked).apply(); }

    private void render(){
        String t = current.equals("available") ? "GREEN · AVAILABLE" : current.equals("maybe") ? "YELLOW · MAYBE" : current.equals("no") ? "RED · NOT AVAILABLE" : "PURPLE · PRIVATE TIME";
        state.setText(t);
        int bg = current.equals("available") ? 0xFF087F3F : current.equals("maybe") ? 0xFFF3C623 : current.equals("no") ? 0xFF9E1D28 : 0xFF4A1F72;
        int fg = current.equals("maybe") ? Color.BLACK : Color.WHITE;
        state.setBackgroundColor(bg);
        state.setTextColor(fg);

        green.setText(current.equals("available") ? "✓ GREEN" : "GREEN");
        yellow.setText(current.equals("maybe") ? "✓ YELLOW" : "YELLOW");
        red.setText(current.equals("no") ? "✓ RED" : "RED");
        purple.setText(current.equals("private") ? "✓ PURPLE" : "PURPLE");
        cue.setText(cueIn ? "CUE-OUT" : "CUE-IN");
        lock.setText(locked ? "UNLOCK SCREEN" : "SCREEN LOCK");
        lockState.setText(locked ? "🔒 CONTROLS LOCKED · INSTANT CUE-OUT ACTIVE" : "CONTROLS UNLOCKED");
        lockState.setTextColor(locked ? 0xFFFFC107 : Color.LTGRAY);

        green.setEnabled(!locked); yellow.setEnabled(!locked); red.setEnabled(!locked); purple.setEnabled(!locked);
        cue.setEnabled(!locked); profile.setEnabled(!locked); swap.setEnabled(!locked);
        cueOut.setEnabled(true); lock.setEnabled(true);
    }

    private void ping(){
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes -> {
            connection.setText(nodes.isEmpty() ? "PHONE NOT CONNECTED" : "PHONE CONNECTED");
            connection.setTextColor(nodes.isEmpty() ? 0xFFFF8A80 : 0xFF9BE7B2);
            if (!nodes.isEmpty()) send("/uc/ping", "watch-v2");
        }).addOnFailureListener(e -> {
            connection.setText("PHONE NOT CONNECTED");
            connection.setTextColor(0xFFFF8A80);
        });
    }

    private void send(String path, String msg){
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes -> {
            for(Node n:nodes) Wearable.getMessageClient(this).sendMessage(n.getId(), path, msg.getBytes(StandardCharsets.UTF_8));
            connection.setText(nodes.isEmpty() ? "PHONE NOT CONNECTED" : "PHONE CONNECTED");
            connection.setTextColor(nodes.isEmpty() ? 0xFFFF8A80 : 0xFF9BE7B2);
        });
    }

    private void vibrateShort(){ vibrate(90); }
    private void vibrateLong(){ vibrate(260); }
    private void vibrate(long ms){ Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE); if(v!=null&&v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE)); }

    @Override public void onResume(){
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
        handler.removeCallbacks(connectionCheck);
        handler.post(connectionCheck);
    }

    @Override public void onPause(){
        handler.removeCallbacks(connectionCheck);
        Wearable.getMessageClient(this).removeListener(this);
        super.onPause();
    }

    @Override public void onMessageReceived(MessageEvent e){
        String msg = new String(e.getData(), StandardCharsets.UTF_8);
        runOnUiThread(() -> {
            if(e.getPath().equals("/uc/status")) { current = msg; save(); render(); }
            else if(e.getPath().equals("/uc/pong")) { connection.setText("PHONE CONNECTED"); connection.setTextColor(0xFF9BE7B2); }
            else if(e.getPath().equals("/uc/cue-state")) { cueIn = "in".equalsIgnoreCase(msg) || "true".equalsIgnoreCase(msg); save(); render(); }
            else if(e.getPath().equals("/uc/haptic")) { if("mutual".equalsIgnoreCase(msg)) vibrateLong(); else vibrateShort(); }
        });
    }
}
