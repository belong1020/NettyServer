package com.worldline.isa.entity;

/**
 * <p>字节.</p>
 * 对象，但可能没有什么用，BITMAP没有创建也不影响使用。
 *
 * @author Magic Joey
 * @version BINARY.java 1.0 Created@2015-06-15 23:05 $
 */
public class BINARY implements IsoType {

    @Override
    public boolean isLVar() {
        return false;
    }

    @Override
    public String setByteValue(byte[] bts) {
        return null;
    }

    @Override
    public byte[] setValue(String bts) {
        return new byte[0];
    }

    @Override
    public int varLength(byte[] bytes) {
        return 0;
    }

}
