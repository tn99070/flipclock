package infiapp.kidsgame.flipclockbook

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import infiapp.kidsgame.flipclockbook.databinding.ActivityMain2Binding
import java.util.Timer
import java.util.TimerTask

class MainActivity2 : AppCompatActivity() {

    private lateinit var bind: ActivityMain2Binding

    private val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bind = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(bind.root)


        // tick every second
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() = runOnUiThread { bind.flipClock.updateTime() }
        }, 0, 1_000)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}