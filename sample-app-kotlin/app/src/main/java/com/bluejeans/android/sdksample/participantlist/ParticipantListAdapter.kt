/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.participantlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluejeans.android.sdksample.databinding.LayoutParticipantItemBinding
import com.bluejeans.android.sdksample.participantlist.ParticipantListAdapter.RosterViewHolder
import com.bluejeans.bluejeanssdk.meeting.ParticipantsService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ParticipantListAdapter constructor(
    private val isForChat: Boolean = false,
    private val chatItemListener: ParticipantChatItemListener? = null
) : RecyclerView.Adapter<RosterViewHolder>() {
    private val participantList = ArrayList<ParticipantsService.Participant>()
    private var everyone = ParticipantsService.Participant(EVERYONE)
    private var disposable = CompositeDisposable()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterViewHolder {
        val itemViewBinding = LayoutParticipantItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return RosterViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: RosterViewHolder, position: Int) {
        holder.bind(this.participantList[position])
    }

    override fun getItemCount(): Int {
        return this.participantList.size
    }

    fun updateMeetingList(participantsList: List<ParticipantsService.Participant>) {
        participantList.clear()
        disposable.dispose()
        disposable = CompositeDisposable()
        if (isForChat && !participantsList.contains(everyone)) {
            this.participantList.add(everyone)
        }
        this.participantList.addAll(participantsList)
        if (isForChat) {
            // remove self participant
            participantList.removeAt(1)
        }
        notifyDataSetChanged()
    }

    inner class RosterViewHolder(private val bindingView: LayoutParticipantItemBinding) :
        RecyclerView.ViewHolder(bindingView.root) {
        fun bind(participant: ParticipantsService.Participant) {
            if (participant.id == EVERYONE) {
                bindingView.tvParticipantName.setText(EVERYONE)
            } else {
                bindingView.tvParticipantName.setText(participant.name)
            }
            bindingView.ivRosterAudioStatus.isSelected = participant.isAudioMuted
            bindingView.ivRosterVideoStatus.isSelected = participant.isVideoMuted
            if (isForChat) {
                bindingView.ivRosterAudioStatus.setVisibility(View.GONE)
                bindingView.ivRosterVideoStatus.setVisibility(View.GONE)
                bindingView.ivPrivateChat.setVisibility(View.VISIBLE)
                chatItemListener?.onMessageCountChange(participant, bindingView.tvUnreadCount)
                bindingView.ivPrivateChat.setOnClickListener { v: View ->
                    chatItemListener?.onParticipantClick(
                        participant
                    )
                }
            } else {
                bindingView.ivRosterAudioStatus.setVisibility(View.VISIBLE)
                bindingView.ivRosterVideoStatus.setVisibility(View.VISIBLE)
                bindingView.ivPrivateChat.setVisibility(View.GONE)
            }
        }
    }
}