package org.macmads.whispers;

import android.graphics.Bitmap;

public class Message {
    private String text; // message body
    private MemberData memberData; // data of the user that sent this message
    private Bitmap bitmap;
    private boolean belongsToCurrentUser; // is this message sent by us?

    public Message(String text, MemberData memberData, boolean belongsToCurrentUser, Bitmap bitmap) {
        this.text = text;
        this.memberData = memberData;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.bitmap = bitmap;
    }

    public String getText() {
        return text;
    }

    public MemberData getMemberData() {
        return memberData;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}