package com.onimaskesi.speechrecognitionhmsdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants




class MainActivity : AppCompatActivity() {


    private var mTextView: TextView? = null


    var handler: Handler = Handler(object : Handler.Callback {
        //@SuppressLint("LongLogTag")
        override fun handleMessage(message: Message): Boolean {
            when (message.what) {
                HANDLE_CODE -> {
                    val text: String = message.getData().getString(HANDLE_KEY)!!
                    mTextView!!.text = """ $text """.trimIndent()
                    //Log.e(TAG, text)
                }
                else -> { }
            }
            return false
        }
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.mTextView = this.findViewById(R.id.textView)

        if (allPermissionsGranted()) {

            //mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this)

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }


    private fun displayResult(str: String) {
        val msg = Message()
        val data = Bundle()
        data.putString(HANDLE_KEY, str)
        msg.data = data
        msg.what = HANDLE_CODE
        handler.sendMessage(msg)
    }


    fun voiceInputClick(view : View){

        openMicrophoneForListenAndGetText()

    }

    fun openMicrophoneForListenAndGetText(){
        val intent = Intent(this, MLAsrCaptureActivity::class.java)
            .putExtra(MLAsrCaptureConstants.LANGUAGE,"en-US")
            .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX)
        startActivityForResult(intent, ML_ASR_CAPTURE_CODE)
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent
    ): Boolean {
        // This is the center button for headphones
        if (event.getKeyCode() === KeyEvent.KEYCODE_HEADSETHOOK) {

            //Toast.makeText(this, "BUTTON PRESSED!", Toast.LENGTH_SHORT).show()
            openMicrophoneForListenAndGetText()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                //mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this)

            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        var text = ""
        if (null == data) {
            displayResult("Intent data is null.")
        }
        if (requestCode == ML_ASR_CAPTURE_CODE) {
            when (resultCode) {
                MLAsrCaptureConstants.ASR_SUCCESS -> if (data != null) {
                    val bundle = data.extras
                    if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                        text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT)!!
                    }
                    if (text == null || "" == text) {
                        text = "Result is null."
                    }
                    displayResult(text)
                }
                MLAsrCaptureConstants.ASR_FAILURE -> {
                    if (data != null) {
                        val bundle = data.extras
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                            text = text + bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE)
                        }
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                            val errorMsg =
                                bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)
                            if (errorMsg != null && "" != errorMsg) {
                                text = "[$text]$errorMsg"
                            }
                        }
                    }
                    displayResult(text)
                    displayResult("Failure.")
                }
                else -> displayResult("Failure.")
            }
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )
        private val TAG = "SpeechRecognitionActivity"
        private val HANDLE_CODE = 0
        private val HANDLE_KEY = "text"
        private val ML_ASR_CAPTURE_CODE = 2

    }
}