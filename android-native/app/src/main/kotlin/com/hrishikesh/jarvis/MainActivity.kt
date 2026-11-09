package com.hrishikesh.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val bg = Color.rgb(5, 10, 18)
    private val panel = Color.rgb(13, 24, 39)
    private val cyan = Color.rgb(77, 211, 232)
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var command: EditText
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        tts = TextToSpeech(this, this)
        buildScreen()
        requestAudioPermissionIfNeeded()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 18)
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        content.addView(TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 30f
            letterSpacing = 0.18f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }, matchWrap())
        content.addView(TextView(this).apply {
            text = "PERSONAL ASSISTANT  ·  ANDROID EDITION"
            textSize = 10f
            letterSpacing = 0.12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }, matchWrap())
        status = TextView(this).apply {
            text = "SYSTEM READY"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(12, 16, 12, 16)
            setBackgroundColor(panel)
        }
        content.addView(status, matchWrap())
        transcript = TextView(this).apply {
            text = "Good day, Sir. JARVIS is ready."
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(4, 24, 4, 24)
            gravity = Gravity.CENTER
        }
        content.addView(transcript, matchWrap())
        command = EditText(this).apply {
            hint = "Give me a command, Sir…"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
            setSingleLine(true)
            setPadding(16, 14, 16, 14)
            setBackgroundColor(panel)
        }
        content.addView(command, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 })
        content.addView(makeButton("EXECUTE COMMAND") { handleCommand(command.text.toString()) }, matchWrap())
        content.addView(makeButton("SPEAK TO JARVIS") { listen() }, matchWrap())
        content.addView(TextView(this).apply {
            text = "QUICK ACCESS"
            setTextColor(cyan)
            textSize = 11f
            letterSpacing = 0.16f
            setPadding(0, 26, 0, 12)
        }, matchWrap())
        val apps = listOf(
            "WHATSAPP" to "com.whatsapp",
            "YOUTUBE" to "com.google.android.youtube",
            "CHROME" to "com.android.chrome",
            "MAPS" to "com.google.android.apps.maps",
            "GMAIL" to "com.google.android.gm",
            "SPOTIFY" to "com.spotify.music"
        )
        apps.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { (label, pkg) ->
                row.addView(makeButton(label) { launchPackage(pkg, label) }, LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                    marginEnd = dp(6); bottomMargin = dp(8)
                })
            }
            if (pair.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, dp(52), 1f))
            content.addView(row, matchWrap())
        }
        content.addView(makeButton("CAMERA") { openCamera() }, matchWrap())
        content.addView(makeButton("PHONE SETTINGS") { openSettings() }, matchWrap())
        content.addView(makeButton("STOP TIMER / SPEECH") {
            timer?.cancel(); timer = null
            tts?.stop()
            speak("Cancelled, Sir.")
        }, matchWrap())
        content.addView(TextView(this).apply {
            text = "Voice recognition uses your phone's speech service. Some commands require internet. JARVIS only opens apps and settings you explicitly request; it does not silently control other apps."
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(4, 22, 4, 8)
        }, matchWrap())
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 13f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(20, 53, 72))
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale.UK
            tts?.setSpeechRate(0.90f)
            tts?.setPitch(0.82f)
        }
    }

    private fun speak(text: String) {
        transcript.text = text
        status.text = "SPEAKING"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
        status.postDelayed({ status.text = "SYSTEM READY" }, 1800)
    }

    private fun requestAudioPermissionIfNeeded() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 41)
        }
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermissionIfNeeded()
            speak("Please grant microphone permission, Sir.")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is not available on this device. You can type a command instead.")
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "LISTENING · SPEAK NOW" }
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() { status.text = "PROCESSING" }
            override fun onError(error: Int) {
                status.text = "SYSTEM READY"
                speak("I didn't catch that, Sir. Please try again.")
            }
            override fun onResults(results: Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                command.setText(phrase)
                handleCommand(phrase)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onRmsChanged(rmsdB: Float) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try { recognizer?.startListening(intent) }
        catch (_: Exception) { speak("Voice input could not start. Please type your command.") }
    }

    private fun handleCommand(raw: String) {
        val text = raw.trim()
        val q = text.lowercase(Locale.ROOT)
        if (q.isBlank()) { speak("Yes, Sir?"); return }
        when {
            q.contains("time") && (q.contains("what") || q == "time" || q.contains("tell")) ->
                speak("The time is " + SimpleDateFormat("h:mm a", Locale.UK).format(Date()) + ", Sir.")
            q.contains("date") || q.contains("today") ->
                speak("Today is " + SimpleDateFormat("EEEE, d MMMM yyyy", Locale.UK).format(Date()) + ", Sir.")
            q.startsWith("set a timer") || q.startsWith("set timer") || q.startsWith("timer") -> setTimer(q)
            q.contains("open whatsapp") || q.contains("launch whatsapp") -> launchPackage("com.whatsapp", "WhatsApp")
            q.contains("open youtube") || q.contains("launch youtube") -> launchPackage("com.google.android.youtube", "YouTube")
            q.contains("open chrome") || q.contains("open browser") -> launchPackage("com.android.chrome", "Chrome")
            q.contains("open maps") || q.contains("open google maps") -> launchPackage("com.google.android.apps.maps", "Maps")
            q.contains("open gmail") -> launchPackage("com.google.android.gm", "Gmail")
            q.contains("open spotify") -> launchPackage("com.spotify.music", "Spotify")
            q.contains("open camera") || q.contains("launch camera") -> openCamera()
            q.contains("open settings") || q.contains("phone settings") -> openSettings()
            q.contains("stop speaking") || q == "stop" || q.contains("cancel timer") -> {
                timer?.cancel(); timer = null; tts?.stop(); speak("Stopped, Sir.")
            }
            q.startsWith("search for ") || q.startsWith("google ") || q.startsWith("look up ") -> {
                val query = text.replace(Regex("(?i)^(search for|google|look up)\\s*"), "")
                searchWeb(query)
            }
            q.contains("who are you") || q.contains("your name") ->
                speak("I am JARVIS, your personal Android assistant. How may I help you, Sir?")
            q.contains("help") || q.contains("what can you do") ->
                speak("I can open supported apps, tell you the time and date, set a timer, search the web, and open phone settings. More capabilities can be added.")
            else -> searchWeb(text)
        }
    }

    private fun setTimer(q: String) {
        val match = Regex("(\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours)").find(q)
        if (match == null) {
            speak("Please say a duration in digits, for example, set a timer for 5 minutes.")
            return
        }
        val amount = match.groupValues[1].toLongOrNull() ?: return
        val unit = match.groupValues[2]
        val millis = amount * when {
            unit.startsWith("hour") -> 3_600_000L
            unit.startsWith("min") -> 60_000L
            else -> 1_000L
        }
        timer?.cancel()
        speak("Timer set for $amount $unit, Sir.")
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                status.text = "TIMER · " + String.format(Locale.UK, "%02d:%02d", millisUntilFinished / 60000, (millisUntilFinished / 1000) % 60)
            }
            override fun onFinish() {
                status.text = "TIMER COMPLETE"
                speak("Your timer is complete, Sir.")
            }
        }.start()
    }

    private fun launchPackage(pkg: String, label: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(pkg)
            if (intent == null) { speak("$label is not installed or cannot be opened on this phone, Sir."); return }
            startActivity(intent)
            speak("Opening $label, Sir.")
        } catch (_: Exception) { speak("I couldn't open $label, Sir.") }
    }

    private fun openCamera() {
        try { startActivity(Intent("android.media.action.IMAGE_CAPTURE")); speak("Opening the camera, Sir.") }
        catch (_: Exception) { speak("I couldn't open the camera, Sir.") }
    }

    private fun openSettings() {
        try { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)); speak("Opening phone settings, Sir.") }
        catch (_: Exception) { speak("I couldn't open settings, Sir.") }
    }

    private fun searchWeb(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
        try { startActivity(intent); speak("Searching the web for that, Sir.") }
        catch (_: Exception) { speak("I couldn't open a browser, Sir.") }
    }

    override fun onDestroy() {
        timer?.cancel()
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
