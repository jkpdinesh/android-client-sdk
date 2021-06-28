package com.bluejeans.android.sdksample.participantlist

import android.widget.TextView
import com.bluejeans.bluejeanssdk.meeting.ParticipantsService

interface ParticipantChatItemListener {
    fun onMessageCountChange(participant: ParticipantsService.Participant, mUnreadCount: TextView)
    fun onParticipantClick(participant: ParticipantsService.Participant)
}