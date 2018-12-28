package pl.restaurant.restaurantmobile

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.content_info.*
import okhttp3.ResponseBody
import pl.restaurant.restaurantmobile.models.Times
import pl.restaurant.restaurantmobile.services.CreateTime
import pl.restaurant.restaurantmobile.services.UpdateTime
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class InfoActivity : AppCompatActivity() {

    private var i = 4
    val baseURL = MainActivity.baseURL
    private var mAuthTask: UserLoginTask? = null
    var isWorking = false
    private var header = ""
    private var startTime = ""
    private var loggingOut = false


    val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create()).build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)
        val userName = if (intent.getStringExtra("user_name").isEmpty())
            "Nieznajomy"
        else
            intent.getStringExtra("user_name")

        helloText.text = getString(R.string.hello, userName)
        isWorking = intent.getBooleanExtra("is_working", false)
        header = intent.getStringExtra("header")
        startTime = intent.getStringExtra("start_time")
        if (isWorking) {
            counterText.text = getString(R.string.stop_work)
            button.text = getString(R.string.stop_work)
        } else {
            counterText.text = getString(R.string.start_working)
            button.text = getString(R.string.start_working)
        }


        Thread {
            try {
                while (i > 0) {

                    Thread.sleep(1000)
                    if(loggingOut){i--}
                    var text : String = getString(R.string.start_working) ?: ""
                    if(isWorking)  text = getWorkedTime()

                    runOnUiThread {
                    counterText.text = text}
                }
            } catch (e: InterruptedException) {
            }
            finish()
        }.start()

        button.setOnClickListener {
            button.visibility = View.INVISIBLE
            mAuthTask = UserLoginTask()
            mAuthTask!!.execute(null as Void?)
            loggingOut = true
            Snackbar.make(button, getString(R.string.logout_in_moment), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.logout)) {
                        finish()
                    }.show()
        }


        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.logout_prompt), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.logout)) {
                        finish()
                    }.show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime() : String{
        val dateFormat =  SimpleDateFormat("HH:mm:ss")
        return dateFormat.format(Date())
    }

    fun getWorkedTime() : String{
        val currentTime = getCurrentTime()
        val currentTimeArray = currentTime.split(":")
        val workTime = startTime.split(":")

        var hoursCurrent = currentTimeArray[0].toInt()*3600
        val minutesCurrent = currentTimeArray[1].toInt()*60
        val secondsCurrent = currentTimeArray[2].toInt()

        val hoursWork = workTime[0].toInt()*3600
        val minutesWork = workTime[1].toInt()*60
        val secondsWork = workTime[2].toInt()

        if(hoursCurrent < hoursWork ){
            hoursCurrent += 24*3600
        }

        var secondsWorked = hoursCurrent+minutesCurrent+secondsCurrent-hoursWork-minutesWork
        -secondsWork

        val hours = secondsWorked / 3600
        secondsWorked -= (hours * 3600)
        val minutes = secondsWorked / 60
        secondsWorked -= (minutes * 60)
        val seconds = secondsWorked

        val diffTimeHours : String = if(hours < 10) "0$hours:" else "$hours:"
        val diffTimeMinutes = if(minutes < 10) "0$minutes:" else "$minutes:"
        val diffTimeSeconds = if(seconds < 10) "0$seconds" else "$seconds"

        return diffTimeHours+diffTimeMinutes+diffTimeSeconds

    }

    override fun onBackPressed() {
        Snackbar.make(button, getString(R.string.logout_prompt), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.logout)) { finish() }.show()
    }

    @SuppressLint("StaticFieldLeak")
    inner class UserLoginTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        var text = ""
        override fun doInBackground(vararg params: Void): Boolean? {
            val times = Times(getCurrentTime())
            val response : Response<ResponseBody>
            Log.d("Blad ", getCurrentTime())


            if (isWorking) {
                val userClient = retrofit.create(UpdateTime::class.java)
                val call = userClient.updateTime(header, times)
                response = call.execute()
            }

            else {
                val userClient = retrofit.create(CreateTime::class.java)
                val call = userClient.createTime(header, times)
                response = call.execute()
            }

            return try {
                if (response.isSuccessful)
                    if (response.body() != null) {
                    } else {
                        Log.d("Blad ", response.body()!!.string())
                    }
                else {
                    Log.d("Blad odpowiedzi", response.message())
                }
                true
            } catch (e: IOException) {
                Log.d("Blad komunikacji", e.message)
                false
            } catch (e: IllegalStateException) {
                Log.d("Blad danych", e.message)
                false
            }
        }

        override fun onPostExecute(success: Boolean?) {
            button.visibility = View.VISIBLE
            mAuthTask = null
            counterText.text = text

            if (success!!) {
                button.isEnabled = false
            } else {
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            button.visibility = View.VISIBLE
        }
    }
}
