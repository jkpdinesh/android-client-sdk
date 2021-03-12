/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.roster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bluejeans.android.sdksample.R;
import com.bluejeans.bluejeanssdk.meeting.MeetingService;

import java.util.ArrayList;
import java.util.List;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.RosterViewHolder> {
    private ArrayList<MeetingService.Participant> rosterList = new ArrayList();
    private Context mContext;

    public RosterAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemLayoutView = inflater.inflate(R.layout.layout_roster_item, parent, false);
        return new RosterViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull RosterViewHolder holder, int position) {
        holder.bind(rosterList.get(position));
    }

    @Override
    public int getItemCount() {
        return rosterList.size();
    }

    public void updateMeetingList(List<MeetingService.Participant> rosterList) {
        this.rosterList.clear();
        this.rosterList.addAll(rosterList);
        notifyDataSetChanged();
    }

    class RosterViewHolder extends RecyclerView.ViewHolder {
        View mItemView;
        TextView mParticipantName;
        ImageView mAudioState;
        ImageView mVideoState;

        public RosterViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            mParticipantName = itemView.findViewById(R.id.tvParticipantName);
            mAudioState = itemView.findViewById(R.id.ivRosterAudioStatus);
            mVideoState = itemView.findViewById(R.id.ivRosterVideoStatus);
        }

        public void bind(MeetingService.Participant participant) {
            mParticipantName.setText(participant.getName());
            mAudioState.setSelected(participant.isAudioMuted());
            mVideoState.setSelected(participant.isVideoMuted());
        }
    }
}
