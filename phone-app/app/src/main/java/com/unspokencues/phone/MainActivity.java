package com.unspokencues.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {
    private static final int FILE_CHOOSER = 1001;
    private static final int CAMERA_PERMISSION = 1002;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingPermission;
    private volatile boolean wearConnected = false;

    public class WearBridge {
        @JavascriptInterface public void sendStatus(String status){ sendToWatch("/uc/status", status); }
        @JavascriptInterface public void sendCue(String state){ sendToWatch("/uc/cue-state", state); }
        @JavascriptInterface public boolean isConnected(){ return wearConnected; }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setLoadWithOverviewMode(true); s.setUseWideViewPort(false); s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false); s.setMediaPlaybackRequiresUserGesture(false); s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.addJavascriptInterface(new WearBridge(), "UnspokenWear");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null); fileCallback = callback;
                try { startActivityForResult(params.createIntent(), FILE_CHOOSER); return true; } catch (Exception e) { fileCallback = null; return false; }
            }
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean wantsCamera=false; for(String resource:request.getResources()) if(PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) wantsCamera=true;
                    if(!wantsCamera){request.deny();return;}
                    if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) request.grant(request.getResources());
                    else { pendingPermission=request; requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION); }
                });
            }
        });
        webView.loadUrl("https://thecdmlegacy-sudo.github.io/UnspokenCues-Demo/app.html?phone=v27");
    }

    private void sendToWatch(String path,String msg){
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes->{
            wearConnected = !nodes.isEmpty();
            for(Node n:nodes) Wearable.getMessageClient(this).sendMessage(n.getId(),path,msg.getBytes(StandardCharsets.UTF_8));
            updateConnection(wearConnected);
        }).addOnFailureListener(e->{ wearConnected=false; updateConnection(false); });
    }
    private void updateConnection(boolean connected){ wearConnected=connected; if(webView!=null) webView.post(()->webView.evaluateJavascript("window.UCWatchConnection&&window.UCWatchConnection("+connected+")",null)); }
    private void js(String code){ if(webView!=null) webView.post(()->webView.evaluateJavascript(code,null)); }
    @Override public void onMessageReceived(MessageEvent e){
        String msg=new String(e.getData(),StandardCharsets.UTF_8); String p=e.getPath();
        if(p.equals("/uc/ping")){sendToWatch("/uc/pong","phone-v27");updateConnection(true);}
        else if(p.equals("/uc/status")) js("window.UCWatchStatus&&window.UCWatchStatus('"+msg.replace("'","\\'")+"')");
        else if(p.equals("/uc/cue")) {
            if("out".equalsIgnoreCase(msg)) js("window.UCWatchCueOut?window.UCWatchCueOut():window.UCWatchCue&&window.UCWatchCue()");
            else js("window.UCWatchCue&&window.UCWatchCue()");
        }
        else if(p.equals("/uc/profile")) js("window.UCWatchOpen&&window.UCWatchOpen('profile')");
        else if(p.equals("/uc/swap")) js("window.UCWatchOpen&&window.UCWatchOpen('swap')");
    }
    @Override protected void onResume(){
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(n->updateConnection(!n.isEmpty())).addOnFailureListener(e->updateConnection(false));
    }
    @Override protected void onPause(){Wearable.getMessageClient(this).removeListener(this);super.onPause();}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==FILE_CHOOSER&&fileCallback!=null){Uri[] result=WebChromeClient.FileChooserParams.parseResult(resultCode,data);fileCallback.onReceiveValue(result);fileCallback=null;}}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==CAMERA_PERMISSION&&pendingPermission!=null){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED) pendingPermission.grant(pendingPermission.getResources());else pendingPermission.deny();pendingPermission=null;}}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
}
