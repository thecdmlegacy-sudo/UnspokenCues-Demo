package com.unspokencues.wear;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {
    private TextView state, connection;
    private String current = "available";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(18,18,18,18);
        root.setBackgroundColor(Color.rgb(8,7,11));

        TextView title = new TextView(this); title.setText("UNSPOKEN CUES"); title.setTextColor(Color.WHITE); title.setTextSize(17); title.setGravity(Gravity.CENTER);
        state = new TextView(this); state.setTextSize(26); state.setTextColor(Color.WHITE); state.setGravity(Gravity.CENTER); state.setPadding(0,18,0,18);
        connection = new TextView(this); connection.setText("Checking phone…"); connection.setTextColor(Color.LTGRAY); connection.setGravity(Gravity.CENTER);
        root.addView(title); root.addView(state); root.addView(connection);

        add(root,"GREEN",0xFF126734,"available");
        add(root,"YELLOW",0xFF806400,"maybe");
        add(root,"RED",0xFF761A20,"no");
        add(root,"PURPLE",0xFF5B2788,"private");
        Button cue = new Button(this); cue.setText("CUE-IN / OUT"); cue.setOnClickListener(v->send("/uc/cue","toggle")); root.addView(cue,new LinearLayout.LayoutParams(-1,-2));
        Button profile = new Button(this); profile.setText("PROFILE / QR"); profile.setOnClickListener(v->send("/uc/profile","open")); root.addView(profile,new LinearLayout.LayoutParams(-1,-2));
        Button swap = new Button(this); swap.setText("S.W.A.P."); swap.setOnClickListener(v->send("/uc/swap","open")); root.addView(swap,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root); render(); ping();
    }

    private void add(LinearLayout root,String label,int color,String value){
        Button b=new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setBackgroundColor(color); b.setOnClickListener(v->{current=value;render();send("/uc/status",value);});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,5,0,5); root.addView(b,p);
    }
    private void render(){String t=current.equals("available")?"GREEN · AVAILABLE":current.equals("maybe")?"YELLOW · MAYBE":current.equals("no")?"RED · NOT AVAILABLE":"PURPLE · PRIVATE TIME"; state.setText(t);}
    private void ping(){Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes->{connection.setText(nodes.isEmpty()?"PHONE NOT CONNECTED":"PHONE CONNECTED"); if(!nodes.isEmpty()) send("/uc/ping","watch");});}
    private void send(String path,String msg){Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes->{for(Node n:nodes) Wearable.getMessageClient(this).sendMessage(n.getId(),path,msg.getBytes(StandardCharsets.UTF_8)); connection.setText(nodes.isEmpty()?"PHONE NOT CONNECTED":"PHONE CONNECTED");});}
    @Override public void onResume(){super.onResume();Wearable.getMessageClient(this).addListener(this);ping();}
    @Override public void onPause(){Wearable.getMessageClient(this).removeListener(this);super.onPause();}
    @Override public void onMessageReceived(MessageEvent e){String msg=new String(e.getData(),StandardCharsets.UTF_8); runOnUiThread(()->{if(e.getPath().equals("/uc/status")){current=msg;render();} if(e.getPath().equals("/uc/pong")) connection.setText("PHONE CONNECTED"); if(e.getPath().equals("/uc/haptic")){Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE); if(v!=null&&v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(250,VibrationEffect.DEFAULT_AMPLITUDE));}});}
}
