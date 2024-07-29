/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.database.DataSetObserver
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import org.treebolic.wheel.WheelScroller.ScrollingListener
import org.treebolic.wheel.adapters.WheelViewAdapter
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract spinner spinnerwheel view. This class should be subclassed.
 *
 * @param context  the application environment.
 * @param attrs    a collection of attributes.
 * @param defStyle The default style to apply to this view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
abstract class AbstractWheel(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) : View(context, attrs) {

    /**
     * Current value
     */
    var currentItem: Int
        get() = currentItemIdx
        /**
         * Sets the current item w/o animation. Does nothing when index is wrong.
         *
         * @param index the item index
         */
        set(index) {
            setCurrentItem(index, false)
        }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index0   the item index
     * @param animated the animation flag
     */
    private fun setCurrentItem(index0: Int, animated: Boolean) {
        var index = index0
        if (viewAdapter == null || viewAdapter!!.itemsCount == 0) {
            return  // throw?
        }

        val itemCount = viewAdapter!!.itemsCount
        if (index < 0 || index >= itemCount) {
            if (isCyclic) {
                while (index < 0) {
                    index += itemCount
                }
                index %= itemCount
            } else {
                return  // throw?
            }
        }
        if (index != currentItemIdx) {
            if (animated) {
                var itemsToScroll = index - currentItemIdx
                if (isCyclic) {
                    val scroll = (itemCount + min(index.toDouble(), currentItemIdx.toDouble()) - max(index.toDouble(), currentItemIdx.toDouble())).toInt()
                    if (scroll < abs(itemsToScroll.toDouble())) {
                        itemsToScroll = if (itemsToScroll < 0) scroll else -scroll
                    }
                }
                scroll(itemsToScroll, 0)
            } else {
                scrollingOffset = 0
                val old = currentItemIdx
                currentItemIdx = index
                notifyChangingListeners(old, currentItemIdx)
                invalidate()
            }
        }
    }

    /**
     * Current value index
     */
    @JvmField
    protected var currentItemIdx: Int = 0

    /** The index of first item in layout */
    @JvmField
    protected var firstItemIdx: Int = 0

    /**
     * Count of visible items
     * Actual amount of visible items depends on spinnerwheel layout parameters. To apply changes and rebuild view call
     * measure().
     */
    var visibleItems: Int = 0

    /**
     * Should all items be visible
     */
    private var isAllVisible: Boolean = false

    /**
     * Whether spinnerwheel is cyclic. That means before the 1st item there is shown the last one
     */
    var isCyclic: Boolean = false
        set(isCyclic) {
            field = isCyclic
            invalidateItemsLayout(false)
        }

    // Adapter

    /**
     * View adapter
     */
    var viewAdapter: WheelViewAdapter? = null
        /**
         * Sets view adapter. Usually new adapters contain different views, so it needs to rebuild view by calling measure().
         */
        set(adapter) {
            if (field != null) {
                field!!.unregisterDataSetObserver(dataObserver!!)
            }
            field = adapter
            if (field != null) {
                field!!.registerDataSetObserver(dataObserver!!)
            }
            invalidateItemsLayout(true)
        }

    // Scrolling

    protected var scroller: WheelScroller? = null

    protected var isScrollingPerformed: Boolean = false

    @JvmField
    protected var scrollingOffset: Int = 0

    // Items layout

    @JvmField
    protected var itemsLayout: LinearLayout? = null

    private var layoutHeight: Int = 0

    private var layoutWidth: Int = 0

    // Recycler

    private val recycler = WheelRecycler(this)

    // Listeners

    private val changingListeners: MutableList<(AbstractWheel, Int, Int) -> Unit> = LinkedList()

    private val scrollingListeners: MutableList<OnWheelScrollListener> = LinkedList()

    private val clickingListeners: MutableList<(wheel: AbstractWheel, itemIndex: Int) -> Unit> = LinkedList()

    // Data listener

    private var dataObserver: DataSetObserver? = null

    init {
        initAttributes(context, attrs, defStyle)
        initData(context)
    }

    // I N I T I A T I N G   D A T A   A N D   A S S E T S    A T   S T A R T   U P

    /**
     * Initiates data and parameters from styles
     *
     * @param context  the application environment.
     * @param attrs    a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    protected open fun initAttributes(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.AbstractWheel, defStyle, 0).use {
                visibleItems = it.getInt(R.styleable.AbstractWheel_visibleItems, DEF_VISIBLE_ITEMS)
                isAllVisible = it.getBoolean(R.styleable.AbstractWheel_isAllVisible, false)
                isCyclic = it.getBoolean(R.styleable.AbstractWheel_isCyclic, DEF_IS_CYCLIC)
            }
        }
    }

    /**
     * Initiates data
     *
     * @param context the context
     */
    protected open fun initData(context: Context) {
        dataObserver = object : DataSetObserver(
        ) {
            override fun onChanged() {
                invalidateItemsLayout(false)
            }

            override fun onInvalidated() {
                invalidateItemsLayout(true)
            }
        }

        // creating new scroller
        scroller = createScroller(object : ScrollingListener {
            override fun onStarted() {
                this@AbstractWheel.isScrollingPerformed = true
                notifyScrollingListenersAboutStart()
                onScrollStarted()
            }

            override fun onTouch() {
                onScrollTouched()
            }

            override fun onTouchUp() {
                if (!this@AbstractWheel.isScrollingPerformed) {
                    onScrollTouchedUp() // if scrolling IS performed, whe should use onFinished instead
                }
            }

            override fun onScroll(distance: Int) {
                doScroll(distance)

                val dimension: Int = baseDimension
                if (this@AbstractWheel.scrollingOffset > dimension) {
                    this@AbstractWheel.scrollingOffset = dimension
                    scroller!!.stopScrolling()
                } else if (this@AbstractWheel.scrollingOffset < -dimension) {
                    this@AbstractWheel.scrollingOffset = -dimension
                    scroller!!.stopScrolling()
                }
            }

            override fun onFinished() {
                if (this@AbstractWheel.isScrollingPerformed) {
                    notifyScrollingListenersAboutEnd()
                    this@AbstractWheel.isScrollingPerformed = false
                    onScrollFinished()
                }

                this@AbstractWheel.scrollingOffset = 0
                invalidate()
            }

            override fun onJustify() {
                if (abs(scrollingOffset.toDouble()) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                    scroller!!.scroll(this@AbstractWheel.scrollingOffset, 0)
                }
            }
        })
    }

    public override fun onSaveInstanceState(): Parcelable {
        // begin boilerplate code that allows parent classes to save state
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)

        // end
        ss.currentItem = currentItem

        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        // begin boilerplate code so parent classes can restore state
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        // end
        currentItemIdx = state.currentItem

        // dirty hack to re-draw child items correctly
        postDelayed({ invalidateItemsLayout(false) }, 100)
    }

    internal class SavedState : BaseSavedState {

        var currentItem: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            currentItem = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentItem)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Creator<SavedState> {

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    protected abstract fun recreateAssets(width: Int, height: Int)

    // S C R O L L

    /**
     * Creates scroller appropriate for specific wheel implementation.
     *
     * @param scrollingListener listener to be passed to the scroller
     * @return Initialized scroller to be used
     */
    protected abstract fun createScroller(scrollingListener: ScrollingListener?): WheelScroller

    /* These methods are not abstract, as we may want to override only some of them */
    protected fun onScrollStarted() {
        //
    }

    protected open fun onScrollTouched() {
        //
    }

    protected open fun onScrollTouchedUp() {
        //
    }

    protected open fun onScrollFinished() {
        //
    }

    /**
     * Stops scrolling
     */
    fun stopScrolling() {
        scroller!!.stopScrolling()
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    fun setInterpolator(interpolator: Interpolator?) {
        scroller!!.setInterpolator(interpolator)
    }

    /**
     * Scroll the spinnerwheel
     *
     * @param itemsToScroll items to scroll
     * @param time          scrolling duration
     */
    private fun scroll(itemsToScroll: Int, time: Int) {
        val distance = itemsToScroll * itemDimension - scrollingOffset
        onScrollTouched() // we have to emulate touch when scrolling spinnerwheel programmatically to light up stuff
        scroller!!.scroll(distance, time)
    }

    /**
     * Scrolls the spinnerwheel
     *
     * @param delta the scrolling value
     */
    private fun doScroll(delta: Int) {
        scrollingOffset += delta

        val itemDimension = itemDimension
        var count = scrollingOffset / itemDimension

        var pos = currentItemIdx - count
        val itemCount = viewAdapter!!.itemsCount

        var fixPos = scrollingOffset % itemDimension
        if (abs(fixPos.toDouble()) <= itemDimension / 2) {
            fixPos = 0
        }
        if (isCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--
                count++
            } else if (fixPos < 0) {
                pos++
                count--
            }
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount
            }
            pos %= itemCount
        } else {
            if (pos < 0) {
                count = currentItemIdx
                pos = 0
            } else if (pos >= itemCount) {
                count = currentItemIdx - itemCount + 1
                pos = itemCount - 1
            } else if (pos > 0 && fixPos > 0) {
                pos--
                count++
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++
                count--
            }
        }

        val offset = scrollingOffset
        if (pos != currentItemIdx) {
            setCurrentItem(pos, false)
        } else {
            invalidate()
        }

        // update offset
        val baseDimension = baseDimension
        scrollingOffset = offset - count * itemDimension
        if (scrollingOffset > baseDimension) {
            scrollingOffset = scrollingOffset % baseDimension + baseDimension
        }
    }

    // L A Y O U T   M E A S U R E M E N T

    /**
     * Returns base dimension of the spinnerwheel — width for horizontal spinnerwheel, height for vertical
     */
    protected abstract val baseDimension: Int

    /**
     * Returns base dimension of base item — width for horizontal spinnerwheel, height for vertical
     */
    protected abstract val itemDimension: Int

     /**
     * Creates item layouts if necessary
     */
    protected abstract fun createItemsLayout()

    /**
     * Sets layout width and height
     */
    protected abstract fun doItemsLayout()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            val w = r - l
            val h = b - t
            doItemsLayout()
            if (layoutWidth != w || layoutHeight != h) {
                recreateAssets(measuredWidth, measuredHeight)
            }
            layoutWidth = w
            layoutHeight = h
        }
    }

    /**
     * Invalidates items layout
     *
     * @param clearCaches if true then cached views will be cleared
     */
    fun invalidateItemsLayout(clearCaches: Boolean) {
        if (clearCaches) {
            recycler.clearAll()
            if (itemsLayout != null) {
                itemsLayout!!.removeAllViews()
            }
            scrollingOffset = 0
        } else if (itemsLayout != null) {
            // cache all items
            recycler.recycleItems(itemsLayout!!, firstItemIdx, ItemsRange())
        }
        invalidate()
    }

    // L I S T E N

    /**
     * Processes MotionEvent and returns relevant position — x for horizontal spinnerwheel, y for vertical
     *
     * @param event MotionEvent to be processed
     * @return relevant position of the MotionEvent
     */
    protected abstract fun getMotionEventPosition(event: MotionEvent): Float

    /**
     * Adds spinnerwheel changing listener
     *
     * @param listener the listener
     */
    fun addChangingListener(listener: (AbstractWheel, Int, Int) -> Unit) {
        changingListeners.add(listener)
    }

    /**
     * Removes spinnerwheel changing listener
     *
     * @param listener the listener
     */
    fun removeChangingListener(listener: (AbstractWheel, Int, Int) -> Unit) {
        changingListeners.remove(listener)
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old spinnerwheel value
     * @param newValue the new spinnerwheel value
     */
    private fun notifyChangingListeners(oldValue: Int, newValue: Int) {
        for (listener in changingListeners) {
            listener.invoke(this, oldValue, newValue)
        }
    }

    /**
     * Adds spinnerwheel scrolling listener
     *
     * @param listener the listener
     */
    fun addScrollingListener(listener: OnWheelScrollListener) {
        scrollingListeners.add(listener)
    }

    /**
     * Removes spinnerwheel scrolling listener
     *
     * @param listener the listener
     */
    fun removeScrollingListener(listener: OnWheelScrollListener) {
        scrollingListeners.remove(listener)
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected fun notifyScrollingListenersAboutStart() {
        for (listener in scrollingListeners) {
            listener.onScrollingStarted(this)
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected fun notifyScrollingListenersAboutEnd() {
        for (listener in scrollingListeners) {
            listener.onScrollingFinished(this)
        }
    }

    /**
     * Adds spinnerwheel clicking listener
     *
     * @param listener the listener
     */
    fun addClickingListener(listener: (wheel: AbstractWheel, itemIndex: Int) -> Unit) {
        clickingListeners.add(listener)
    }

    /**
     * Removes spinnerwheel clicking listener
     *
     * @param listener the listener
     */
    fun removeClickingListener(listener: (wheel: AbstractWheel, itemIndex: Int) -> Unit) {
        clickingListeners.remove(listener)
    }

    /**
     * Notifies listeners about clicking
     *
     * @param item clicked item
     */
    private fun notifyClickListenersAboutClick(item: Int) {
        for (listener in clickingListeners) {
            listener.invoke(this, item)
        }
    }

    // T O U C H   E V E N T S

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || viewAdapter == null) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP -> if (!isScrollingPerformed) {
                var distance = getMotionEventPosition(event).toInt() - baseDimension / 2
                if (distance > 0) {
                    distance += itemDimension / 2
                } else {
                    distance -= itemDimension / 2
                }
                val items = distance / itemDimension
                if (items != 0 && isValidItemIndex(currentItemIdx + items)) {
                    notifyClickListenersAboutClick(currentItemIdx + items)
                }
            }

            else -> {}
        }
        return scroller!!.onTouchEvent(event)
    }

    // I T E M S   R A N G E

    private val itemsRange: ItemsRange
        /**
         * Calculates range for spinnerwheel items
         *
         * @return the items range
         */
        get() {
            if (isAllVisible) {
                val baseDimension = baseDimension
                val itemDimension = itemDimension
                if (itemDimension != 0) {
                    visibleItems = baseDimension / itemDimension + 1
                }
            }

            var start = currentItemIdx - visibleItems / 2
            var end = start + visibleItems - (if (visibleItems % 2 == 0) 0 else 1)
            if (scrollingOffset != 0) {
                if (scrollingOffset > 0) {
                    start--
                } else {
                    end++
                }
            }
            if (!isCyclic) {
                if (start < 0) {
                    start = 0
                }
                if (viewAdapter == null) {
                    end = 0
                } else if (end > viewAdapter!!.itemsCount) {
                    end = viewAdapter!!.itemsCount
                }
            }
            return ItemsRange(start, end - start + 1)
        }

    /**
     * Checks whether item index is valid
     *
     * @param index the item index
     * @return true if item index is not out of bounds or the spinnerwheel is cyclic
     */
    private fun isValidItemIndex(index: Int): Boolean {
        return (viewAdapter != null) && (viewAdapter!!.itemsCount > 0) && (isCyclic || (index >= 0 && index < viewAdapter!!.itemsCount))
    }

    // I T E M   V I E W

    /**
     * Adds view for item to items layout
     *
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private fun addItemView(index: Int, first: Boolean): Boolean {
        val view = getItemView(index)
        if (view != null) {
            if (first) {
                itemsLayout!!.addView(view, 0)
            } else {
                itemsLayout!!.addView(view)
            }
            return true
        }
        return false
    }

    /**
     * Returns view for specified item
     *
     * @param index0 the item index
     * @return item view or empty view if index is out of bounds
     */
    private fun getItemView(index0: Int): View? {
        var index = index0
        if (viewAdapter == null || viewAdapter!!.itemsCount == 0) {
            return null
        }
        val count = viewAdapter!!.itemsCount
        if (!isValidItemIndex(index)) {
            return viewAdapter!!.getEmptyItem(recycler.emptyItem!!, itemsLayout!!)
        }
        while (index < 0) {
            index += count
        }
        index %= count
        return viewAdapter!!.getItem(index, recycler.item!!, itemsLayout!!)
    }

    // R E B U I L D I N G   I T E M S

    /**
     * Rebuilds spinnerwheel items if necessary. Caches all unused items.
     *
     * @return true if items are rebuilt
     */
    protected fun rebuildItems(): Boolean {
        var updated: Boolean
        val range = itemsRange

        if (itemsLayout != null) {
            val first = recycler.recycleItems(itemsLayout!!, firstItemIdx, range)
            updated = firstItemIdx != first
            firstItemIdx = first
        } else {
            createItemsLayout()
            updated = true
        }

        if (!updated) {
            updated = firstItemIdx != range.first || itemsLayout!!.childCount != range.count
        }

        if (firstItemIdx > range.first && firstItemIdx <= range.last) {
            for (i in firstItemIdx - 1 downTo range.first) {
                if (!addItemView(i, true)) {
                    break
                }
                firstItemIdx = i
            }
        } else {
            firstItemIdx = range.first
        }

        var first = firstItemIdx
        for (i in itemsLayout!!.childCount until range.count) {
            if (!addItemView(firstItemIdx + i, false) && itemsLayout!!.childCount == 0) {
                first++
            }
        }
        firstItemIdx = first

        return updated
    }

    companion object {

        /**
         * Default count of visible items
         */
        private const val DEF_VISIBLE_ITEMS = 4
        private const val DEF_IS_CYCLIC = false
    }
}
