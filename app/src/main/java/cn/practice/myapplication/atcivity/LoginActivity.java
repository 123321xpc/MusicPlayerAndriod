package cn.practice.myapplication.atcivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.practice.myapplication.R;
import cn.practice.myapplication.atcivity.WelcomeAct;
import cn.practice.myapplication.util.MusicUtils;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        executorService = Executors.newSingleThreadExecutor();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("username", username);
            jsonData.put("password", password);

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String response = sendPostRequest(jsonData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleLoginResponse(response, username);
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "登录失败，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    private String sendPostRequest(JSONObject jsonData) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + MusicUtils.ip + ":8080/login"); // Replace with your backend endpoint
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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void handleLoginResponse(String response, String username) {
        if (response == null) {
            Toast.makeText(this, "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(response);
            int code = jsonResponse.getInt("code");

            if (code == 200) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                saveLoginState(username);
                // 登录成功后跳转到主界面
                Intent intent = new Intent(LoginActivity.this, MainAct.class);
                startActivity(intent);
                finish();
            } else {
                String msg = jsonResponse.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "登录失败，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginState(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
