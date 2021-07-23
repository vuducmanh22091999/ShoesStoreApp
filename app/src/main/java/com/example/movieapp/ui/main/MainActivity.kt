package com.example.movieapp.ui.main

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.data.local.AppPreferences
import com.example.movieapp.ui.account.AccountFragment
import com.example.movieapp.ui.favorite.FavoriteFragment
import com.example.movieapp.ui.home.HomeFragment
import com.example.movieapp.ui.login.LoginActivity
import com.example.movieapp.ui.search.SearchMovieFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_question_login.*


class MainActivity : BaseActivity() {
    private var fragment = Fragment()
    private lateinit var currentFragment: Fragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var searchFragment: SearchMovieFragment
    private lateinit var favoriteFragment: FavoriteFragment
    private lateinit var accountFragment: AccountFragment
    private var fragmentManager = supportFragmentManager

    private lateinit var auth: FirebaseAuth
    private lateinit var appPreferences: AppPreferences
    private lateinit var dialog: Dialog
    var user: FirebaseUser? = null

    override fun getLayoutID(): Int {
        return R.layout.activity_main
    }

    override fun doViewCreated() {
        setupFragment()
        handleNavigationBottom()
    }

    private fun openDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_question_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.dialogQuestionLogin_tvDelete.setOnClickListener {
            val intentNewScreen = Intent(this, LoginActivity::class.java)
            startActivity(intentNewScreen)
            dialog.dismiss()
        }

        dialog.dialogQuestionLogin_tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun handleNavigationBottom() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    if (currentFragment === homeFragment)
                        fragmentManager.beginTransaction().show(homeFragment).commit()
                    else
                        fragmentManager.beginTransaction().hide(currentFragment).show(homeFragment)
                            .commit()
                    currentFragment = homeFragment
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.favouritesFragment -> {
                    if (user == null)
                        openDialog()
                    else {
                        if (currentFragment === favoriteFragment)
                            fragmentManager.beginTransaction().show(favoriteFragment).commit()
                        else
                            fragmentManager.beginTransaction().hide(currentFragment)
                                .show(favoriteFragment).commit()
                        currentFragment = favoriteFragment
                        return@setOnNavigationItemSelectedListener true
                    }
                }
                R.id.accountFragment -> {
                    if (user == null)
                        openDialog()
                    else {
                        if (currentFragment === accountFragment)
                            fragmentManager.beginTransaction().show(accountFragment).commit()
                        else
                            fragmentManager.beginTransaction().hide(currentFragment)
                                .show(accountFragment).commit()
                        currentFragment = accountFragment
                        return@setOnNavigationItemSelectedListener true
                    }
                }
                R.id.searchFragment -> {
                    if (currentFragment === searchFragment)
                        fragmentManager.beginTransaction().show(searchFragment).commit()
                    else
                        fragmentManager.beginTransaction().hide(currentFragment)
                            .show(searchFragment).commit()
                    currentFragment = searchFragment
                    return@setOnNavigationItemSelectedListener true
                }
            }
            true
        }
    }

    private fun setupFragment() {
        homeFragment = HomeFragment()
        searchFragment = SearchMovieFragment()
        accountFragment = AccountFragment()
        favoriteFragment = FavoriteFragment()

        fragmentManager.beginTransaction()
            .add(R.id.frameLayout, homeFragment, fragment::class.java.simpleName)
            .commit()

        fragmentManager.beginTransaction()
            .add(R.id.frameLayout, accountFragment, fragment::class.java.simpleName)
            .commit()
        fragmentManager.beginTransaction().hide(accountFragment).commit()

        fragmentManager.beginTransaction()
            .add(R.id.frameLayout, searchFragment, fragment::class.java.simpleName)
            .commit()
        fragmentManager.beginTransaction().hide(searchFragment).commit()

        fragmentManager.beginTransaction()
            .add(R.id.frameLayout, favoriteFragment, fragment::class.java.simpleName)
            .commit()
        fragmentManager.beginTransaction().hide(favoriteFragment).commit()

        currentFragment = homeFragment
    }

    fun hideBottom() {
        bottomNavigation.visibility = View.GONE
    }

    override fun onBackPressed() {
        val index = fragmentManager.backStackEntryCount - 1
        if (index >= 0) {
            super.onBackPressed()
            bottomNavigation.visibility = View.VISIBLE
        } else {
            val menuItem = bottomNavigation.menu.getItem(0)
            if (bottomNavigation.selectedItemId != menuItem.itemId) {

                if (currentFragment === homeFragment)
                    fragmentManager.beginTransaction().show(homeFragment).commit()
                else {
                    fragmentManager.beginTransaction().hide(currentFragment).show(homeFragment)
                        .commit()
                    currentFragment = homeFragment
                }
                bottomNavigation.selectedItemId = menuItem.itemId
            } else
                super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        appPreferences = AppPreferences(this)
        auth = Firebase.auth
        user = auth.currentUser
        user?.displayName?.let { appPreferences.setLoginUserName(it) }
        user?.email?.let { appPreferences.setLoginEmail(it) }
        user?.let { appPreferences.setLoginAvatar(it.photoUrl.toString()) }

//        Log.d("TESTABC", user?.displayName.toString())
    }
}