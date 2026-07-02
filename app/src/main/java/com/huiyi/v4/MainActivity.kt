package com.huiyi.v4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.huiyi.v4.ui.HuiyiRoot
import com.huiyi.v4.ui.theme.HuiyiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuiyiTheme {
                HuiyiRoot()
            }
        }
    }
}
