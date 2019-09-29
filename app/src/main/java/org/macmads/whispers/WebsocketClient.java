package org.macmads.whispers;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebsocketClient extends WebSocketClient {
    public Context context;
    public ChatActivity activity;
    public WebsocketClient(URI serverUri, Context context, ChatActivity activity) {
        super(serverUri);
        this.context = context;
        this.activity = activity;
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
//                this.send("from android");
    }

    @Override
    public void onMessage(String message) {
        final String m = message;

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {//server ka msg yahan recieve hota hay, client doesnt know k yea kahan say aya hay but server does
                activity.recieveMessage(m);//addss msg to listview
                activity.messagesView.refreshDrawableState();
            }
        });



    }

    @Override
    public void onClose(int code, final String reason, boolean remote) {
//make it go back to main activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), "connection closed ", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onError(Exception ex) {
//make it go back to main activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), "connection error", Toast.LENGTH_LONG).show();
            }
        });
    }
}
