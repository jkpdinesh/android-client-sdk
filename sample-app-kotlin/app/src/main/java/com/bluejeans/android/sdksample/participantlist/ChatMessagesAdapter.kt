/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.participantlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bjnclientcore.inmeeting.chat.model.ChatMessage
import com.bjnclientcore.ui.util.extensions.visible
import com.bluejeans.android.sdksample.R

class ChatMessagesAdapter(
    private val isChatPublic: Boolean,
    callback: DiffUtil.ItemCallback<ChatMessage>
) : ListAdapter<ChatMessage, ChatMessagesAdapter.ChatMessageViewHolder>(callback) {
    private val chatMessages = ArrayList<ChatMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val inflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val itemLayoutView: View = if (viewType == 0) {
            inflater.inflate(R.layout.layout_chat_item_send, parent, false)
        } else {
            inflater.inflate(R.layout.layout_chat_item_receive, parent, false)
        }
        return ChatMessageViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(chatMessages[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].sentBySelf) {
            0
        } else 1
    }

    fun updateMessages(msgs: List<ChatMessage>) {
        if (msgs.isEmpty()) return
        if (chatMessages.isEmpty()) {
            chatMessages.clear()
            chatMessages.addAll(msgs)
            notifyDataSetChanged()
        } else {
            val oldSize = chatMessages.size
            val diff = msgs.size - oldSize
            chatMessages.addAll(msgs.subList(oldSize, msgs.size))
            notifyItemRangeInserted(oldSize, diff)
        }
    }

    inner class ChatMessageViewHolder(mItemView: View) : RecyclerView.ViewHolder(mItemView) {
        private val participantName: TextView = itemView.findViewById(R.id.tvParticipantName)
        private val participantMsg: TextView = itemView.findViewById(R.id.tvMessage)

        @Synchronized
        fun bind(msg: ChatMessage) {
            synchronized(this) {
                if (isChatPublic) {
                    if (!msg.sentBySelf) {
                        participantName.text = msg.sender.name
                        participantName.visible()
                    }
                }
                showMsg(msg)
            }
        }

        @Synchronized
        private fun showMsg(msg: ChatMessage) {
            participantMsg.text = msg.body
            participantMsg.visible()
        }
    }
}