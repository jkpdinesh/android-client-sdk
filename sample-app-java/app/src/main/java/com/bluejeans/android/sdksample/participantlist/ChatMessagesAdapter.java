/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.participantlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bjnclientcore.inmeeting.chat.model.ChatMessage;
import com.bjnclientcore.inmeeting.chat.model.Sender;
import com.bluejeans.android.sdksample.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Synchronized;

public class ChatMessagesAdapter extends ListAdapter<ChatMessage, ChatMessagesAdapter.ChatMessageViewHolder> {

    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private boolean isChatPublic;

    public ChatMessagesAdapter(boolean isPublicChat, DiffUtil.ItemCallback<ChatMessage> callback) {
        super(callback);
        isChatPublic = isPublicChat;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemLayoutView;
        if (viewType == 0) {
            itemLayoutView = inflater.inflate(R.layout.layout_chat_item_send, parent, false);
        } else {
            itemLayoutView = inflater.inflate(R.layout.layout_chat_item_receive, parent, false);
        }
        return new ChatMessageViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        holder.bind(chatMessages.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSentBySelf()) {
            return 0;
        } else return 1;
    }

    public void updateMessages(List<ChatMessage> msgs) {
        if (msgs.isEmpty()) return;
        if (chatMessages.isEmpty()) {
            chatMessages.clear();
            chatMessages.addAll(msgs);
            notifyDataSetChanged();
        } else {
            int oldSize = chatMessages.size();
            int diff = msgs.size() - oldSize;
            chatMessages.addAll(msgs.subList(oldSize, msgs.size()));
            notifyItemRangeInserted(oldSize, diff);
        }
    }

    class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        View mItemView;
        TextView participantName, participantMsg;

        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            participantName = itemView.findViewById(R.id.tvParticipantName);
            participantMsg = itemView.findViewById(R.id.tvMessage);
        }

        @Synchronized
        public void bind(ChatMessage msg) {
            synchronized (this) {
                if (isChatPublic) {
                    if (!msg.getSentBySelf()) {
                        participantName.setText(msg.getSender().getName());
                        participantName.setVisibility(View.VISIBLE);
                    }
                }
                showMsg(msg);
            }
        }

        @Synchronized
        private void showMsg(ChatMessage msg) {
            participantMsg.setText(msg.getBody());
            participantMsg.setVisibility(View.VISIBLE);
        }
    }

}
