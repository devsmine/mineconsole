package com.devsmine.mineconsole

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableString
import android.widget.TextView

import java.lang.ref.WeakReference
import java.util.ArrayList

internal class ConsoleController {

    val consoles: MutableList<WeakReference<Console>> = ArrayList()
    val buffer = ConsoleBuffer()

    private val printBufferHandler: Handler by lazy { PrintBufferHandler(this) }

    private val isUIThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    fun add(console: Console) {
        consoles.add(WeakReference(console))
    }

    fun writeLine(scrollUpPosition:Boolean=false) {
        write(END_LINE,scrollUpPosition)
    }

    fun writeLine(o: Any?,scrollUpPosition:Boolean=false) {

        if(scrollUpPosition){

            buffer.prepend(o).prepend(END_LINE)
            scheduleBufferPrint(scrollUpPosition)
        }else{
            buffer.append(o).append(END_LINE)
            scheduleBufferPrint(scrollUpPosition)
        }



    }

    fun write(spannableString: SpannableString?,scrollUpPosition:Boolean=false) {

        if(scrollUpPosition){
            buffer.prepend(spannableString)
            scheduleBufferPrint(scrollUpPosition)
        }else{
            buffer.append(spannableString)
            scheduleBufferPrint(scrollUpPosition)
        }


    }

    fun writeLine(spannableString: SpannableString?,scrollUpPosition:Boolean=false) {

        if(scrollUpPosition){
            buffer.prepend(spannableString).prepend(END_LINE)
            scheduleBufferPrint(scrollUpPosition)
        }else{
            buffer.append(spannableString).append(END_LINE)
            scheduleBufferPrint(scrollUpPosition)
        }


    }

    fun write(o: Any?,scrollUpPosition:Boolean=false) {

        if(scrollUpPosition){
            buffer.prepend(o)
            scheduleBufferPrint(scrollUpPosition)
        }else{
            buffer.append(o)
            scheduleBufferPrint(scrollUpPosition)
        }
    }

    fun clear(scrollUpPosition:Boolean=false) {
        buffer.clear()
        scheduleBufferPrint(scrollUpPosition)
    }

    fun scheduleBufferPrint(scrollUpPosition:Boolean=false) {
        runBufferPrint(scrollUpPosition)
    }

    fun size(): Int {
        return consoles.size
    }

    fun printTo(text: TextView) {
        buffer.printTo(text)
    }

    private fun runBufferPrint(scrollUpPosition: Boolean=false) {
        if (!isUIThread) {
            if (!printBufferHandler.hasMessages(PRINT_BUFFER)) {
                printBufferHandler.obtainMessage(PRINT_BUFFER).sendToTarget()
            }
            return
        }

        val iterator = consoles.iterator()
        while (iterator.hasNext()) {
            val console = iterator.next().get()
            if (console == null) {
                iterator.remove()
            } else {
                console.printScroll(scrollUpPosition)
            }
        }
    }

    private class PrintBufferHandler(val controller: ConsoleController) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == PRINT_BUFFER) {
                controller.runBufferPrint()
            }
        }
    }

    companion object {
        val END_LINE = "\n"
        const val PRINT_BUFFER = 653276
    }
}
