package cn.practice.myapplication.atcivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.practice.myapplication.R;

public class PrivateSetActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PrivacyPrefs";
    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String KEY_ID_NUMBER = "idNumber";
    private static final String KEY_ADDRESS = "address";

    private EditText idNumberEditText;
    private EditText addressEditText;
    private Button submitButton;
    private ExecutorService executorService;
    private String username;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_set);

        idNumberEditText = findViewById(R.id.idNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);
        submitButton = findViewById(R.id.submitButton);
        executorService = Executors.newSingleThreadExecutor();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkLoginState();
        if (isLoggedIn) {
            loadSavedData();
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        idNumberEditText.addTextChangedListener(textWatcher);
        addressEditText.addTextChangedListener(textWatcher);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPrivacySettings();
            }
        });
    }

    private void checkLoginState() {
        SharedPreferences loginPrefs = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        isLoggedIn = loginPrefs.getBoolean("isLoggedIn", false);
        username = loginPrefs.getString("username", null);
    }

    private void checkInputFields() {
        String idNumber = idNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        boolean isIdNumberValid = idNumber.length() == 18;
        submitButton.setEnabled(isIdNumberValid && !address.isEmpty());

        if (!isIdNumberValid && !idNumber.isEmpty()) {
            idNumberEditText.setError("身份证号必须是18位");
        }
    }

    private void submitPrivacySettings() {
        String idNumber = idNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("idNumber", idNumber);
            jsonData.put("address", address);

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    boolean success = sendPostRequest(jsonData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(PrivateSetActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                                idNumberEditText.setText(maskIdNumber(idNumber));
                                addressEditText.setText(address);
                                // 锁定输入框
                                idNumberEditText.setEnabled(false);
                                addressEditText.setEnabled(false);
                                // 保存数据
                                saveData(idNumber, address);
                            } else {
                                Toast.makeText(PrivateSetActivity.this, "提交失败，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "提交失败，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean sendPostRequest(JSONObject jsonData) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://10.136.5.201:8080/login"); // Replace with your backend endpoint
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber.length() >= 6) {
            String start = idNumber.substring(0, 3);
            String end = idNumber.substring(idNumber.length() - 3);
            StringBuilder masked = new StringBuilder(start);
            for (int i = 0; i < idNumber.length() - 6; i++) {
                masked.append("*");
            }
            masked.append(end);
            return masked.toString();
        }
        return idNumber;
    }

    private void saveData(String idNumber, String address) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username + "_" + KEY_ID_NUMBER, idNumber);
        editor.putString(username + "_" + KEY_ADDRESS, address);
        editor.apply();
    }

    private void loadSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedIdNumber = sharedPreferences.getString(username + "_" + KEY_ID_NUMBER, "");
        String savedAddress = sharedPreferences.getString(username + "_" + KEY_ADDRESS, "");

        if (!savedIdNumber.isEmpty() && !savedAddress.isEmpty()) {
            idNumberEditText.setText(maskIdNumber(savedIdNumber));
            addressEditText.setText(savedAddress);
            // 锁定输入框
            idNumberEditText.setEnabled(false);
            addressEditText.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
