package org.macmads.whispers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatActivity extends AppCompatActivity {

    public ListView messagesView;
    public WebSocketClient client;
    public MessageAdapter messageAdapter;
    public EditText msg_to_send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        msg_to_send = findViewById(R.id.editText);
        msg_to_send.setText("");
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
                    public void run() {//server ka msg yahan recieve hota hay, client doesnt know k yea kahan say aya hay but server does
                        recieveMessage(m);//addss msg to listview
                        messagesView.refreshDrawableState();
                    }
                });



            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
//make it go back to main activity
            }

            @Override
            public void onError(Exception ex) {
//make it go back to main activity

            }
        };
        client.connect();

    }
    public void sendMessage(View view){

        client.send(msg_to_send.getText().toString());
        messageAdapter.add(new Message(msg_to_send.getText().toString(),new MemberData("abdullah","red"),true));
        msg_to_send.setText("");
        messageAdapter.notifyDataSetChanged();

    }
    public void recieveMessage(String message){
        messageAdapter.add(new Message(message,new MemberData("abdullah","red"),false));
        messageAdapter.notifyDataSetChanged();
    }
}
