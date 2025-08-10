package com.example.testfeatureofqiksafe.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment?
        val navController = navHostFragment?.navController

        NavigationUI.setupWithNavController(binding.toolBar.myToolbar, navController!!)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolBar.lblTitleToolbar.text = when (destination.id) {
                R.id.homeFragment -> "QikSafe App"
                R.id.emergencySettingFragment -> "Setting"
                R.id.startEmergencyFragment -> "Start Emergency"
                R.id.ContactFragment -> "Contact"
                R.id.addContactFragment -> "Add Contact"
                R.id.recentAlertFragment -> "Recent Alert"
                R.id.userProfileFragment -> "Profile"
                R.id.helpCenterFragment -> "Help Center"
                else -> ""
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.nav_contact -> {
                    navController.navigate(R.id.ContactFragment)
                    true
                }
                R.id.nav_alert -> {
                    navController.navigate(R.id.recentAlertFragment)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.userProfileFragment)
                    true
                }
                else -> false
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                0,
            )
            insets
        }

    }

    override fun onNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHost)
        return navController.navigateUp() || super.onNavigateUp()
    }
}
