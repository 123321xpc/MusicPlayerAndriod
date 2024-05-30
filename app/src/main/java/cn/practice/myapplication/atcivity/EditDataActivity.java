package cn.practice.myapplication.atcivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import cn.practice.myapplication.R;
import cn.practice.myapplication.util.MusicUtils;

public class EditDataActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserDataPrefs";
    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTHDAY = "birthday";

    private EditText nameEditText;
    private RadioGroup genderRadioGroup;
    private DatePicker birthdayDatePicker;
    private Button saveButton;
    private String username;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        nameEditText = findViewById(R.id.nameEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        birthdayDatePicker = findViewById(R.id.birthdayDatePicker);
        saveButton = findViewById(R.id.saveButton);

        checkLoginState();
        if (isLoggedIn) {
            loadSavedData();
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void checkLoginState() {
        SharedPreferences loginPrefs = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        isLoggedIn = loginPrefs.getBoolean("isLoggedIn", false);
        username = loginPrefs.getString("username", null);
    }

    private void loadSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = sharedPreferences.getString(username + "_" + KEY_NAME, "");
        String savedGender = sharedPreferences.getString(username + "_" + KEY_GENDER, "");
        String savedBirthday = sharedPreferences.getString(username + "_" + KEY_BIRTHDAY, "");

        nameEditText.setText(savedName);

        if ("男".equals(savedGender)) {
            genderRadioGroup.check(R.id.maleRadioButton);
        } else if ("女".equals(savedGender)) {
            genderRadioGroup.check(R.id.femaleRadioButton);
        }

        if (!savedBirthday.isEmpty()) {
            String[] dateParts = savedBirthday.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);
            birthdayDatePicker.updateDate(year, month, day);
        }

        if (!savedName.isEmpty() || !savedGender.isEmpty() || !savedBirthday.isEmpty()) {
            // Lock the fields if data is present
            lockFields();
        }
    }

    private void saveData() {
        String name = nameEditText.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = selectedGenderRadioButton != null ? selectedGenderRadioButton.getText().toString() : "";
        int day = birthdayDatePicker.getDayOfMonth();
        int month = birthdayDatePicker.getMonth();
        int year = birthdayDatePicker.getYear();

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGenderId == -1) {
            Toast.makeText(this, "请选择性别", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            jsonData.put("gender", gender);
            jsonData.put("birthday", year + "-" + (month + 1) + "-" + day);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = sendPostRequest(jsonData);

                    if (success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameEditText.setText(name);
                                nameEditText.setEnabled(false);

                                if ("男".equals(gender)) {
                                    genderRadioGroup.check(R.id.maleRadioButton);
                                } else if ("女".equals(gender)) {
                                    genderRadioGroup.check(R.id.femaleRadioButton);
                                }
                                genderRadioGroup.setEnabled(false);
                                for (int i = 0; i < genderRadioGroup.getChildCount(); i++) {
                                    genderRadioGroup.getChildAt(i).setEnabled(false);
                                }

                                birthdayDatePicker.updateDate(year, month, day);
                                birthdayDatePicker.setEnabled(false);

                                saveToPreferences(name, gender, year + "-" + (month + 1) + "-" + day);

                                Toast.makeText(EditDataActivity.this, "数据保存成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(EditDataActivity.this, "数据保存失败", Toast.LENGTH_SHORT).show());
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sendPostRequest(JSONObject jsonData) {
        try {
            URL url = new URL("http://" + MusicUtils.ip + ":8080/login"); // Replace with your backend endpoint
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveToPreferences(String name, String gender, String birthday) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username + "_" + KEY_NAME, name);
        editor.putString(username + "_" + KEY_GENDER, gender);
        editor.putString(username + "_" + KEY_BIRTHDAY, birthday);
        editor.apply();
    }

    private void lockFields() {
        nameEditText.setEnabled(false);
        genderRadioGroup.setEnabled(false);
        for (int i = 0; i < genderRadioGroup.getChildCount(); i++) {
            genderRadioGroup.getChildAt(i).setEnabled(false);
        }
        birthdayDatePicker.setEnabled(false);
    }
}
