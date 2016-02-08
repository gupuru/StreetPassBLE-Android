package gupuru.streetpass.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gupuru.streetpass.R;
import gupuru.streetpass.bean.ChatData;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private ArrayList<ChatData> chatDataArrayList;

    public ChatRecyclerAdapter(ArrayList<ChatData> chatDataArrayList) {
        this.chatDataArrayList = chatDataArrayList;
    }

    public void add(ChatData data) {
        chatDataArrayList.add(data);
    }

    public void clear() {
        chatDataArrayList.clear();
    }

    public int getDataSize() {
        if (chatDataArrayList != null && !chatDataArrayList.isEmpty()) {
            return chatDataArrayList.size();
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (chatDataArrayList == null || chatDataArrayList.isEmpty()) {
            return 0;
        }
        return chatDataArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatDataArrayList.get(position).isMe()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v;
        switch (i) {
            case 0:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_view_me_chat, viewGroup, false);
                break;
            case 1:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_view_other_chat, viewGroup, false);
                break;
            default:

                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_view_me_chat, viewGroup, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        if (!chatDataArrayList.isEmpty()) {
            switch (viewHolder.getItemViewType()) {
                case 0:
                    viewHolder.meMessageTextView.setText(chatDataArrayList.get(position).getMessage());
                    break;
                case 1:
                    viewHolder.otherMessageTextView.setText(chatDataArrayList.get(position).getMessage());
                    break;
                default:
                    break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView meMessageTextView;
        private TextView otherMessageTextView;

        public ViewHolder(View v) {
            super(v);
            meMessageTextView = (TextView) v.findViewById(R.id.me_message);
            otherMessageTextView = (TextView) v.findViewById(R.id.other_message);
        }
    }

}
