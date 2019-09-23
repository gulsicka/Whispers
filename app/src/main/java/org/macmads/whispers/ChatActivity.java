package org.macmads.whispers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatActivity extends AppCompatActivity {

    public ListView messagesView;
    public WebSocketClient client;
    public MessageAdapter messageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        messagesView = (ListView) findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(this);
        messagesView.setAdapter(messageAdapter);
        Message message = new Message("message",new MemberData("abdullah","red"),false);
        messageAdapter.add(message);
        messageAdapter.notifyDataSetChanged();
        URI serverUri = null;
        try {
            serverUri = new URI("ws://"+intent.getStringExtra("server_ip")+":38301");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        client = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
//                this.send("from android");
            }

            @Override
            public void onMessage(String message) {
                final String m = message;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        recieveMessage(m);
                        messagesView.refreshDrawableState();
                    }
                });



            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {

            }
        };
        client.connect();

    }
    public void sendMessage(View view){
        client.send("button pressed");
        messageAdapter.add(new Message("message",new MemberData("abdullah","red"),true));
        messageAdapter.notifyDataSetChanged();

    }
    public void recieveMessage(String message){
        messageAdapter.add(new Message(message,new MemberData("abdullah","red"),false));
        messageAdapter.notifyDataSetChanged();
    }
}
