package cn.practice.myapplication.page;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import cn.practice.myapplication.R;
import cn.practice.myapplication.atcivity.EditDataActivity;
import cn.practice.myapplication.atcivity.LoginActivity;
import cn.practice.myapplication.atcivity.PrivateSetActivity;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MePager extends BasePager {

    public ImageView profileImageView;
    public EditText titleEditText;
    public Button editDataButton, privateSetButton, changeCoverButton, shareButton, cancelButton;
    public ConstraintLayout rootViewLayout;

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final String PREFS_NAME = "UserProfilePrefs";
    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String KEY_PROFILE_IMAGE = "profileImage";
    private static final String KEY_TITLE = "title";

    private String username;
    private boolean isLoggedIn;

    @Override
    public void initData() {
        this.LayerId = 2;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me_music, container, false);
        initData();

        profileImageView = rootView.findViewById(R.id.profileImageView);
        titleEditText = rootView.findViewById(R.id.titleEditText);
        editDataButton = rootView.findViewById(R.id.editdata);
        privateSetButton = rootView.findViewById(R.id.privateset);
        changeCoverButton = rootView.findViewById(R.id.firstEditText);
        shareButton = rootView.findViewById(R.id.shareButton);
        cancelButton = rootView.findViewById(R.id.cancelButton);
        rootViewLayout = rootView.findViewById(R.id.rootViewLayout); // Assuming you have this ID in your layout

        checkLoginState();
        if (isLoggedIn) {
            loadSavedData();
        } else {

        }

        profileImageView.setOnClickListener(v -> openGallery());
        editDataButton.setOnClickListener(v -> navigateToEditData());
        privateSetButton.setOnClickListener(v -> navigateToPrivateSet());
        changeCoverButton.setOnClickListener(v -> changeCoverBackground());
        shareButton.setOnClickListener(v -> shareScreenshot());
        cancelButton.setOnClickListener(v -> navigateToLogin());

        setupEditText();

        return rootView;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void navigateToEditData() {
        Intent intent = new Intent(getActivity(), EditDataActivity.class); // Replace with your target Activity
        startActivity(intent);
    }

    private void navigateToPrivateSet() {
        Intent intent = new Intent(getActivity(), PrivateSetActivity.class); // Replace with your target Activity
        startActivity(intent);
    }

    private void changeCoverBackground() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        rootViewLayout.setBackgroundColor(color);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class); // Replace with your target Activity
        startActivity(intent);
    }

    private void shareScreenshot() {
        // 启用绘图缓存
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        try {
            // 保存截图到外部缓存目录
            File file = new File(getContext().getExternalCacheDir(), "screenshot.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();

            // 获取文件的Uri
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);

            // 创建一个分享意图
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setPackage("com.tencent.mm"); // 微信的包名

            // 启动分享活动
            startActivity(Intent.createChooser(shareIntent, "分享截图"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            profileImageView.setImageURI(selectedImage);
            saveProfileImage(selectedImage);
        }
    }

    private void setupEditText() {
        titleEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                titleEditText.setHint("");
                titleEditText.setCursorVisible(true);
            } else {
                if (titleEditText.getText().toString().isEmpty()) {
                    titleEditText.setHint("请输入昵称");
                }
                // 移除光标
                titleEditText.setCursorVisible(false);
                saveTitle();
            }
        });

        // 当输入框有文本输入时，显示光标
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleEditText.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 点击其他地方隐藏键盘并移除焦点
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (titleEditText.isFocused()) {
                    titleEditText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
                }
            }
            return false;
        });
    }

    private void saveTitle() {
        String title = titleEditText.getText().toString().trim();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username + "_" + KEY_TITLE, title);
        editor.apply();
    }

    private void saveProfileImage(Uri imageUri) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username + "_" + KEY_PROFILE_IMAGE, imageUri.toString());
        editor.apply();
    }

    private void loadSavedData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);
        String savedTitle = sharedPreferences.getString(username + "_" + KEY_TITLE, "");
        String savedProfileImageUri = sharedPreferences.getString(username + "_" + KEY_PROFILE_IMAGE, "");

        titleEditText.setText(savedTitle);

        if (!savedProfileImageUri.isEmpty()) {
            Uri imageUri = Uri.parse(savedProfileImageUri);
            profileImageView.setImageURI(imageUri);
        }
    }

    private void checkLoginState() {
        SharedPreferences loginPrefs = getContext().getSharedPreferences(LOGIN_PREFS, getContext().MODE_PRIVATE);
        isLoggedIn = loginPrefs.getBoolean("isLoggedIn", false);
        username = loginPrefs.getString("username", null);

        if (!isLoggedIn || username == null) {
            // Handle the case when the user is not logged in
            // You may want to redirect the user to the login page
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
