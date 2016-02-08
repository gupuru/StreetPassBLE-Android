package gupuru.streetpass.bean;

public class ChatData {

    private String message;
    private boolean isMe = true;

    public ChatData(String message, boolean isMe) {
        this.message = message;
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setIsMe(boolean isMe) {
        this.isMe = isMe;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
