package org.macmads.whispers;

import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Abdullah Cheema
 */
public class WebsocketServer extends WebSocketServer {

    public WebsocketServer(InetSocketAddress address) {
        super(address);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
        // TODO Auto-generated method stub
        System.out.println("connection closed");

    }

    @Override
    public void onError(WebSocket arg0, Exception arg1) {
        // TODO Auto-generated method stub
        System.out.println(arg1.getStackTrace());

    }

    @Override
    public void onMessage(WebSocket arg0, String arg1) {
        // TODO Auto-generated method stub
        this.broadcast(arg1);//jo msg aata hay wo broadcast krta hay
        System.out.println("message recieved");

    }

    @Override
    public void onOpen(WebSocket arg0, ClientHandshake arg1) {
        // TODO Auto-generated method stub

        System.out.println("new connection to " + arg0.getRemoteSocketAddress());
//        this.broadcast("broadcast");
//        this.broadcast("broadcast2");

    }

    @Override
    public void onStart() {
        System.out.println("server started");
    }


}
