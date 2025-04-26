package com.anahit.pawmatch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        @SuppressLint("WrongViewCast") MaterialButton backToSignInButton = findViewById(R.id.backToSignInButton);
        progressBar = findViewById(R.id.progressBar);

        signUpButton.setOnClickListener(v -> registerUser());

        backToSignInButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.error_password_too_short, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    signUpButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, R.string.verification_email_sent, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, R.string.error_sending_verification_email, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, SwipeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        if (errorMessage.contains("email address is already in use")) {
                            Toast.makeText(SignUpActivity.this, R.string.error_email_in_use, Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("email address is badly formatted")) {
                            Toast.makeText(SignUpActivity.this, R.string.error_invalid_email, Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("password is invalid")) {
                            Toast.makeText(SignUpActivity.this, R.string.error_weak_password, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}