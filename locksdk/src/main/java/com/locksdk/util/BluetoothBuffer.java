package com.locksdk.util;

/**
 * Created by Sujiayong on 2017/11/22.
 */

public class BluetoothBuffer {

    private byte[] _rawBuffer;
    private int _rawBufferSize = _rawBuffer.length;

    public  void appendBuffer(byte[] buffer) {
        if (null == buffer || 0 == buffer.length) return;
        int size = buffer.length + this._rawBufferSize;
        if (size <= this._rawBuffer.length) {
            System.arraycopy(buffer, 0, this._rawBuffer, this._rawBufferSize, buffer.length);
            this._rawBufferSize += buffer.length;
        } else {
            int newSize = this._rawBuffer.length;
            while (newSize <= size) {
                newSize *= 1.5;
            }
            byte[] newRawBuffer = new byte[newSize];
            System.arraycopy(this._rawBuffer, 0, newRawBuffer, 0, this._rawBufferSize);
            this._rawBuffer = newRawBuffer;
            System.arraycopy(buffer, 0, this._rawBuffer, this._rawBufferSize, buffer.length);
            this._rawBufferSize += buffer.length;
        }
    }

    public  byte[] getFrontBuffer(int size) {
        if (0 >= size || size > this._rawBufferSize) return null;
        byte[] buffer = new byte[size];
        System.arraycopy(this._rawBuffer, 0, buffer, 0, size);
        return buffer;
    }

    public  void releaseFrontBuffer(int size) {
        if (0 >= size || size > this._rawBufferSize) return;
        System.arraycopy(this._rawBuffer, size, this._rawBuffer, 0, this._rawBufferSize - size);
        this._rawBufferSize -= size;
    }



}
