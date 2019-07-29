package com.example.front_ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.front_ui.DataModel.UserInfo;
import com.example.front_ui.Utils.AlgoliaUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.oob.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class SignUpActivity extends AppCompatActivity {

    private String TAG = "TAGSignUpActivity";
//    TextView emailLogin;
//    TextView facebookLogin;
//    TextView googleLogin;
//    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder()
//            .setAllowNewAccounts(true)
//            .setRequireName(true)
//            .build());
//    private int RC_EMAIL_LOGIN = 1001;
//    private int RC_GOOGLE_LOGIN = 1002;
//    FirebaseAuth auth = FirebaseAuth.getInstance();
//    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//    @Nullable CallbackManager callbackManager;
    Button goLogin;
    ViewPager viewPager;
    SignUpAdapter signUpAdapter;
    ScrollingPagerIndicator scrollingPagerIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

//        //해시키 가져오기
//        getHashKey(this);

        goLogin = findViewById(R.id.goLogin_button_activitySignUp);
        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginDialog.class));
            }
        });

        viewPager = findViewById(R.id.viewpager_activitySignUp);
        signUpAdapter = new SignUpAdapter(this);
        viewPager.setAdapter(signUpAdapter);
        scrollingPagerIndicator = findViewById(R.id.viewpager_indicator_activitySignUp);
        scrollingPagerIndicator.attachToPager(viewPager);
        scrollingPagerIndicator.setDotColor(getResources().getColor(R.color.colorAccent));
        scrollingPagerIndicator.setSelectedDotColor(getResources().getColor(R.color.MainColor));
//
//        //이메일로 시작하기
//        emailLogin = findViewById(R.id.emailLogin_textview_signUpActivity);
//        emailLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AuthUI.getInstance().createSignInIntentBuilder()
//                        .setAvailableProviders(providers)
//                        .build());
//                startActivityForResult(intent, RC_EMAIL_LOGIN);
//            }
//        });
//
//        //페이스북으로 시작하기
//        facebookLogin = findViewById(R.id.facebookLogin_textview_signUpActivity);
//        facebookLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AccessToken accessToken = AccessToken.getCurrentAccessToken();
//                Boolean isLogged = accessToken != null && !accessToken.isExpired(); //토큰이 사용가능한지 파악
//                LoginManager.getInstance()
//                        .logInWithReadPermissions(
//                                SignUpActivity.this,
//                                Arrays.asList("email", "public_profile"));//페이스북에서 가져올 정보 선택하기 , "user_friends" 앱 검수 필요(완전히 앱을 만드는게 조건)
//                setFacebookLogin();//로그인 실행
//            }
//        });
//
//        //구글로 시작하기
//        googleLogin = findViewById(R.id.googleLogin_textview_signUpActivity);
//        googleLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setGoogleLogin(SignUpActivity.this);
//            }
//        });

    }

//    public void setGoogleLogin(Context context){
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
//        Intent signInIntent = googleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
//    }
//
//    public void setFacebookLogin(){
//        //페이스북에서 응답을 받기 위한 콜백
//        callbackManager = CallbackManager.Factory.create();
//        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "페이스북 로그인 성공");
//                handleFacebookAccessToken(loginResult.getAccessToken());//토큰을 이용해서 credential로 로그인 위한 함수
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "페이스북 로그인 실패");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.d(TAG, "페이스북 로그인 에러 => "+ error.getMessage());
//            }
//
//            private void handleFacebookAccessToken(AccessToken token){
//                Log.d(TAG, "handleFacebookAccessToken : "+token);
//
//                final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
//                progressDialog.setMessage("회원정보 확인중입니다");
//                progressDialog.show();
//
//                AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            Log.d(TAG, "signInWithCredential이 성공적");
//
//                            final FirebaseUser user = auth.getCurrentUser();
//                            final DocumentReference userRef = firestore.collection("사용자").document(auth.getUid());
//
//                            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    if(!documentSnapshot.exists()){
//                                        UserInfo newUser = new UserInfo(
//                                                user.getEmail(),
//                                                user.getDisplayName(),
//                                                MyPage.basicProfile,
//                                                null,
//                                                null,
//                                                0
//                                        );
//                                        AlgoliaUtils.addObject("user", newUser);
//
//                                        userRef.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Log.d(TAG, "새로운 유저 등록에 성공했습니다.");
//                                                startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                                                finish();
//                                                progressDialog.dismiss();
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Log.d(TAG, "새로운 유저 등록에 실패했습니다.");
//                                                progressDialog.dismiss();
//                                            }
//                                        });
//                                    }
//                                    else{
//                                        Log.d(TAG, "이미 가입한 유저입니다.");
//                                        startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                                        finish();
//                                        progressDialog.dismiss();
//                                    }
//                                }
//                            });
//
//                        }
//                        else{
//                            Log.d(TAG, "signInWithCredential이 실패 => "+ task.getException());
//                            Toast.makeText(SignUpActivity.this, "페이스북 인증에 실패했습니다.", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//            }
//
//            //생략가능
//            public void requestMe(AccessToken token){
//                GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
//                    @Override
//                    public void onCompleted(JSONObject object, GraphResponse response) {
//                        Log.d(TAG, "토큰요청 성공의 결과물 = "+ object.toString());
//                    }
//                });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,email,gender,birthday");
//                graphRequest.setParameters(parameters);
//                graphRequest.executeAsync();
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        /**
//         * 이메일 로그인 응답
//         * **/
//        if(requestCode == RC_EMAIL_LOGIN){
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//
//            IdpResponse response = IdpResponse.fromResultIntent(data);
//
//            if(resultCode == RESULT_OK){
//                progressDialog.setMessage("회원정보를 확인중입니다.");
//                progressDialog.show();
//
//                final DocumentReference userRef = firestore.collection("사용자").document(auth.getUid());
//
//                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if(!documentSnapshot.exists()){
//                            final UserInfo newUser = new UserInfo(
//                                    auth.getCurrentUser().getEmail(),
//                                    auth.getCurrentUser().getDisplayName(),
//                                    MyPage.basicProfile,
//                                    null,
//                                    null,
//                                    0);
//
//                            AlgoliaUtils.addObject("user", newUser);
//
//                            userRef.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Log.d(TAG, "새로운 유저 등록에 성공했습니다.");
//                                    startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                                    finish();
//                                    progressDialog.dismiss();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "새로운 유저 등록에 실패했습니다.");
//                                    progressDialog.dismiss();
//                                }
//                            });
//                        }
//                        else{
//                            Log.d(TAG, "이미 가입한 유저입니다.");
//                            startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                            finish();
//                            progressDialog.dismiss();
//                        }
//                    }
//                });
//            }
//            else{
//                Toast.makeText(this, "에러발생", Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//            }
//        }
//        /**
//         * 페이스북 로그인 응답
//         * **/
//        if(callbackManager != null){
//            callbackManager.onActivityResult(requestCode, resultCode, data);
//        }
//
//        /**
//         * 구글 로그인 응답
//         * **/
//        if(requestCode == RC_GOOGLE_LOGIN){
//            Task task = GoogleSignIn.getSignedInAccountFromIntent(data);
//
//            final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
//            progressDialog.setMessage("회원정보 확인중입니다.");
//            progressDialog.show();
//            try{
//                GoogleSignInAccount account = (GoogleSignInAccount) task.getResult(ApiException.class);
//                Log.d(TAG, "구글에서 받은 계정정보 = "+ account.toString());
//                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//                auth.signInWithCredential(credential)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if(task.isSuccessful()){
//                                    Log.d(TAG, "계정정보 받은 후 signInWithCredential:성공");
//
//                                    final DocumentReference userRef = firestore.collection("사용자").document(auth.getUid());
//
//                                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                            if(!documentSnapshot.exists()){
//                                                UserInfo newUser = new UserInfo(
//                                                        auth.getCurrentUser().getEmail(),
//                                                        auth.getCurrentUser().getDisplayName(),
//                                                        MyPage.basicProfile,
//                                                        null,
//                                                        null,
//                                                        0
//                                                );
//                                                AlgoliaUtils.addObject("user", newUser);
//
//                                                userRef.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.d(TAG, "새로운 유저 등록에 성공했습니다.");
//                                                        startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                                                        finish();
//                                                        progressDialog.dismiss();
//                                                    }
//                                                }).addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.d(TAG, "새로운 유저 등록에 실패했습니다.");
//                                                        progressDialog.dismiss();
//                                                    }
//                                                });
//                                            }
//                                            else{
//                                                Log.d(TAG, "이미 가입한 유저입니다.");
//                                                startActivity(new Intent(SignUpActivity.this, SubActivity.class));
//                                                finish();
//                                                progressDialog.dismiss();
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        });
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//        }
    }

//    // 프로젝트의 해시키를 반환
//    @Nullable
//
//    public static String getHashKey(Context context) {
//
//        final String TAG = "KeyHash";
//
//        String keyHash = null;
//
//        try {
//
//            PackageInfo info =
//
//                    context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
//
//
//
//            for (Signature signature : info.signatures) {
//
//                MessageDigest md;
//
//                md = MessageDigest.getInstance("SHA");
//
//                md.update(signature.toByteArray());
//
//                keyHash = new String(Base64.encode(md.digest(), 0));
//
//                Log.d(TAG, keyHash);
//
//            }
//
//        } catch (Exception e) {
//
//            Log.e("name not found", e.toString());
//
//        }
//
//
//
//        if (keyHash != null) {
//
//            return keyHash;
//
//        } else {
//
//            return null;
//
//        }
//
//    }

}
