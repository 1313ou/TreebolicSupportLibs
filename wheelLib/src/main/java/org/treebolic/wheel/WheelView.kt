/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes

/**
 * Default Wheel View
 */
class WheelView : WheelVerticalView {

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)
}
