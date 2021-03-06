package com.rococodish.front_ui.Search;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.NestedScrollView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rococodish.front_ui.DataModel.AlgoliaTagData;
import com.rococodish.front_ui.DataModel.SearchedData;
import com.rococodish.front_ui.DataModel.StoreInfo;
import com.rococodish.front_ui.DataModel.UserInfo;
import com.rococodish.front_ui.Interface.AlgoliaSearchPredicate;
import com.rococodish.front_ui.R;
import com.rococodish.front_ui.Utils.AlgoliaUtils;
import com.rococodish.front_ui.Utils.JsonParsing;
import com.rococodish.front_ui.Utils.KakaoApiStoreSearchService;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SubSearchPage extends AppCompatActivity {

    private final String TAG = "TAGSubSearchPage";

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentStore fragmentStore = new FragmentStore();
    private FragmentRegion fragmentRegion = new FragmentRegion();
    private FragmentTag fragmentTag = new FragmentTag();
    private FragmentPeople fragmentPeople = new FragmentPeople();
    private SearchRecordFragment fragmentRecord = new SearchRecordFragment();


    Retrofit retrofit;
    KakaoApiStoreSearchService service;
    //TODO: 리팩토링 : kakao api utils따로 만들어서 StoreSearchFragment코드랑 합치기
    private final String kakaoApiId = getString(R.string.kakaoApiId);


    public static MyDBHandler dbHandler;
    SimpleCursorAdapter mAdapter = null;
    EditText editText;
    ImageView searchBtn;
    ImageView backBtn;

    //검색결과 리스트
    ArrayList<StoreInfo> storeList;
    ArrayList<SearchedData> regionList;
    ArrayList<UserInfo> peopleList;
    ArrayList<AlgoliaTagData> tagList;
    ArrayList<RecordData> recordList;


    //리사이클러 뷰
    RecyclerView recyclerViewStore;
    RecyclerView recyclerViewPeople;
    RecyclerView recyclerViewRegion;
    RecyclerView recyclerViewTag;

    NestedScrollView scrollView;

    CardView cardViewStore;
    CardView cardViewPeople;
    CardView cardViewRegion;
    CardView cardViewTag;

    TextView tvStore;
    TextView tvRegion;
    TextView tvPeople;
    TextView tvTag;


    double currentLatitude;
    double currentLongtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_search_page);

        Intent intent = this.getIntent();
        currentLatitude = intent.getDoubleExtra("latitude", 0.0);
        currentLongtitude = intent.getDoubleExtra("longitude", 0.0);

        storeList = new ArrayList<>();;
        regionList = new ArrayList<>();
        peopleList = new ArrayList<>();
        recordList = new ArrayList<>();


        recyclerViewStore = findViewById(R.id.recyclerviewStore);
        recyclerViewPeople = findViewById(R.id.recyclerviewPeople);
        recyclerViewRegion = findViewById(R.id.recyclerviewRegion);
        recyclerViewTag = findViewById(R.id.recyclerviewTag);

        cardViewStore = findViewById(R.id.cardviewStore);
        cardViewPeople = findViewById(R.id.cardviewPeople);
        cardViewRegion = findViewById(R.id.cardviewRegion);
        cardViewTag = findViewById(R.id.cardviewTag);

        tvStore = findViewById(R.id.tv_store);
        tvPeople = findViewById(R.id.tv_people);
        tvRegion = findViewById(R.id.tv_region);
        tvTag = findViewById(R.id.tv_tag);

        backBtn = findViewById(R.id.backButton);

        editText = findViewById(R.id.mainsearch_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty( editText.getText().toString().trim())){
                    Log.d(TAG, "search button is clicked");
                    String keyword = editText.getText().toString();
                    searchButtonClicked(keyword);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                return true;
            }
        });

        //검색 버튼
        searchBtn = findViewById(R.id.main_search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty( editText.getText().toString().trim())) {
                    Log.d(TAG, "search button is clicked");
                    String keyword = editText.getText().toString();
                    searchButtonClicked(keyword);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        tvStore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "tvStore is clicked");
                initFragment(fragmentStore);
            }
        });
        tvRegion.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "tvRegion is clicked");
                initFragment(fragmentRegion);
            }
        });
        tvPeople.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "tvPeople is clicked");
                initFragment(fragmentPeople);
            }
        });
        tvTag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "tvTag is clicked");
                initFragment(fragmentTag);

            }
        });


        //뒤로가기(백) 버튼
        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        /*
        * DB설정
        * */
        //검색 기록을 위한 DB
        if( dbHandler == null ) {
            dbHandler = MyDBHandler.open(this);
        }
        Cursor c = dbHandler.select();
        Log.d(TAG, "record index : " + c.getColumnIndex("RECORD"));
        Log.d(TAG, "record size : " + c.getCount());

        if(c.getCount() != 0)
            dbHandler.getAllRecordData(recordList);
        //검색 기록 프래그먼트부터 보여준다.
        initFragment(fragmentRecord);
//        mAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.simple_list_item_activated_2,
//                c, new String[]{"id", "record"}, new int[]{android.R.id.text1, android.R.id.text2}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        //id와 record를 찍어보자

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHandler.close();
    }

    public void searchButtonClicked(String keyword){
        if(keyword != null)
            dbHandler.insert(keyword);
        deleteFragment();
        getRegionSearchResult(keyword);
        getPeopleSearchResult(keyword);
        //TODO: TAG data 일단 검색 막았음!!!
        recyclerViewTag.setVisibility(View.GONE);
        cardViewTag.setVisibility(View.GONE);
//        getTagSearchResult(keyword);
        getStoreSearchResult(keyword);
    }

    /********************************* 리사이클러 뷰 4개 세팅  ********************************/
    void initRecyclerViewStore(){
        Log.d(TAG, "initRecyclerViewStore");
        //가게 데이터를 4개만 가져간다.
        ArrayList<StoreInfo> shortList = new ArrayList<>();
        int size = (4 < storeList.size()? 4 : storeList.size());
        for(int i = 0 ; i < size; i++)
            shortList.add(storeList.get(i));
        //가게 안에 목록 가져오는 리사이클러뷰
        recyclerViewStore.setHasFixedSize(true);
        FragmantStoreRecyclerViewAdapter storeRecyclerViewAdapter = new FragmantStoreRecyclerViewAdapter(this, shortList);
        storeRecyclerViewAdapter.notifyDataSetChanged();
        recyclerViewStore.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerViewStore.setAdapter(storeRecyclerViewAdapter);
    }
    void initRecyclerViewPeople(){
        Log.d(TAG, "initRecyclerViewPeople");

        //가게 데이터를 4개만 가져간다.
        ArrayList<UserInfo> shortList = new ArrayList<>();
        int size = (4 < peopleList.size()? 4 : peopleList.size());
        for(int i = 0 ; i < size; i++)
            shortList.add(peopleList.get(i));

        //가게 안에 목록 가져오는 리사이클러뷰
        recyclerViewPeople.setHasFixedSize(true);
        FragmantPeopleRecyclerViewAdapter peopleRecyclerViewAdapter = new FragmantPeopleRecyclerViewAdapter(this, shortList, currentLatitude, currentLongtitude);
        peopleRecyclerViewAdapter.notifyDataSetChanged();
        recyclerViewPeople.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerViewPeople.setAdapter(peopleRecyclerViewAdapter);
    }
    void initRecyclerViewRegion(){
        Log.d(TAG, "initRecyclerViewRegion");
        //가게 데이터를 4개만 가져간다.
        ArrayList<SearchedData> shortList = new ArrayList<>();
        int size = (4 < regionList.size()? 4 : regionList.size());
        for(int i = 0 ; i < size; i++)
            shortList.add(regionList.get(i));
        //가게 안에 목록 가져오는 리사이클러뷰
        recyclerViewRegion.setHasFixedSize(true);
        FragmantRegionRecyclerViewAdapter regionRecyclerViewAdapter = new FragmantRegionRecyclerViewAdapter(this, shortList);
        regionRecyclerViewAdapter.notifyDataSetChanged();
        recyclerViewRegion.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerViewRegion.setAdapter(regionRecyclerViewAdapter);
    }
    void initRecyclerViewTag(){
        Log.d(TAG, "initRecyclerViewStore");
        //가게 데이터를 4개만 가져간다.
        ArrayList<AlgoliaTagData> shortList = new ArrayList<>();
        int size = (4 < tagList.size()? 4 : tagList.size());
        for(int i = 0 ; i < size; i++)
            shortList.add(tagList.get(i));
        //가게 안에 목록 가져오는 리사이클러뷰
        recyclerViewTag.setHasFixedSize(true);
        FragmantTagRecyclerViewAdapter tagRecyclerViewAdapter = new FragmantTagRecyclerViewAdapter(this, shortList);
        tagRecyclerViewAdapter.notifyDataSetChanged();
        recyclerViewTag.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerViewTag.setAdapter(tagRecyclerViewAdapter);
    }

    void initFragment(Fragment fragment){
        fragmentManager.beginTransaction().replace(R.id.resultFrameLayout, fragment).commit();
    }
    void deleteFragment(){
        fragmentManager.beginTransaction().remove(fragmentStore).commit();
        fragmentManager.beginTransaction().remove(fragmentRegion).commit();
        fragmentManager.beginTransaction().remove(fragmentPeople).commit();
//        fragmentManager.beginTransaction().remove(fragmentTag).commit();
        fragmentManager.beginTransaction().remove(fragmentRecord).commit();
    }

    /********************************* 가게, 지역, 사람 검색 만들기 필요 변수 및 함수  *********************************/
    void getStoreSearchResult(String keyword){
        AlgoliaUtils.searchData("store", "name", keyword, new AlgoliaSearchPredicate() {
            @Override
            public void gettingJSONArrayCompleted(JSONArray jsonArray) {
                storeList =  JsonParsing.getStoreListFromJsonList(jsonArray);
                for(int i = 0; i < storeList.size(); i++){
                    Log.d(TAG, "store data " + i + " : "+ storeList.get(i).getName());
                }
                if(storeList.size() != 0) {
                    initRecyclerViewStore();
                    cardViewStore.setVisibility(View.VISIBLE);
                    recyclerViewStore.setVisibility(View.VISIBLE);
                }else {
                    Log.d(TAG, "no data for Store");
                    recyclerViewStore.setVisibility(View.GONE);
                    cardViewStore.setVisibility(View.GONE);
                }
            }
        });
    }

    void getPeopleSearchResult(String keyword){
        AlgoliaUtils.searchData("user", "nickname", keyword, new AlgoliaSearchPredicate() {
            @Override
            public void gettingJSONArrayCompleted(JSONArray jsonArray) {
                peopleList =  JsonParsing.getUserListFromJsonList(jsonArray);
                for(int i = 0; i < peopleList.size(); i++){
                    Log.d(TAG, "user data " + i + " : "+ peopleList.get(i).getNickname());
                }
                if(peopleList.size() != 0) {
                    initRecyclerViewPeople();
                    cardViewPeople.setVisibility(View.VISIBLE);
                    recyclerViewPeople.setVisibility(View.VISIBLE);
                }else {
                    Log.d(TAG, "no data for People");
                    recyclerViewPeople.setVisibility(View.GONE);
                    cardViewPeople.setVisibility(View.GONE);
                }
            }
        });
    }

    void getTagSearchResult(String keyword){
        AlgoliaUtils.searchData("tag", "text", keyword, new AlgoliaSearchPredicate() {
            @Override
            public void gettingJSONArrayCompleted(JSONArray jsonArray) {
                Log.d(TAG, "tag data json array " + jsonArray.toString());
                tagList =  JsonParsing.getTagListFromJsonList(jsonArray);
                for(int i = 0; i < tagList.size(); i++){
                    Log.d(TAG, "tag data " + i + " : "+ tagList.get(i).getText());
                }
                if(tagList.size() != 0) {
                    initRecyclerViewTag();
                    cardViewTag.setVisibility(View.VISIBLE);
                    recyclerViewTag.setVisibility(View.VISIBLE);
                }else {
                    Log.d(TAG, "no data for Tag");
                    recyclerViewTag.setVisibility(View.GONE);
                    cardViewTag.setVisibility(View.GONE);
                }
            }
        });
    }

    /********************************* 지역검색 만들기 필요 변수 및 함수 *********************************/
    private ArrayList<SearchedData> local_list;
    private ArrayList<SearchedData> subway_list;
//    private ArrayList<SearchedData> attraction_list;
    private ArrayList<SearchedData> school_list;

    private int localCnt;
    private int schoolCnt;
    private int subwayCnt;
    private int attractionCnt;
    private final String school_code = "SC4";
    private final String subway_code = "SW8";
//    private final String attraction_code = "AT4";
    void getRegionSearchResult(final String searchWord){

        localCnt = -1;
        schoolCnt  = -1;
        subwayCnt  = -1;
        attractionCnt = -1;

        retrofit = new Retrofit.Builder()
                .baseUrl(KakaoApiStoreSearchService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(KakaoApiStoreSearchService.class);
        Call<JsonObject> request_local = service.getKakaoLocalInfo(kakaoApiId, searchWord);//, code
        Call<JsonObject> request_school = service.getKakaoStoreInfo(kakaoApiId, searchWord, school_code, "2");//, code
        Call<JsonObject> request_subway = service.getKakaoStoreInfo(kakaoApiId, searchWord, subway_code, "1");//, code
//        Call<JsonObject> request_attraction = service.getKakaoStoreInfo(kakaoApiId, searchWord, attraction_code, "4");//, code

        //로컬 listener
        request_local.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.body() != null) {
                    Log.d(TAG, "request_local enqueue is Successed. data : " + response.body().toString());
                    local_list = JsonParsing.parseJsonToSearchedInfo(response.body(), searchWord, 1);
                    localCnt = local_list.size();
                    if(localCnt > -1 && schoolCnt >-1 && subwayCnt > -1){
                        makeResultForStoreSearch();
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "request_local enqueue is failed : w : " + t.toString());
                t.printStackTrace();
            }
        });

        //대학교 listener
        request_school.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.body() != null) {
                    Log.d(TAG, "request_school enqueue is Successed. data : " + response.body().toString());
                    school_list = JsonParsing.parseJsonToSearchedInfo(response.body(),  searchWord,2);
                    schoolCnt = school_list.size();
                    if(localCnt > -1 && schoolCnt >-1 && subwayCnt > -1){
                        makeResultForStoreSearch();
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "request_school enqueue is failed : w : " + t.toString());
                t.printStackTrace();
            }
        });

        //지하철 listener
        request_subway.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.body() != null) {
                    Log.d(TAG, "request_subway enqueue is Successed. data : " + response.body().toString());
                    subway_list = JsonParsing.parseJsonToSearchedInfo(response.body(), searchWord, 2);
                    subwayCnt = subway_list.size();
                    if(localCnt > -1 && schoolCnt >-1 && subwayCnt > -1){
                        makeResultForStoreSearch();
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "request_subway enqueue is failed : w : " + t.toString());
                t.printStackTrace();
            }
        });

        //관광명소 listener
//        request_attraction.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if(response.body() != null) {
//                    Log.d(TAG, "request_attraction enqueue is Successed. data : " + response.body().toString());
//                    attraction_list = JsonParsing.parseJsonToSearchedInfo(response.body(), searchWord, 2);
//                    attractionCnt = attraction_list.size();
//                    if(localCnt > -1 && schoolCnt >-1 && subwayCnt > -1 && attractionCnt > -1){
//                        makeResultForStoreSearch();
//                    }
//                }
//            }
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG, "request_attraction enqueue is failed : w : " + t.toString());
//                t.printStackTrace();
//            }
//        });
    }

    void makeResultForStoreSearch(){
        regionList = local_list;
        regionList.addAll(subway_list);
        regionList.addAll(school_list);
//        regionList.addAll(attraction_list);
        Log.d(TAG, "size of local list : " + local_list.size());
        Log.d(TAG, "size of region list : " + regionList.size());

        for(int i = 0; i < regionList.size(); i++){
            Log.d(TAG, "region data " + i + " : "+ regionList.get(i).getPlace_name()
                    + " x : " + regionList.get(i).getX() + " y : " + regionList.get(i).getY());
        }
        if(regionList.size() != 0) {
            initRecyclerViewRegion();
            cardViewRegion.setVisibility(View.VISIBLE);
            recyclerViewRegion.setVisibility(View.VISIBLE);
        }else {
            Log.d(TAG, "no data for region");
            recyclerViewRegion.setVisibility(View.GONE);
            cardViewRegion.setVisibility(View.GONE);
        }
    }

    /********************************* 데이터 전달을 위한 함수 - 리팩토링 필요한가?? *********************************/
    public ArrayList<StoreInfo> getStoreList(){
        return storeList;
    }
    public ArrayList<SearchedData> getRegionList(){
        return regionList;
    }
    public ArrayList<UserInfo> getPeopleList(){
        return peopleList;
    }
    public ArrayList<AlgoliaTagData> getTagList(){
        return tagList;
    }

    public ArrayList<RecordData> getRecordList(){
        return recordList;
    }
    public double getLat(){
        return currentLatitude;
    }
    public double getLon(){
        return currentLongtitude;
    }



}
