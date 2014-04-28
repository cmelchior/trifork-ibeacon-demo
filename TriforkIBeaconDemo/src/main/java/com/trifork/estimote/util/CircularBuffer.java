package com.trifork.estimote.util;
/**
 * Class that implements a Circular Buffer:
 * http://en.wikipedia.org/wiki/Circular_buffer
 *
 * The advantage of a Circular Buffer is that it enables you get a "sliding
 * scale" of readings, which makes it possible to detect patterns in the 
 * numbers without and at the same time use a finite amount of space.
 */
import java.util.Iterator;

public class CircularBuffer<T> implements Iterable<T> {

    public Object[] buffer; 	// Data structure, i = 0 is never used (=Always Keep One Byte Open)
    private int size;			// Actual buffer size

    private int start;			// Internal pointer to the the "start" of the buffer (oldest data)
    private int end;			// Internal pointer to the current "end" of the buffer (newest data)


    /**
     * Constructor
     *
     * @param size	Size of buffer before it starts overriding old values
     */
    public CircularBuffer(int size) {
        this.buffer = new Object[size + 1];
        this.size = size;
        this.start = 0;
        this.end = 0;
    }

    /**
     * Add a element to the buffer
     *
     * @param element
     */
    public void add(T element) {

        // Empty buffer is a special case
        if (start == end && end == 0) {
            start = 1;
            end = 1;
            buffer[end] = element;

            // Else adjust pointers and add element
        } else {
            end = getNextIndex(end);
            buffer[end] = element;

            // Adjust start pointer if old data was overwritten
            if (end == start) {
                start = getNextIndex(start);
            }
        }
    }

    /**
     * Retrieve a element on the given index starting from the oldest data
     *
     * @param index 	A index between 0 - (buffer.length - 1)
     * @return Integer found at index. 0 can also means that value wasn't defined.
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index > (size-1)) throw new IllegalArgumentException();
        return (T) buffer[(start + index) % size];
    }

    @SuppressWarnings("unchecked")
    public T getLatest() {
        return (T) buffer[end];
    }

    @SuppressWarnings("unchecked")
    public T getFirst() {
        return (T) buffer[start];
    }

    /**
     * Clears the entire buffer and resets internal pointers
     */
    public void clear() {
        for(int i = 0; i < buffer.length; i++) {
            buffer[i] = null;
        }

        start = 0;
        end = 0;
    }

    /**
     * Returns the no. of slots in the buffer containing actual values
     *
     * @return Size of the filled buffer
     */
    public int size() {
        // Partially filled buffer
        if (end > start) {
            return end-start+1;

            //Filled buffer
        } else {
            return size;
        }
    }

    /**
     * Returns a iterator, iterating the buffer from oldest to newest
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int index = start;
            private boolean seenEndPointer = false;	// True, if iterator has passed end pointer

            public boolean hasNext() {
                // Check that we are not looping
                if (seenEndPointer) return false;
                if (size == 0) return false;

                // Check if at last element
                if (index == end) seenEndPointer = true;

                // If we are not past endPointer, there are still elements remaining
                return true;
            }

            @SuppressWarnings("unchecked")
            public T next() {
                Object obj = buffer[index];
                index = getNextIndex(index);
                return (T) obj;
            }

            public void remove() {
                // Ignore
            }
        };
    }


    /**
     * Helper method.
     * Determines next internal array index from a given index
     *
     * @param	index
     * @return	Next internal index
     */
    private int getNextIndex(int index) {
        return (index % size) + 1;
    }
}
