package com.example.front_ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.front_ui.Util_Kotlin.Storage;
import com.example.front_ui.Utils.GlideApp;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import java.io.ByteArrayOutputStream;
import com.example.front_ui.DataModel.PostingInfo;
import com.example.front_ui.Interface.MyPageDataPass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

//interface for datapass to MyPage
public class MyPage extends AppCompatActivity implements MyPageDataPass {

    private final String TAG = "TAGMyPage";
    int numOfMyPost = 0;
    TextView tvOfNum;

    @Override
    public void setNumberOfData(int value) {
        Log.d(TAG, "setNumberOfData - callback function");
        numOfMyPost = value;
        if(tvOfNum == null)
            tvOfNum = findViewById(R.id.textNumOfData);
        tvOfNum.setText(Integer.toString(numOfMyPost));
    }

    private int RC_GALLERY = 1;
    private int RC_CROP = 2;
    private int RC_CAMERA = 1001;
    public ImageButton imageButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_page);

        MyAdapter adapter = new MyAdapter(
                getApplicationContext(),
                R.layout.polar_style,
                this);       // GridView 항목의 레이아웃 row.xml

        GridView gv = findViewById(R.id.gridview);
        gv.setAdapter(adapter);  // 커스텀 아답타를 GridView 에 적용
        TextView tv = findViewById(R.id.textNumOfData);

        imageButton = (ImageButton) findViewById(R.id.imageView);
        //킬 때마다 프로필 사진 불러옴
        final ProgressDialog progressDialog = ProgressDialog.show(this, "로딩중", "잠시만 기다려주세요...");
        FirebaseFirestore.getInstance()
                .collection("사용자")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.get("profileImage") != null){//프로필 사진이 있을 경우
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
                    circularProgressDrawable.setStrokeCap(Paint.Cap.ROUND);
                    circularProgressDrawable.setCenterRadius(10f);
                    circularProgressDrawable.setBackgroundColor(R.color.colorMainSearch);
                    circularProgressDrawable.start();

                    String path = documentSnapshot.get("profileImage").toString();
                    GlideApp.with(getApplicationContext())
                            .load(path)
                            .placeholder(circularProgressDrawable)
                            .into(imageButton);
                    progressDialog.dismiss();
                }
                else{//프로필 사진이 없을 경우 걍 로딩 없앰.
                    progressDialog.dismiss();
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            final String[] profiles = new String[] {"앨범에서 사진 선택", "기본 이미지로 변경"};
            @Override
            public void onClick(final View v) {
                switch (v.getId()) {
                    case R.id.imageView :
                        new AlertDialog.Builder(MyPage.this)
                            .setTitle("프로필")
                            .setItems(profiles, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (profiles[which]) {
                                        case "앨범에서 사진 선택":
                                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                            startActivityForResult(intent, RC_GALLERY);
                                            break;
                                        case "기본 이미지로 변경":
                                            FirebaseFirestore.getInstance().collection("사용자")
                                                    .document(FirebaseAuth.getInstance().getUid())
                                                    .update("profileImage", null);
                                            Intent mintent = new Intent(getApplicationContext(), MyPage.class);
                                            startActivity(mintent);
                                            finish();
                                            break;
                                    }
                                }
                            })
                            .show();
                        break;
                }
            }
        });

        imageButton.setBackground(new ShapeDrawable(new OvalShape()));
        if(Build.VERSION.SDK_INT >= 22) {
            imageButton.setClipToOutline(true);//프로필 이미지 동그랗게
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //프로필 사진 고를 시 앨범에서 사진 가져올 경우
        if(requestCode == RC_GALLERY && resultCode == Activity.RESULT_OK && data != null){
            Uri selectedImageFromGallery = data.getData();

            Uri destinationUri = Uri.fromFile(new File(getApplicationContext().getCacheDir(), "IMG_" + System.currentTimeMillis()));
            UCrop.of(selectedImageFromGallery, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(450, 450)
                    .start(this);

//            //크롭 자체 제작(라이브러리 안씀)
//            Intent cropIntent = new Intent("com.android.camera.action.CROP");
//            cropIntent.setDataAndType(selectedImageFromGallery, "image/*");
//            cropIntent.putExtra("crop", "true");
//            cropIntent.putExtra("aspectX", 1);
//            cropIntent.putExtra("aspectY", 1);
//            cropIntent.putExtra("outputX", 128);
//            cropIntent.putExtra("outputY", 128);
//            cropIntent.putExtra("return-data", true);
//            startActivityForResult(cropIntent, RC_CROP);
//            Log.d(TAG, "갤러리에서 이미지를 받은 후 크롭으로 넘어갑니다.");
        }
//        if(requestCode == RC_CROP){
//            if(resultCode == Activity.RESULT_OK){
//                Bundle extras = data.getExtras();
//                final ProgressDialog progressDialog  = ProgressDialog.show(this, "로딩중", "잠시만 기다려주세요...");
//                if(extras != null){
//                    Bitmap bmp = extras.getParcelable("data");
//                    ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                    byte[] byteArray = outputStream.toByteArray();
//                    Storage.INSTANCE.uploadProfileImage(byteArray, progressDialog);
//                    GlideApp.with(this)
//                            .load(bmp)
//                            .into(imageButton);
//                }
//            }
//        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            final ProgressDialog progressDialog  = ProgressDialog.show(this, "로딩중", "잠시만 기다려주세요...");
            if (resultUri != null) {
                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    Storage.INSTANCE.uploadProfileImage(byteArray, progressDialog);
                    GlideApp.with(this)
                            .load(bmp)
                            .into(imageButton);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class MyAdapter extends BaseAdapter {
    private final String TAG = "TAGMyAdapter";
    FirebaseFirestore db;

    Context context;
    int layout;
    ArrayList<PostingInfo> list;
    LayoutInflater inf;
    FirebaseStorage storage;
    StorageReference storageReference;

    //interface for datapass to MyPage
    private MyPageDataPass mCallback;


    public MyAdapter(Context context, int layout, MyPageDataPass listener) {
        list = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        this.context = context;
        this.layout = layout;
        mCallback = listener;
        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        getPostingDataFromCloud();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    //이미지 세팅
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inf.inflate(layout, null);
        ImageView iv = (ImageView)convertView.findViewById(R.id.imagefood);

        final PostingInfo singleItem = list.get(position);
        Log.d(TAG, "downloadImageFromFirebaseStorage : " + singleItem.imagePathInStorage);
        StorageReference fileReference = storage.getReferenceFromUrl(singleItem.imagePathInStorage);
        GlideApp.with(context).load(fileReference).into(iv);


        return convertView;
    }

    //내가 쓴 데이터를 모두 가져온다.
    private void getPostingDataFromCloud() {
        Log.d(TAG, "getDataFromFirestore");

        db.collection("포스팅")
                .whereEqualTo("writerId", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                //store 정보를 가져오고, id를 따로 저장한다.
                                PostingInfo postingInfo = document.toObject(PostingInfo.class);
                                //해당 가게 정보의 post데이터를 가져온다.
                                list.add(postingInfo);
                                notifyDataSetChanged();
                            }
                            Log.d(TAG, "getPostingData size : " + task.getResult().size());
                            mCallback.setNumberOfData(task.getResult().size());
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}

