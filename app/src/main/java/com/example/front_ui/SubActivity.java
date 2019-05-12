package com.example.front_ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.front_ui.DataModel.PostingInfo;
import com.example.front_ui.DataModel.StoreInfo;
import com.example.front_ui.PostingProcess.MainShareActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SubActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 10002;
    private static final String TAG = "TAGSubActivity";
    private FusedLocationProviderClient mFusedLocationClient;
    RecyclerView my_recycler_view;
    RecyclerView myPage_recyclerview;
    NestedScrollView nestedScrollView;
    private Location mCurrentLocation;
    private boolean mLocationPermissionGranted = false;
    TextView myPageTextview;
    private TextView starText;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        my_recycler_view = (RecyclerView) findViewById(R.id.mrecyclerView);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        //mCurrentLocation초기화 -> permission안될 경우 때문!!
        mCurrentLocation = new Location("dummyprovider");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission(); //Permission완료해야 recyclerview를 불러온다.(그 전에 불러오면 안되기 때문)


        //마이페이지용 리사이클러 뷰 변수
        myPage_recyclerview = findViewById(R.id.myPage_recyclerview_activitySub);
        Recyclerview_myPage_Adapter myPageAdapter = new Recyclerview_myPage_Adapter(this);
        myPage_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myPage_recyclerview.setAdapter(myPageAdapter);

        //마이페이지 글자 누를시 이벤트
        myPageTextview = findViewById(R.id.myPage_textview_activitySub);
        my_recycler_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                nestedScrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                return false;
            }
        });

      //마이페이지 리사이클러 터치
        myPage_recyclerview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                nestedScrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                return false;
            }
        });

        myPageTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubActivity.this, MyPage.class);
                startActivity(intent);
            }
        });

        //별점평균 누를시 생기는 이벤트
        starText = findViewById(R.id.storeStar);
        starText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //검색 동작.
        TextView searchText = findViewById(R.id.editTextDescription);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        // 검색 동작
                        //kakao api에서 좌표가져오기
                        //해당 좌표 근처 가게들 불러오기 getCloseStoreIdAndGetData(location) ->recyclerview setting
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });
    }


    //location permission을 가져온다.
    //permission이 있으면 location을 가져온다.
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission");

        //permission없는 경우 요청, 있는 경우 location을 가져온다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //permission없는 경우
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            //gps켜져있는지 확인
            final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                Log.d(TAG, "GPS is disabled");
                mCurrentLocation.setLatitude(37.583816);
                mCurrentLocation.setLongitude(127.058877);
                initRecyclerView(mCurrentLocation);
            }else {
                getCurrentLocation();
            }
            mLocationPermissionGranted = true;
        }
    }

    //location을 가져오고 recylcer뷰를 실행한다.
    private void getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation");
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mCurrentLocation = task.getResult();
                                Log.d(TAG, "getCurrentLocation - success mCurrentLocation lat : " + mCurrentLocation.getLatitude() + " long : " + mCurrentLocation.getLongitude());
                                Toast.makeText(getApplicationContext(), "lat : " + mCurrentLocation.getLatitude() +
                                        "\n lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                                initRecyclerView(mCurrentLocation);//내 위치를 가져오면 그 때 recyclerview 실행
                            } else {
                                Log.e(TAG, "getCurrentLocation Exception" + task.getException());
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //permission요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult : grantResult size" + grantResults.length);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission허용한 경우
                    getCurrentLocation();
                } else {
                    //permission거절한 경우
                    Log.d(TAG, "permission is denied");
                    Toast.makeText(getApplicationContext(), "permission is denied", Toast.LENGTH_LONG).show();
                    mCurrentLocation.setLatitude(37.583816);
                    mCurrentLocation.setLongitude(127.058877);
                    initRecyclerView(mCurrentLocation);//확인필요!!!
                }
                return;
            }
        }
    }

    //주변 가게 recyclerviewt세팅!
    private void initRecyclerView(Location locationCenter) {
        Log.d(TAG, "initRecyclerView");
        my_recycler_view.setHasFixedSize(true);
        RecyclerViewDataAdapter adapter = new RecyclerViewDataAdapter(this, mCurrentLocation);
        my_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        my_recycler_view.setAdapter(adapter);
    }


}