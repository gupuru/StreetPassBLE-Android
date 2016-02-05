package gupuru.streetpass.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import gupuru.streetpass.R;
import gupuru.streetpass.adapter.ChatRecyclerAdapter;
import gupuru.streetpass.bean.ChatData;
import gupuru.streetpass.utils.DividerItemDecoration;
import gupuru.streetpassble.DataTransfer;
import gupuru.streetpassble.parcelable.Error;

public class ChatActivity extends AppCompatActivity implements DataTransfer.OnDeviceConnectionListener,
        DataTransfer.OnDeviceCommunicationListener, View.OnClickListener {

    private DataTransfer dataTransfer;
    private String deviceAddress;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private EditText messageEditTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //ツールバー
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (!TextUtils.isEmpty(getIntent().getStringExtra("device"))) {
            deviceAddress = getIntent().getStringExtra("device");
        }

        Button sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
        messageEditTextView = (EditText) findViewById(R.id.edit);

        dataTransfer = new DataTransfer(ChatActivity.this);
        dataTransfer.setOnDeviceConnectionListener(this);
        dataTransfer.setOnDeviceCommunicationListener(this);
        dataTransfer.open();

        //RecyclerView初期化
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //区切り線をつける
        recyclerView.addItemDecoration(new DividerItemDecoration(ChatActivity.this, null));
        ArrayList<ChatData> chatDataArrayList = new ArrayList<>();
        //Adapter初期化
        chatRecyclerAdapter = new ChatRecyclerAdapter(ChatActivity.this, chatDataArrayList);
        //アダプターにセット
        recyclerView.setAdapter(chatRecyclerAdapter);
        //更新を通知
        chatRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataTransfer != null) {
            dataTransfer.disconnectDevice();
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
                    if (message.equals("")){
                        message = "やまっぷ";
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

    /**
     * メッセージ受け取り
     * @param message
     */
    @Override
    public void deviceConnectSendReceiveData(String message) {
        addMessage(message, false);
    }

    /**
     * エラー
     * @param error
     */
    @Override
    public void deviceConnectError(Error error) {
        Log.d("ここ", "deviceConnectError エラー" + error.getErrorMessage());
    }




    @Override
    public void deviceCommunicationReceiveData(String data) {
        Log.d("ここ", "deviceCommunicationReceiveData3" + data);
    }

    @Override
    public void deviceCommunicationSendData(String data) {
        addMessage(data, true);
    }

    @Override
    public void deviceCommunicationError(Error error) {
        Log.d("ここ", "deviceCommunicationError エラー" + error.getErrorMessage());
    }

}
