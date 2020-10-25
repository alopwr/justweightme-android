package io.github.gladko.justweight.ui.main.home

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter

object BindingUtils {
    @BindingAdapter("android:text")
    fun setFloat(view: TextView, value:Float) {
        if (java.lang.Float.isNaN(value))
            view.setText("")
        else
            view.setText(value.toString())
    }
    @InverseBindingAdapter(attribute = "android:text")
    fun getFloat(view:TextView):Float {
        val num = view.getText().toString()
        if (num.isEmpty()) return 0.0f
        try
        {
            return java.lang.Float.parseFloat(num)
        }
        catch (e:NumberFormatException) {
            return 0.0f
        }
    }
}