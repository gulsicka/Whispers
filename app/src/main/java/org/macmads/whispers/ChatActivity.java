package org.macmads.whispers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import static android.graphics.Bitmap.createScaledBitmap;

public class ChatActivity extends AppCompatActivity {

    public ListView messagesView;
    public WebSocketClient client;
    public MessageAdapter messageAdapter;
    public EditText msg_to_send;
    public Intent intent;
    public ImageButton sendImage;
    LinearLayout imageLayout;
    FrameLayout frameLayout;
    ImageView imageToSend;
    public Bitmap bitmap = null;
    Bitmap previewImageBM = null;
    int imageHeight, imageWidth;
    public byte[] byteArray = null;
    public Uri imageuri;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        msg_to_send = findViewById(R.id.editText);
        msg_to_send.setText("");
        intent = getIntent();
        imageToSend = (ImageView) findViewById(R.id.imageToBeSent);
        sendImage = findViewById(R.id.imageBtn);


        messagesView = (ListView) findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(this);
        messagesView.setAdapter(messageAdapter);
        // Message message = new Message("message",new MemberData("abdullah","red"),false);
        //messageAdapter.add(message);
        //messageAdapter.notifyDataSetChanged();


        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(getIntent().ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "select picture"), PICK_IMAGE);
            }
        });

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
                System.out.println(message);
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

    public void sendMessage(View view) {

        String imageString = null;
        if (byteArray != null) {
            imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        try {
            InetSocketAddress address = client.getLocalSocketAddress();
            if (address != null){
                client.send(msg_to_send.getText().toString() + "=.=" + address.toString() +
                        "=.=" + imageString);
            }
            else {
                throw new WebsocketNotConnectedException();
            }


        } catch (WebsocketNotConnectedException exception) {

            ConnectivityManager connectivityManager = (ConnectivityManager) ChatActivity.this.getSystemService(ChatActivity.this.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    boolean isData = activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                    if (isData) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "please turn of mobile data in order for app to work", Toast.LENGTH_LONG).show();

                            }
                        });
                    } else {
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);

                    }
                }
            }


        }


        //
        //imageLayout.setVisibility(LinearLayout.GONE);
        // frameLayout.setVisibility(FrameLayout.GONE);


        messageAdapter.add(new Message(msg_to_send.getText().toString(), new MemberData("abdullah", "red"), true, bitmap));
        msg_to_send.setText("");
        messageAdapter.notifyDataSetChanged();

        previewImageBM = bitmap;
        bitmap = null;
        byteArray = null;
        imageString = null;


    }

    public void recieveMessage(String message) {

        String[] parts = message.split("=.=");

        if (!client.getLocalSocketAddress().toString().equals(parts[1])) {
            //Toast.makeText(ChatActivity.this, parts.length, Toast.LENGTH_SHORT).show();

            byteArray = Base64.decode(parts[2], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            messageAdapter.add(new Message(parts[0], new MemberData("abdullah", "red"), false, bitmap));

            previewImageBM = bitmap;
            parts = null;
            bitmap = null;
            byteArray = null;


            messageAdapter.notifyDataSetChanged();
        }
    }

    public void ShowImage(View view) {
        Toast.makeText(ChatActivity.this, "im image", Toast.LENGTH_SHORT).show();
        final Dialog nagDialog = new Dialog(ChatActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setCancelable(false);
        nagDialog.setContentView(R.layout.preview_image);
        Button btnClose = (Button) nagDialog.findViewById(R.id.btnIvClose);
        ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
        previewImageBM = createScaledBitmap(previewImageBM, 500, 500, true);
        ivPreview.setImageBitmap(previewImageBM);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                nagDialog.dismiss();
            }
        });
        nagDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageuri = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                imageHeight = bitmap.getHeight();
                imageWidth = bitmap.getWidth();
                bitmap = createScaledBitmap(bitmap, 200, 200, true);
                //imageToSend.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

