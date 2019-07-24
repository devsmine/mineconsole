package com.devsmine.mineconsole

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Console like output view, which allows writing via static console methods
 * from anywhere of application.
 * If you want to see the output, you should use console in any of your layouts,
 * all calls to console static write methods will affect all instantiated consoles.
 */
open class Console : FrameLayout {

    companion object {

        @JvmStatic
        fun writeLine(scrollUpPosition: Boolean=false) {
            controller.writeLine(scrollUpPosition)
        }

        /**
         * Write provided object String representation to console and starts new line
         * "null" is written if the object is null
         *
         * @param o Object to write
         */
        @JvmStatic
        fun writeLine(o: Any?, scrollUpPosition: Boolean=false) {
            controller.writeLine(o,scrollUpPosition)
        }

        /**
         * Write Spannable to console and starts new line
         * "null" is written if the object is null
         *
         * @param spannableString SpannableString to write
         */
        @JvmStatic
        fun writeLine(spannableString: SpannableString?, scrollUpPosition: Boolean=false) {
            controller.writeLine(spannableString,scrollUpPosition)
        }

        /**
         * Write provided object String representation to console
         * "null" is written if the object is null
         *
         * @param o Object to write
         */
        @JvmStatic
        fun write(o: Any? , scrollUpPosition: Boolean=false) {
            controller.write(o,scrollUpPosition)
        }

        /**
         * Write SpannableString to the console
         * "null" is written if the object is null
         *
         * @param spannableString SpannableString to write
         */
        @JvmStatic
        fun write(spannableString: SpannableString?,scrollUpPosition: Boolean=false) {
            controller.write(spannableString,scrollUpPosition)
        }

        /**
         * Clears the console text
         */
        @JvmStatic
        fun clear(scrollUpPosition: Boolean=false) {
            controller.clear(scrollUpPosition)
        }

        @JvmStatic
        fun consoleCount(): Int {
            return controller.size()
        }

        internal val controller = ConsoleController()
    }

    private val textView: TextView
    private val scrollView: ScrollView
    private val userTouchingListener = UserTouchingListener()
    private val flingProperty: FlingProperty
    private val scrollDownRunnable = Runnable { scrollFullDown() }
    private val scrollUpRunnable = Runnable { scrollFullUp() }

    val text: CharSequence
        get() = textView.text.toString()

    // Fields are used to not schedule more than one runnable for scroll down
    private var fullScrollScheduled: Boolean = false

    private fun isUserInteracting(): Boolean {
        return userTouchingListener.isUserTouching || flingProperty.isFlinging
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    init {
        controller.add(this)

        LayoutInflater.from(context).inflate(R.layout.console_content, this)

        textView = findViewById(R.id.console_text)
        scrollView = findViewById(R.id.console_scroll_view)
        flingProperty = FlingProperty.create(scrollView)
        scrollView.setOnTouchListener(userTouchingListener)

        printBuffer()
        // need to have extra post here, because scroll view is fully initialized after another frame
        post { scrollDown() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        fullScrollScheduled = false
    }

    internal fun printScroll(scrollUpPosition: Boolean=false) {
        printBuffer()
        if (scrollUpPosition){
            scrollUp()
        }else{
            scrollDown()
        }

    }

    private fun printBuffer() {
        controller.printTo(textView)
    }

    private fun scrollDown() {
        if (!isUserInteracting() && !fullScrollScheduled) {
            post(scrollDownRunnable)
            fullScrollScheduled = true
        }
    }

    private fun scrollUp() {
        if (!isUserInteracting() && !fullScrollScheduled) {
            post(scrollUpRunnable)
            fullScrollScheduled = true
        }
    }

    private fun scrollFullDown() {
        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    private fun scrollFullUp() {
        scrollView.fullScroll(View.FOCUS_UP)
    }
}
