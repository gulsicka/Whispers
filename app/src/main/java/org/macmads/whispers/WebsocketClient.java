package org.macmads.whispers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebsocketClient extends WebSocketClient {
    private static WebsocketClient insstance = null;
    public Context context;
    public ChatActivity activity;

    private WebsocketClient(URI serverUri) {
        super(serverUri);
    }

    public static void initialize(URI uri,Context context){
        if (insstance == null){
            insstance = new WebsocketClient(uri);
            insstance.connect();
        }

    }

    public static void setContext(Context context){
        insstance.context=context;
    }

    public static void setActivity(ChatActivity activity){
        insstance.activity=activity;
    }

    public static WebsocketClient getInstance(){
        return insstance;
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
//                this.send("from android");
        Intent intent = new Intent(context,ChatActivity.class);
        context.startActivity(intent);

    }

    @Override
    public void onMessage(String message) {
        final String m = message;
        if (activity!=null){
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {//server ka msg yahan recieve hota hay, client doesnt know k yea kahan say aya hay but server does
                    activity.recieveMessage(m);//addss msg to listview
                    activity.messagesView.refreshDrawableState();
                }
            });
        }



    }

    @Override
    public void onClose(int code, final String reason, boolean remote) {
//make it go back to main activity
        if (activity!=null && context!=null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), "connection closed ", Toast.LENGTH_LONG).show();
                }
            });
        }



    }

    @Override
    public void onError(Exception ex) {
//make it go back to main activity
        if (activity!=null &&  context!=null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            });
        }

    }
}
