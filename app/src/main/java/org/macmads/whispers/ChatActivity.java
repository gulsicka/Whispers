package org.macmads.whispers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatActivity extends AppCompatActivity {

    public ListView messagesView;
    public WebSocketClient client;
    public MessageAdapter messageAdapter;
    public EditText msg_to_send;
    public URI serverUri = null;
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

        try {
            serverUri = new URI("ws://"+intent.getStringExtra("server_ip")+":38301");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        client = new WebsocketClient(serverUri,ChatActivity.this,this);
        client.connect();

    }
    public void sendMessage(View view){
        try {
            client.send(msg_to_send.getText().toString());
        }catch (WebsocketNotConnectedException exception){

            ConnectivityManager connectivityManager = (ConnectivityManager)ChatActivity.this.getSystemService(ChatActivity.this.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    boolean isData = activeNetworkInfo.getType()==ConnectivityManager.TYPE_MOBILE;
                    if (isData){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "please turn of mobile data in order for app to work", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                    else{
                        Intent i = new Intent(this,MainActivity.class);
                        startActivity(i);

                    }
                }
            }


        }


        messageAdapter.add(new Message(msg_to_send.getText().toString(),new MemberData("abdullah","red"),true));
        msg_to_send.setText("");
        messageAdapter.notifyDataSetChanged();

    }
    public void recieveMessage(String message){
        messageAdapter.add(new Message(message,new MemberData("abdullah","red"),false));
        messageAdapter.notifyDataSetChanged();
    }
}
