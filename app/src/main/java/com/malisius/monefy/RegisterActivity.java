package com.malisius.monefy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.malisius.monefy.budget.Budget;
import com.malisius.monefy.category.Category;

import java.io.Console;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class RegisterActivity  extends AppCompatActivity {
    private String username, email, password;
    private TextInputLayout TILusername, TILemail, TILpassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase =  FirebaseDatabase.getInstance();
        TILusername = findViewById(R.id.signup_username);
        TILemail = findViewById(R.id.signup_email);
        TILpassword = findViewById(R.id.signup_password);
        constraintLayout = findViewById(R.id.register_Constraint);
    }

    public void openLoginActivity(View view){
        this.onBackPressed();
    }

    public void signUp(View view){
        email = TILemail.getEditText().getText().toString();
        password = TILpassword.getEditText().getText().toString();
        username = TILusername.getEditText().getText().toString();
        Log.d("RegisterActivity", "email: "+email+" password: "+password);
        boolean isOk = validate(email, password, username);
        if(isOk == false) return;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("RegisterActivity", "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            User user_username = new User(username, user.getEmail());
                            mDatabase.getReference("Users").child(mAuth.getUid()).setValue(user_username);
                            initiateCategory();
                            Bundle userBundle = new Bundle();
                            userBundle.putString("username", username);
                            userBundle.putString("uid", user.getUid());
                            Snackbar.make(constraintLayout,"User Created",Snackbar.LENGTH_SHORT).show();
                            Intent homeIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                            homeIntent.putExtra("USER_DATA", userBundle);
                            startActivity(homeIntent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("RegisterActivity", task.getException().getMessage());

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                TILemail.setError(task.getException().getMessage());
                            } else {
                                Snackbar.make(constraintLayout,"Unknown Error",Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private boolean validate(String email, String password, String username){
        final boolean[] isOk = new boolean[1];
        isOk[0] = true;
        TILpassword.setError(null);
        TILemail.setError(null);
        TILusername.setError(null);
        if(username.isEmpty()){
            TILusername.setError("Username Must Not Empty");
            isOk[0] = false;
        } else {
            if(Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                TILusername.setError("Username Is Same as email");
                isOk[0] = false;
            }
            DatabaseReference userDataRef = mDatabase.getReference().child("Users");
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot userDataSnapshot : dataSnapshot.getChildren()) {
                        Log.i("RegisterActivity", userDataSnapshot.child("username").getValue().toString());
                        if (username.equals(userDataSnapshot.child("username").getValue().toString())) {
                            isOk[0] = false;
                            TILusername.setError("Username Has been Taken");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            userDataRef.addListenerForSingleValueEvent(eventListener);
        }
        if(email.isEmpty()){
            TILemail.setError("Email Must Not Empty");
            isOk[0] = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            TILemail.setError("Email Field Must Be in Email Format");
            isOk[0] = false;
        }
        if(password.isEmpty() || password.length() <= 6){
            if(password.length() <= 6) TILpassword.setError("Password Must be 6 charater long");
            else TILpassword.setError("Password Must Not Empty");
            isOk[0] = false;
        }

        return isOk[0];
    }

    private void initiateCategory() {
        DatabaseReference userDataRef = mDatabase.getReference().child("Data").child(mAuth.getCurrentUser().getUid()).child("Categories");
        DatabaseReference userBudgetRef = mDatabase.getReference().child("Data").child(mAuth.getCurrentUser().getUid()).child("Budget");
        ArrayList<Category> categoriesName = new ArrayList<Category>();
        ArrayList<String> names = new ArrayList<String>();
        names.add("Food");
        names.add("Shopping");
        names.add("Housing");
        names.add("Transportation");
        names.add("Financial");
        for(int i = 0; i < 5; i++){
            Random obj = new Random();
            int rand_num = obj.nextInt(0xffffff + 1);
            String colorCode = String.format("#%06x", rand_num);
            categoriesName.add(new Category(names.get(i),colorCode));
        }

        for(Category category : categoriesName){
            String categoryKey = userDataRef.push().getKey();
            userDataRef.child(categoryKey).setValue(category);
            userBudgetRef.child(categoryKey).setValue(new Budget(0,0, category.getName()));
        }
    }
}
