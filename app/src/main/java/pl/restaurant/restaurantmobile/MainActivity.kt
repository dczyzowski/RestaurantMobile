package pl.restaurant.restaurantmobile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import pl.restaurant.restaurantmobile.database.UsersDatabase
import pl.restaurant.restaurantmobile.fragments.UserListFragment
import pl.restaurant.restaurantmobile.models.User
import pl.restaurant.restaurantmobile.services.GetTime
import pl.restaurant.restaurantmobile.services.GetUser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


/**
 * A login screen that offers login via email/password.
*/


class MainActivity : AppCompatActivity(), UserListFragment.OnListFragmentInteractionListener {

    override fun onListFragmentInteraction(item: pl.restaurant.restaurantmobile.database.User) {
        mAuthTask = UserLoginTask(null, null, item.authHeader)
        mAuthTask!!.execute(null as Void?)
        Snackbar.make(cardView, "Loguję jako: " + item.firstName, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    companion object {
        const val baseURL = "https://resto-worker-api.herokuapp.com/"
    }

    var db : UsersDatabase? = null
    private var mAuthTask: UserLoginTask? = null


    val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create()).build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set up the login form.


        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                //attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
        password.setOnEditorActionListener { v, actionId, event ->
            attemptLogin()
            true
        }

    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(emailStr, passwordStr, null)
            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length >= 3
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class UserLoginTask internal constructor(mUsername: String?, mPassword: String?, header: String?) : AsyncTask<Void, Void, User>() {

        val base = "$mUsername:$mPassword"
        private var authHeader = header ?: "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        private var startTime = ""
        private var isWorking = false
        var internetConnectionWorking = true
        var userInformationIsValid = false
        var userIsHired = false

        override fun doInBackground(vararg params: Void): User? {
            val getUser = retrofit.create(GetUser::class.java)
            val callGetUser = getUser.getUser(authHeader)
            val user = User()

            try {
                val response = callGetUser.execute()
                if (response.isSuccessful) {
                    val username = response.body()!!.username
                    userInformationIsValid = username.isNotEmpty()
                    if(userInformationIsValid){
                        userIsHired = response.body()!!.isActive
                        if(userIsHired) {
                            user.username = username
                            user.isActive = userIsHired
                            user.firstName = response.body()!!.firstName

                            db = UsersDatabase.getInstance(this@MainActivity)
                            if (db!!.userDao().loadUser(user.username).isEmpty())
                                db!!.userDao().insertAll(pl.restaurant.restaurantmobile.database.User(
                                        user.hashCode(), username, user.firstName, authHeader, null)
                                )
                        }
                    } else return null
                }else return null
            } catch (e: IOException) {
                internetConnectionWorking = false
                Log.d("Blad komunikacji", e.message)
                return null
            } catch (e: IllegalStateException) {
                internetConnectionWorking = false
                Log.d("Blad danych", e.message)
                return null
            }

            val getTime = retrofit.create(GetTime::class.java)
            val callGetTime = getTime.getTimes(authHeader)

            return try {
                val response = callGetTime.execute()
                isWorking = response.isSuccessful
                if(isWorking){
                    startTime = response.body()!!.startTime
                }
                user
            }catch (e:IOException){
                Log.d("Blad komunikacji", e.message)
                null
            }catch (e:IllegalStateException){
                Log.d("Blad danych", e.message)
                null
            }

        }

        override fun onPostExecute(user: User?) {
            mAuthTask = null
            showProgress(false)

            if (user != null) {
                val i = Intent(baseContext, InfoActivity::class.java)

                i.putExtra("user_name", user.firstName)
                i.putExtra("is_working", isWorking)
                i.putExtra("header", authHeader)
                i.putExtra("start_time", startTime)
                startActivity(i)

            } else {
                //TODO WHY... SET ERRORS
                if(!internetConnectionWorking) {
                    Toast.makeText(applicationContext, R.string.internet_error, Toast.LENGTH_LONG)
                            .show()
                }
                else{
                    if (!userInformationIsValid) {
                        password.error = getString(R.string.error_incorrect_password)
                        password.requestFocus()
                    }
                }
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    override fun onResume() {
        super.onResume()
        email.text = null
        password.text = null
    }
}
