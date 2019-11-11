package dev7.id.sidausappspublic.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import dev7.id.sidausappspublic.Model.User;
import dev7.id.sidausappspublic.R;
import dev7.id.sidausappspublic.Server.ApiUtil;
import dev7.id.sidausappspublic.Server.RegisInterface;
import dev7.id.sidausappspublic.Server.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private UserInterface userInterface = ApiUtil.getUserInterface();
    private RegisInterface regisInterface = ApiUtil.getRegisInterface();
    private SharedPreferences setting;
    private static final String TAG = "Login";
    private TextInputEditText etUsername, etPassword, etEmail;
    private Button btnRegis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        doLogin();
    }

    private void initView() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnRegis = findViewById(R.id.btnRegis);
        setting = getSharedPreferences("USER", MODE_PRIVATE);
    }

    private void doLogin() {
        btnRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && password.length() > 3 && username.length() > 2){

                    btnRegis.setEnabled(false);
                    Call<User> _user = regisInterface.registerr(username, email, password);
                    _user.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            User result = response.body();
                            if (response.isSuccessful() && result != null){
                                Log.i("done", result.toString());
                                System.out.println("wew "+ response );
                                Intent a = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(a);
                            }else {
                                Toast.makeText(RegisterActivity.this, "Gagal Daftar", Toast.LENGTH_SHORT).show();
                                System.err.println("apaaan " + response);
                                Log.i("done", response.toString());
                            }
                            btnRegis.setEnabled(true);
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            btnRegis.setEnabled(true);
                            System.err.println("mbuh " +t.getMessage());
                            Toast.makeText(RegisterActivity.this, "Daftar Gagal", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }
}
