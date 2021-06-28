package com.bluejeans.android.sdksample.participantlist;

import android.widget.TextView;

import com.bluejeans.bluejeanssdk.meeting.ParticipantsService;

public interface ParticipantChatItemListener {
    void onMessageCountChange(ParticipantsService.Participant participant, TextView mUnreadCount);

    void onParticipantClick(ParticipantsService.Participant participant);
}
