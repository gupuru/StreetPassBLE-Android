package gupuru.streetpass.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import gupuru.streetpass.R;
import gupuru.streetpass.adapter.ChatRecyclerAdapter;
import gupuru.streetpass.bean.ChatData;
import gupuru.streetpass.utils.DividerItemDecoration;
import gupuru.streetpassble.DataTransfer;
import gupuru.streetpassble.parcelable.Error;

public class ChatActivity extends AppCompatActivity implements DataTransfer.OnDataTransferListener,
        DataTransfer.OnConnectedDeviceInitialInfoListener, View.OnClickListener {

    private DataTransfer dataTransfer;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private RecyclerView recyclerView;
    private EditText messageEditTextView;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //ツールバー
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle(getResources().getString(R.string.chat_name));
            toolbar.setTitleTextColor(ContextCompat.getColor(ChatActivity.this, R.color.white));
        }

        sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
        sendBtn.setEnabled(false);
        messageEditTextView = (EditText) findViewById(R.id.edit);

        dataTransfer = new DataTransfer(ChatActivity.this);
        dataTransfer.setOnDataTransferListener(this);
        dataTransfer.setOnConnectedDeviceInitialInfoListener(this);
        dataTransfer.open();

        //RecyclerView初期化
        recyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //区切り線をつける
        recyclerView.addItemDecoration(new DividerItemDecoration(ChatActivity.this, null));
        ArrayList<ChatData> chatDataArrayList = new ArrayList<>();
        //Adapter初期化
        chatRecyclerAdapter = new ChatRecyclerAdapter(chatDataArrayList);
        //アダプターにセット
        recyclerView.setAdapter(chatRecyclerAdapter);
        //更新を通知
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataTransfer != null) {
            dataTransfer.close();
            dataTransfer = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (dataTransfer != null) {
                    String message = messageEditTextView.getText().toString();
                    if (message.equals("")) {
                        message = "からです";
                    }
                    dataTransfer.sendDataToDevice(message);
                }
                break;
            default:
                break;
        }
    }

    private void addMessage(String message, boolean isMe) {
        if (message == null) {
            message = "";
        }
        ChatData chatData = new ChatData(message, isMe);
        chatRecyclerAdapter.add(chatData);
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 初回メッセージ受信
     *
     * @param message
     */
    @Override
    public void connectedDeviceInitialMessage(String message) {
        sendBtn.setEnabled(true);
        showToast(getString(R.string.cant_send_data));
    }

    /**
     * 初回メッセージ受信エラー
     *
     * @param error
     */
    @Override
    public void connectedDeviceError(Error error) {
        showToast(error.getErrorMessage());
    }

    /**
     * 送信メッセージ
     *
     * @param message
     */
    @Override
    public void dataTransferSendMessage(String message) {
        addMessage(message, true);
        recyclerView.smoothScrollToPosition(chatRecyclerAdapter.getDataSize());
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * 受信メッセージ
     *
     * @param message
     */
    @Override
    public void dataTransferReceiveMessage(String message) {
        addMessage(message, false);
        recyclerView.smoothScrollToPosition(chatRecyclerAdapter.getDataSize());
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * データ送信エラー
     *
     * @param error
     */
    @Override
    public void dataTransferError(Error error) {
        showToast(error.getErrorMessage());
    }

}
