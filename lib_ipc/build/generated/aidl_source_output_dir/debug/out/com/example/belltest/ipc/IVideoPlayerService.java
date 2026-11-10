/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /Users/bell/Library/Android/sdk/build-tools/36.0.0/aidl -p/Users/bell/Library/Android/sdk/platforms/android-37.0/framework.aidl -o/Users/bell/projects/BellTest/lib_ipc/build/generated/aidl_source_output_dir/debug/out -I/Users/bell/projects/BellTest/lib_ipc/src/main/aidl -I/Users/bell/projects/BellTest/lib_ipc/src/debug/aidl -I/Users/bell/.gradle/caches/9.5.0/transforms/35c1e9d7956b4d3517ce2cde3f5e951e/transformed/core-1.10.1/aidl -I/Users/bell/.gradle/caches/9.5.0/transforms/531cc25006e5ef00b3b0a1fc664ff403/transformed/versionedparcelable-1.1.1/aidl -d/var/folders/yk/91hhzp792nn5v2drzldht0g80000gn/T/aidl17043514279019295634.d /Users/bell/projects/BellTest/lib_ipc/src/main/aidl/com/example/belltest/ipc/IVideoPlayerService.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.example.belltest.ipc;
public interface IVideoPlayerService extends android.os.IInterface
{
  /** Default implementation for IVideoPlayerService. */
  public static class Default implements com.example.belltest.ipc.IVideoPlayerService
  {
    @Override public void attachSurface(long surfaceToken, android.view.Surface surface) throws android.os.RemoteException
    {
    }
    @Override public void clearSurface(long surfaceToken) throws android.os.RemoteException
    {
    }
    @Override public void prepare(long requestId, java.lang.String mediaId, java.lang.String url, boolean playWhenReady) throws android.os.RemoteException
    {
    }
    @Override public void play() throws android.os.RemoteException
    {
    }
    @Override public void pause() throws android.os.RemoteException
    {
    }
    @Override public void seekTo(long positionMs) throws android.os.RemoteException
    {
    }
    @Override public void registerCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException
    {
    }
    @Override public void unregisterCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException
    {
    }
    @Override public void releasePlayback() throws android.os.RemoteException
    {
    }
    @Override public com.example.belltest.ipc.PlaybackSnapshot getSnapshot() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int getServicePid() throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.example.belltest.ipc.IVideoPlayerService
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.example.belltest.ipc.IVideoPlayerService interface,
     * generating a proxy if needed.
     */
    public static com.example.belltest.ipc.IVideoPlayerService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.example.belltest.ipc.IVideoPlayerService))) {
        return ((com.example.belltest.ipc.IVideoPlayerService)iin);
      }
      return new com.example.belltest.ipc.IVideoPlayerService.Stub.Proxy(obj);
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
        case TRANSACTION_attachSurface:
        {
          long _arg0;
          _arg0 = data.readLong();
          android.view.Surface _arg1;
          _arg1 = _Parcel.readTypedObject(data, android.view.Surface.CREATOR);
          this.attachSurface(_arg0, _arg1);
          break;
        }
        case TRANSACTION_clearSurface:
        {
          long _arg0;
          _arg0 = data.readLong();
          this.clearSurface(_arg0);
          break;
        }
        case TRANSACTION_prepare:
        {
          long _arg0;
          _arg0 = data.readLong();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          boolean _arg3;
          _arg3 = (0!=data.readInt());
          this.prepare(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        case TRANSACTION_play:
        {
          this.play();
          break;
        }
        case TRANSACTION_pause:
        {
          this.pause();
          break;
        }
        case TRANSACTION_seekTo:
        {
          long _arg0;
          _arg0 = data.readLong();
          this.seekTo(_arg0);
          break;
        }
        case TRANSACTION_registerCallback:
        {
          com.example.belltest.ipc.IVideoPlayerCallback _arg0;
          _arg0 = com.example.belltest.ipc.IVideoPlayerCallback.Stub.asInterface(data.readStrongBinder());
          this.registerCallback(_arg0);
          break;
        }
        case TRANSACTION_unregisterCallback:
        {
          com.example.belltest.ipc.IVideoPlayerCallback _arg0;
          _arg0 = com.example.belltest.ipc.IVideoPlayerCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterCallback(_arg0);
          break;
        }
        case TRANSACTION_releasePlayback:
        {
          this.releasePlayback();
          break;
        }
        case TRANSACTION_getSnapshot:
        {
          com.example.belltest.ipc.PlaybackSnapshot _result = this.getSnapshot();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getServicePid:
        {
          int _result = this.getServicePid();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.example.belltest.ipc.IVideoPlayerService
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
      @Override public void attachSurface(long surfaceToken, android.view.Surface surface) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(surfaceToken);
          _Parcel.writeTypedObject(_data, surface, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_attachSurface, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void clearSurface(long surfaceToken) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(surfaceToken);
          boolean _status = mRemote.transact(Stub.TRANSACTION_clearSurface, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void prepare(long requestId, java.lang.String mediaId, java.lang.String url, boolean playWhenReady) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(requestId);
          _data.writeString(mediaId);
          _data.writeString(url);
          _data.writeInt(((playWhenReady)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepare, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void play() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_play, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void pause() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_pause, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void seekTo(long positionMs) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(positionMs);
          boolean _status = mRemote.transact(Stub.TRANSACTION_seekTo, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void registerCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerCallback, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void unregisterCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void releasePlayback() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_releasePlayback, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public com.example.belltest.ipc.PlaybackSnapshot getSnapshot() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.example.belltest.ipc.PlaybackSnapshot _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSnapshot, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.example.belltest.ipc.PlaybackSnapshot.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public int getServicePid() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getServicePid, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_attachSurface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_clearSurface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_prepare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_play = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_seekTo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_releasePlayback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_getSnapshot = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_getServicePid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.example.belltest.ipc.IVideoPlayerService";
  public void attachSurface(long surfaceToken, android.view.Surface surface) throws android.os.RemoteException;
  public void clearSurface(long surfaceToken) throws android.os.RemoteException;
  public void prepare(long requestId, java.lang.String mediaId, java.lang.String url, boolean playWhenReady) throws android.os.RemoteException;
  public void play() throws android.os.RemoteException;
  public void pause() throws android.os.RemoteException;
  public void seekTo(long positionMs) throws android.os.RemoteException;
  public void registerCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException;
  public void unregisterCallback(com.example.belltest.ipc.IVideoPlayerCallback callback) throws android.os.RemoteException;
  public void releasePlayback() throws android.os.RemoteException;
  public com.example.belltest.ipc.PlaybackSnapshot getSnapshot() throws android.os.RemoteException;
  public int getServicePid() throws android.os.RemoteException;
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
