/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.roster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bluejeans.android.sdksample.R;
import com.bluejeans.bluejeanssdk.meeting.MeetingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Participant display fragment
 */
public class RosterFragment extends Fragment {
    private static final String TAG = "RosterFragment";

    private RosterAdapter rosterAdapter;
    private RecyclerView rosterListView;
    private ArrayList<MeetingService.Participant> participantsList = new ArrayList();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roster_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rosterListView = view.findViewById(R.id.rvRosterParticipants);
        rosterAdapter = new RosterAdapter(getContext());
        rosterListView.setAdapter(rosterAdapter);
        rosterListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        rosterAdapter.updateMeetingList(participantsList);
        view.findViewById(R.id.closeRoster).setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        rosterAdapter = null;
        super.onDestroyView();
    }

    public void updateMeetingList(List<MeetingService.Participant> rosterList) {
        if (rosterAdapter != null) {
            rosterAdapter.updateMeetingList(rosterList);
        }
        participantsList.clear();
        participantsList.addAll(rosterList);
    }
}
