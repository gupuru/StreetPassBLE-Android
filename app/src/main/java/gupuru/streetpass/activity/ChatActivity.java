package gupuru.streetpass.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
        View.OnClickListener {

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
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(disconnect,
                new IntentFilter("disconnect"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(disconnect);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (dataTransfer != null) {
                    dataTransfer.close();
                }
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
                    messageEditTextView.setText("");
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
        recyclerView.smoothScrollToPosition(chatRecyclerAdapter.getDataSize());
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 送信メッセージ
     *
     * @param message
     */
    @Override
    public void dataTransferSendMessage(String message) {
        if (message != null) {
            addMessage(message, true);
        }
    }

    /**
     * 受信メッセージ
     *
     * @param message
     */
    @Override
    public void dataTransferReceiveMessage(String message) {
        sendBtn.setEnabled(true);
        if (message != null) {
            addMessage(message, false);
        }
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

    private BroadcastReceiver disconnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dataTransfer != null) {
                dataTransfer.disconnectDevice();
            }
            finish();
        }
    };
}
