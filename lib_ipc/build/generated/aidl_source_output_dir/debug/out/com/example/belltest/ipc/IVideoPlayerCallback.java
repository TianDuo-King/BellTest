/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /Users/bell/Library/Android/sdk/build-tools/36.0.0/aidl -p/Users/bell/Library/Android/sdk/platforms/android-37.0/framework.aidl -o/Users/bell/projects/BellTest/lib_ipc/build/generated/aidl_source_output_dir/debug/out -I/Users/bell/projects/BellTest/lib_ipc/src/main/aidl -I/Users/bell/projects/BellTest/lib_ipc/src/debug/aidl -I/Users/bell/.gradle/caches/9.5.0/transforms/35c1e9d7956b4d3517ce2cde3f5e951e/transformed/core-1.10.1/aidl -I/Users/bell/.gradle/caches/9.5.0/transforms/531cc25006e5ef00b3b0a1fc664ff403/transformed/versionedparcelable-1.1.1/aidl -d/var/folders/yk/91hhzp792nn5v2drzldht0g80000gn/T/aidl3139062256078467403.d /Users/bell/projects/BellTest/lib_ipc/src/main/aidl/com/example/belltest/ipc/IVideoPlayerCallback.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.example.belltest.ipc;
public interface IVideoPlayerCallback extends android.os.IInterface
{
  /** Default implementation for IVideoPlayerCallback. */
  public static class Default implements com.example.belltest.ipc.IVideoPlayerCallback
  {
    @Override public void onPlaybackStateChanged(com.example.belltest.ipc.PlaybackSnapshot snapshot) throws android.os.RemoteException
    {
    }
    @Override public void onVideoSizeChanged(long requestId, java.lang.String mediaId, int width, int height, float pixelWidthHeightRatio) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.example.belltest.ipc.IVideoPlayerCallback
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.example.belltest.ipc.IVideoPlayerCallback interface,
     * generating a proxy if needed.
     */
    public static com.example.belltest.ipc.IVideoPlayerCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.example.belltest.ipc.IVideoPlayerCallback))) {
        return ((com.example.belltest.ipc.IVideoPlayerCallback)iin);
      }
      return new com.example.belltest.ipc.IVideoPlayerCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_onPlaybackStateChanged:
        {
          com.example.belltest.ipc.PlaybackSnapshot _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.example.belltest.ipc.PlaybackSnapshot.CREATOR);
          this.onPlaybackStateChanged(_arg0);
          break;
        }
        case TRANSACTION_onVideoSizeChanged:
        {
          long _arg0;
          _arg0 = data.readLong();
          java.lang.String _arg1;
          _arg1 = data.readString();
          int _arg2;
          _arg2 = data.readInt();
          int _arg3;
          _arg3 = data.readInt();
          float _arg4;
          _arg4 = data.readFloat();
          this.onVideoSizeChanged(_arg0, _arg1, _arg2, _arg3, _arg4);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.example.belltest.ipc.IVideoPlayerCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onPlaybackStateChanged(com.example.belltest.ipc.PlaybackSnapshot snapshot) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, snapshot, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPlaybackStateChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onVideoSizeChanged(long requestId, java.lang.String mediaId, int width, int height, float pixelWidthHeightRatio) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(requestId);
          _data.writeString(mediaId);
          _data.writeInt(width);
          _data.writeInt(height);
          _data.writeFloat(pixelWidthHeightRatio);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onVideoSizeChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onPlaybackStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onVideoSizeChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.example.belltest.ipc.IVideoPlayerCallback";
  public void onPlaybackStateChanged(com.example.belltest.ipc.PlaybackSnapshot snapshot) throws android.os.RemoteException;
  public void onVideoSizeChanged(long requestId, java.lang.String mediaId, int width, int height, float pixelWidthHeightRatio) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
