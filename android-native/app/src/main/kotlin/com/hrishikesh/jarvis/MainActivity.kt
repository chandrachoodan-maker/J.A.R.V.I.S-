package com.hrishikesh.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
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
    private lateinit var coreView: JarvisCoreView
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
            setPadding(dp(20), dp(24), dp(20), dp(18))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(3, 9, 20), Color.rgb(7, 20, 36), Color.rgb(3, 9, 20))
            )
        }
        val scroll = ScrollView(this).apply { clipToPadding = false }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        content.addView(TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 29f
            letterSpacing = 0.24f
            typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
            setTextColor(Color.rgb(117, 229, 255))
            gravity = Gravity.CENTER
        }, matchWrap())
        content.addView(TextView(this).apply {
            text = "JUST A RATHER VERY INTELLIGENT SYSTEM"
            textSize = 9f
            letterSpacing = 0.16f
            setTextColor(Color.rgb(112, 150, 174))
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(22))
        }, matchWrap())

        coreView = JarvisCoreView(this)
        val orbWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
            addView(coreView, LinearLayout.LayoutParams(dp(190), dp(190)))
        }
        content.addView(orbWrap, matchWrap())

        status = TextView(this).apply {
            text = "●  SYSTEM ONLINE"
            textSize = 11f
            letterSpacing = 0.14f
            setTextColor(Color.rgb(103, 231, 205))
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedPanel(Color.rgb(9, 35, 48), Color.rgb(31, 104, 125), 24)
        }
        content.addView(status, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })

        val assistantCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = roundedPanel(Color.rgb(10, 24, 41), Color.rgb(26, 65, 91), 18)
        }
        assistantCard.addView(TextView(this).apply {
            text = "PERSONAL ASSISTANT"
            textSize = 9f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(92, 190, 220))
        }, matchWrap())
        transcript = TextView(this).apply {
            text = "Good day, Sir.\nHow may I assist you?"
            textSize = 17f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, dp(2))
            gravity = Gravity.CENTER_VERTICAL
        }
        assistantCard.addView(transcript, matchWrap())
        content.addView(assistantCard, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })

        content.addView(sectionLabel("COMMAND CONSOLE"), matchWrap())
        command = EditText(this).apply {
            hint = "Type a command, Sir..."
            setHintTextColor(Color.rgb(111, 139, 158))
            setTextColor(Color.WHITE)
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = roundedPanel(Color.rgb(10, 24, 40), Color.rgb(35, 84, 111), 14)
        }
        content.addView(command, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        content.addView(makeButton("⌁   EXECUTE COMMAND") { handleCommand(command.text.toString()) }, LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(9) })
        content.addView(makeButton("◉   ACTIVATE VOICE") { listen() }, LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(20) })

        content.addView(TextView(this).apply {
            text = "J.A.R.V.I.S.  •  ANDROID SYSTEMS\nVoice recognition availability depends on your phone's speech service."
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(93, 125, 148))
            setPadding(dp(8), dp(12), dp(8), dp(14))
        }, matchWrap())

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun roundedPanel(fill: Int, stroke: Int, radius: Int): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }

    private fun sectionLabel(label: String) = TextView(this).apply {
        text = label
        textSize = 10f
        letterSpacing = 0.18f
        setTextColor(Color.rgb(91, 190, 221))
        setPadding(dp(2), 0, 0, dp(10))
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 12f
        letterSpacing = 0.08f
        setTextColor(Color.rgb(220, 246, 255))
        background = roundedPanel(Color.rgb(12, 39, 59), Color.rgb(37, 112, 145), 13)
        stateListAnimator = null
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
        coreView.setMode(JarvisCoreView.Mode.SPEAKING)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
        status.postDelayed({ status.text = "SYSTEM READY"; coreView.setMode(JarvisCoreView.Mode.IDLE) }, 1800)
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
            override fun onReadyForSpeech(params: Bundle?) { status.text = "LISTENING · SPEAK NOW"; coreView.setMode(JarvisCoreView.Mode.LISTENING) }
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() { status.text = "PROCESSING"; coreView.setMode(JarvisCoreView.Mode.THINKING) }
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
                speak("I can open supported apps by command, tell the time and date, set timers, and search the web when you explicitly ask. I do not yet have an AI knowledge engine connected.")
            else -> speak("I cannot answer that yet, Sir. My knowledge engine is not connected. Say search for, followed by your question, if you would like me to look it up online.")
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


private class JarvisCoreView(context: android.content.Context) : View(context) {
    enum class Mode { IDLE, LISTENING, THINKING, SPEAKING }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle = 0f
    private var mode = Mode.IDLE
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 9000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { angle = it.animatedValue as Float; invalidate() }
        start()
    }
    fun setMode(next: Mode) { mode = next; invalidate() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f; val cy = height / 2f
        val radius = minOf(width, height) * 0.39f
        val cyan = when (mode) {
            Mode.LISTENING -> Color.rgb(72, 255, 202)
            Mode.THINKING -> Color.rgb(170, 120, 255)
            Mode.SPEAKING -> Color.rgb(85, 205, 255)
            Mode.IDLE -> Color.rgb(70, 205, 245)
        }
        paint.style = Paint.Style.FILL; paint.color = Color.rgb(5, 25, 43)
        paint.setShadowLayer(28f, 0f, 0f, cyan); canvas.drawCircle(cx, cy, radius * 0.69f, paint); paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 2.4f; paint.color = cyan
        canvas.drawCircle(cx, cy, radius * 0.72f, paint)
        paint.strokeWidth = 1.5f; paint.color = Color.argb(120, 100, 220, 255)
        canvas.drawCircle(cx, cy, radius * 0.88f, paint)
        val oval = RectF(cx-radius, cy-radius, cx+radius, cy+radius)
        paint.strokeWidth = 4f; paint.color = cyan
        canvas.drawArc(oval, angle, 92f, false, paint); canvas.drawArc(oval, angle + 180f, 52f, false, paint)
        val inner = RectF(cx-radius*0.58f, cy-radius*0.58f, cx+radius*0.58f, cy+radius*0.58f)
        paint.strokeWidth = 1.4f; paint.color = Color.argb(180, 125, 230, 255)
        canvas.save(); canvas.rotate(-angle * 1.6f, cx, cy)
        canvas.drawArc(inner, 0f, 250f, false, paint); canvas.drawArc(inner, 180f, 70f, false, paint); canvas.restore()
        paint.style = Paint.Style.FILL; paint.color = Color.WHITE; paint.setShadowLayer(18f, 0f, 0f, cyan)
        canvas.drawCircle(cx, cy, radius * 0.16f, paint); paint.clearShadowLayer()
        paint.color = cyan; canvas.drawCircle(cx, cy, radius * 0.075f, paint)
    }
    override fun onDetachedFromWindow() { animator.cancel(); super.onDetachedFromWindow() }
}
