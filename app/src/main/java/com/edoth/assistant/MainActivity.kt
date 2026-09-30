package com.edoth.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var heard: TextView
    private lateinit var speakButton: Button
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        heard = findViewById(R.id.heard)
        speakButton = findViewById(R.id.speakButton)

        tts = TextToSpeech(this, this)

        speakButton.setOnClickListener {
            startListening()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition tidak tersedia."
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : SimpleRecognitionListener() {
                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "Mendengarkan..."
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?: return

                    heard.text = "Kamu: $text"
                    answer(text)
                }

                override fun onError(error: Int) {
                    status.text = "Coba lagi."
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer?.startListening(intent)
    }

    private fun answer(text: String) {
        val lower = text.lowercase(Locale.getDefault())
        val response = when {
            "halo" in lower || "hai" in lower ->
                "Halo! Saya EDOTH Assistant. Ada yang bisa saya bantu?"
            "siapa kamu" in lower ->
                "Saya EDOTH Assistant."
            "jam" in lower ->
                "Untuk saat ini saya masih dalam tahap awal pengembangan."
            else ->
                "Saya mendengar: $text. Fitur AI akan kita sambungkan pada tahap berikutnya."
        }

        status.text = "Menjawab..."
        speak(response)
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EDOTH_RESPONSE")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
