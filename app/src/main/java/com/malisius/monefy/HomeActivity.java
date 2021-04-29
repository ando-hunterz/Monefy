package com.malisius.monefy;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private int backPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navView = findViewById(R.id.bottom_nav);

        /* Only use this when using appbar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.incomeFragment, R.id.expenseFragment, R.id.budgetFragment, R.id.settingsFragment)
                .build();
         */

        /* Nitip buat nanti add */
//        if(dataSnapshot.child("name").getValue().equals("Fashion")){
//            String key = dataSnapshot.getKey();
//            mDatabase.getReference().child(mAuth.getUid()).child(key).setValue(cat);
//        }

        NavController navController = Navigation.findNavController(this, R.id.fragment);

        /* Only use this when using appbar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
         */

        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public void onBackPressed() {
        if(backPressed == 0){
            Toast.makeText(this, "Press the back button once again to exit", Toast.LENGTH_SHORT).show();
            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    backPressed = 0;
                }
            }.start();
        }else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        backPressed++;
    }
}
