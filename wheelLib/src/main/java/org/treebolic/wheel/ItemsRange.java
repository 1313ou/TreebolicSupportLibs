/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

/**
 * Range for visible items.
 */
@SuppressWarnings("WeakerAccess")
public class ItemsRange
{
	// First item number
	private final int first;

	// Items count
	private final int count;

	/**
	 * Default constructor. Creates an empty range
	 */
	public ItemsRange()
	{
		this(0, 0);
	}

	/**
	 * Constructor
	 *
	 * @param first0 the number of first item
	 * @param count0 the count of items
	 */
	public ItemsRange(int first0, int count0)
	{
		this.first = first0;
		this.count = count0;
	}

	/**
	 * Gets number of first item
	 *
	 * @return the number of the first item
	 */
	public int getFirst()
	{
		return this.first;
	}

	/**
	 * Gets number of last item
	 *
	 * @return the number of last item
	 */
	public int getLast()
	{
		return getFirst() + getCount() - 1;
	}

	/**
	 * Get items count
	 *
	 * @return the count of items
	 */
	public int getCount()
	{
		return this.count;
	}

	/**
	 * Tests whether item is contained by range
	 *
	 * @param index the item number
	 * @return true if item is contained
	 */
	public boolean contains(int index)
	{
		return index >= getFirst() && index <= getLast();
	}
}