package com.cryptomcgrath.pyrexia


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.cryptomcgrath.pyrexia.login.RESULT_CODE_LOGIN


class MainActivity: AppCompatActivity() {

    private val navController: NavController? get() {
        val navFrag = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navFrag?.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == RESULT_CODE_LOGIN && resultCode != Activity.RESULT_OK) {
                navController?.popBackStack(R.id.deviceListFragment, false)
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
    }
}