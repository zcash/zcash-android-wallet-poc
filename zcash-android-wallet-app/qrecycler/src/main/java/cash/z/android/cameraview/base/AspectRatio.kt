/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cash.z.android.cameraview.base

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.collection.SparseArrayCompat

/**
 * Immutable class for describing proportional relationship between width and height.
 */
class AspectRatio private constructor(val x: Int, val y: Int) : Comparable<AspectRatio>, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    fun matches(size: Size): Boolean {
        val gcd = gcd(size.width, size.height)
        val x = size.width / gcd
        val y = size.height / gcd
        return this.x == x && this.y == y
    }

    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (this === o) {
            return true
        }
        if (o is AspectRatio) {
            val ratio = o as AspectRatio?
            return x == ratio!!.x && y == ratio.y
        }
        return false
    }

    override fun toString(): String {
        return x.toString() + ":" + y
    }

    fun toFloat(): Float {
        return x.toFloat() / y
    }

    override fun hashCode(): Int {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return y xor (x shl Integer.SIZE / 2 or x.ushr(Integer.SIZE / 2))
    }

    override fun compareTo(@NonNull another: AspectRatio): Int {
        if (equals(another)) {
            return 0
        } else if (toFloat() - another.toFloat() > 0) {
            return 1
        }
        return -1
    }

    /**
     * @return The inverse of this [AspectRatio].
     */
    fun inverse(): AspectRatio {

        return AspectRatio.of(y, x)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(x)
        dest.writeInt(y)
    }
    companion object CREATOR : Parcelable.Creator<AspectRatio> {

        private val sCache = SparseArrayCompat<SparseArrayCompat<AspectRatio>>(16)

        /**
         * Returns an instance of [AspectRatio] specified by `x` and `y` values.
         * The values `x` and `` will be reduced by their greatest common divider.
         *
         * @param x The width
         * @param y The height
         * @return An instance of [AspectRatio]
         */
        fun of(inX: Int, inY: Int): AspectRatio {
            var x = inX
            var y = inY
            val gcd = gcd(x, y)
            x /= gcd
            y /= gcd
            var arrayX: SparseArrayCompat<AspectRatio>? = sCache.get(x)
            if (arrayX == null) {
                val ratio = AspectRatio(x, y)
                arrayX = SparseArrayCompat()
                arrayX!!.put(y, ratio)
                sCache.put(x, arrayX)
                return ratio
            } else {
                var ratio = arrayX!!.get(y)
                if (ratio == null) {
                    ratio = AspectRatio(x, y)
                    arrayX!!.put(y, ratio)
                }
                return ratio
            }
        }

        /**
         * Parse an [AspectRatio] from a [String] formatted like "4:3".
         *
         * @param s The string representation of the aspect ratio
         * @return The aspect ratio
         * @throws IllegalArgumentException when the format is incorrect.
         */
        fun parse(s: String): AspectRatio {
            val position = s.indexOf(':')
            if (position == -1) {
                throw IllegalArgumentException("Malformed aspect ratio: $s")
            }
            try {
                val x = Integer.parseInt(s.substring(0, position))
                val y = Integer.parseInt(s.substring(position + 1))
                return AspectRatio.of(x, y)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Malformed aspect ratio: $s", e)
            }

        }

        private fun gcd(a: Int, b: Int): Int {
            var a = a
            var b = b
            while (b != 0) {
                val c = b
                b = a % b
                a = c
            }
            return a
        }
        override fun createFromParcel(parcel: Parcel): AspectRatio {
            return AspectRatio(parcel)
        }

        override fun newArray(size: Int): Array<AspectRatio?> {
            return arrayOfNulls(size)
        }
    }
}
