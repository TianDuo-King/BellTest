package com.example.belltest.ipc

import android.os.Parcel
import android.os.Parcelable

data class PlaybackSnapshot(
    val requestId: Long = 0L,
    val mediaId: String = "",
    val playbackState: Int = PlaybackContract.STATE_IDLE,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val errorCode: Int = PlaybackContract.ERROR_NONE,
    val errorMessage: String? = null,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        requestId = parcel.readLong(),
        mediaId = parcel.readString().orEmpty(),
        playbackState = parcel.readInt(),
        isPlaying = parcel.readInt() == 1,
        positionMs = parcel.readLong(),
        durationMs = parcel.readLong(),
        bufferedPositionMs = parcel.readLong(),
        errorCode = parcel.readInt(),
        errorMessage = parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(requestId)
        parcel.writeString(mediaId)
        parcel.writeInt(playbackState)
        parcel.writeInt(if (isPlaying) 1 else 0)
        parcel.writeLong(positionMs)
        parcel.writeLong(durationMs)
        parcel.writeLong(bufferedPositionMs)
        parcel.writeInt(errorCode)
        parcel.writeString(errorMessage)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PlaybackSnapshot> {
        override fun createFromParcel(parcel: Parcel): PlaybackSnapshot = PlaybackSnapshot(parcel)

        override fun newArray(size: Int): Array<PlaybackSnapshot?> = arrayOfNulls(size)
    }
}
