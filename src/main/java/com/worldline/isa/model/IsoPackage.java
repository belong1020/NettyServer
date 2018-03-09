package com.worldline.isa.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.worldline.isa.key.SimpleConstants;

/**
 * <p>
 * 8583报文包.
 * </p>
 *
 * @author Magic Joey
 * @version Simple8583Test.java 1.0 @2014-07-10 10:43 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IsoPackage extends LinkedList<IsoField> implements Cloneable {

	private static final long serialVersionUID = 2715585287358366066L;

	// @XmlAttribute(name="mti")
	// private String mti;

	@XmlElement(name = "field")
	public List<IsoField> getFields() {
		return this;
	}

	// 默认64位
	private boolean bit64 = true;

	/**
	 * 根据key获取报文域
	 * 
	 * @param key
	 * @return
	 */
	public IsoField getIsoField(String key) {
		// int lastIndexOf = this.lastIndexOf(key);
		for (IsoField isoField : this) {
			if (isoField.getId().equalsIgnoreCase(key)) {
				return isoField;
			}
		}
		return null;
	}

	/**
	 * 是否为Mac位
	 * 
	 * @param key
	 * @return
	 */
	public boolean isMacPos(String key) {
		return (this.isBit64() && "64".equals(key)) || (!this.isBit64() && "128".equals(key));
	}

	/**
	 * 深度拷贝方法
	 * 
	 * @return
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	public IsoPackage deepClone() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream out = null;
		ByteArrayInputStream byteIn = null;
		ObjectInputStream in = null;
		IsoPackage isoPackage;
		try {
			byteOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			in = new ObjectInputStream(byteIn);
			isoPackage = (IsoPackage) in.readObject();
		} finally {
			if (byteOut != null) {
				byteOut.close();
			}
			if (out != null) {
				out.close();
			}
			if (byteIn != null) {
				byteIn.close();
			}
			if (in != null) {
				in.close();
			}
		}
		return isoPackage;
	}

	/*
	 * public String getMti() { return mti; }
	 * 
	 * public void setMti(String mti) { this.mti = mti; }
	 */

	public boolean isBit64() {
		return bit64;
	}

	public void setBit64(boolean bit64) {
		this.bit64 = bit64;
	}

	@Override
	public String toString() {
		StringBuffer accum = new StringBuffer("[");
		for (IsoField isoField : this) {
			accum.append(isoField.getId()).append(":").append(isoField.getValue()).append(",");
		}
		accum.append("]");
		return accum.toString();
	}

	public Map<String, String> getMap() {
		Map<String, String> isoPackageMap = new LinkedHashMap<String, String>();

		this.forEach((isoField) -> {
			if (isoField == null) {
				return;
			}
			// 0 - 60 1 - 90
			byte[] index_xx = new byte[2];

			BitMap bitMap = null;
			if (isoField.isAppData()) {
				// 子域在此拆分已存进returnMap 中父域
				if (isoField.getId().indexOf('.') > 0) {
					// 重复map.get()
					addSonField("60", isoPackageMap, isoField, index_xx, 0);
					addSonField("90", isoPackageMap, isoField, index_xx, 1);
					return;
				}
				// 其他域值
				int index = Integer.valueOf(isoField.getId());
				if (index == 1) {
					return;// 第一位不处理，只是标志位
				}
				if (bitMap.getBit(index - 1) == 1) {
					isoPackageMap.put(isoField.getId(), isoField.getValue());
				}
			} else {
				isoPackageMap.put(isoField.getId(), isoField.getValue());
				if (isoField.getId().equalsIgnoreCase(SimpleConstants.BIT_MAP)) {
					bitMap = BitMap.addBitmap(isoField.getByteValue());
				}
			}
		});

		return isoPackageMap;
	}

	/*
	 * public void insert(Map<String, String> isoPackageMap) { if(
	 * isoPackageMap.size() < 0 ) { return; }
	 * 
	 * this.getIsoField(SimpleConstants.BIT_MAP).setByteValue(isoPackageMap.get(
	 * "").toString().getBytes());
	 * 
	 * 
	 * for (String key : isoPackageMap.keySet()) { IsoField field =
	 * this.getIsoField(key); if (field == null) { continue; }
	 * field.setValue(isoPackageMap.get(key)); // 数据域 if
	 * (SimpleUtil.isNumeric(key)) { int val = Integer.valueOf(key); if
	 * (pack.isBit64() && val > 64) { // 设置位非64位图模式，即128模式 pack.setBit64(false);
	 * // 将bitMap第一位置为1，表示这个数据域为128位长 dataFieldList.add(1); }
	 * dataFieldList.add(val); }
	 * 
	 * int index = Integer.valueOf(isoField.getId()); if (index == 1) {
	 * return;// 第一位不处理，只是标志位 } if (bitMap.getBit(index - 1) == 1) {
	 * isoPackageMap.put(isoField.getId(), isoField.getValue()); } }
	 * 
	 * }
	 */

	/**
	 * 判断是否有子域
	 * 
	 * @param mapKey
	 *            域值
	 * @param returnMap
	 *            Map
	 * @param field
	 *            子域对象
	 * @param index_xx
	 *            子域分割index
	 * @param index
	 *            不同子域对象在index_xx 数组判断的位置
	 * @return
	 */
	boolean addSonField(String mapKey, Map<String, String> returnMap, IsoField field, byte[] index_xx, int index) {
		if (returnMap.get(mapKey) != null && mapKey.equals(field.getId().substring(0, 2))
				&& !mapKey.equals(field.getId())) {
			returnMap.put(field.getId(),
					returnMap.get(mapKey).substring(index_xx[index], index_xx[index] + field.getLength()));
			index_xx[index] += field.getLength();
			return true;
		}
		return false;
	}

}
