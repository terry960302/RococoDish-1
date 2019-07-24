package com.example.front_ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.front_ui.DataModel.FollowInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowerFragment extends Fragment {

    String TAG = "TAGFollowerFrag";
    RecyclerView followerRecycler;
    FollowRecyAdapter followRecyAdapter;
    List<FollowInfo> followerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follower, container, false);

        followerRecycler = view.findViewById(R.id.followerList_recyclerview_followerFrag);
        followRecyAdapter = new FollowRecyAdapter(getActivity(), followerList);
        followerRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        followerRecycler.setAdapter(followRecyAdapter);

        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String userUUID = getArguments().getString("userUUID");

        //todo : 팔로워 취소했을 때에도 실시간 반영 필요
        //마이페이지에서 왔을 때
        FirebaseFirestore.getInstance()
                .collection("사용자")
                .document(userUUID)
                .collection("팔로워")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e != null)
                            Log.d(TAG, e.getMessage());
                        if(!queryDocumentSnapshots.getDocuments().isEmpty()){

                            followerList.clear();

                            for(DocumentSnapshot dc: queryDocumentSnapshots.getDocuments()){

                                String followerUid = dc.getId();

                                //유저의 프로필 정보 가져오기
                                FirebaseFirestore.getInstance()
                                        .collection("사용자")
                                        .document(followerUid)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                String imagePath = documentSnapshot.get("profileImage").toString();
                                                String name = documentSnapshot.get("nickname").toString();
                                                String email = documentSnapshot.get("eMail").toString();
                                                String uid = documentSnapshot.getId();

                                                followerList.add(new FollowInfo(imagePath, name, email, uid));
                                                followRecyAdapter.notifyItemChanged(followerList.size());
                                            }
                                        });
                            }
                            followRecyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
