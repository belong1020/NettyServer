package com.worldline.isa.factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.worldline.isa.Listener.Simple8583Exception;
import com.worldline.isa.key.SimpleConstants;
import com.worldline.isa.model.BitMap;
import com.worldline.isa.model.IsoField;
import com.worldline.isa.model.IsoPackage;
import com.worldline.isa.util.EncodeUtil;
import com.worldline.isa.util.SimpleUtil;

/**
 * <p>
 * 报文组装抽象类.
 * </p>
 * 
 * @author Magic Joey
 * @version AbstractIsoMsgFactory.java 1.0 Created@2014-07-10 10:43 $
 */
public abstract class AbstractIsoMsgFactory {

	protected String macKey;

	protected AbstractIsoMsgFactory() {

	}

	// 入口和出口
	public byte[] pack(Map<String, String> dataMap, final IsoPackage pack) throws Exception {
		String split = dataMap.get(SimpleConstants.BIT_MAP);
		StringBuilder sb = new StringBuilder();
				
		byte[] bitMapByte ;// = dataMap.get(SimpleConstants.BIT_MAP).toString().getBytes();
		BitMap bitMap = new BitMap(split.length());
		bitMapByte = bitMap.addBits(dataMap.get(SimpleConstants.BIT_MAP));
		pack.getIsoField(SimpleConstants.BIT_MAP).setByteValue(bitMapByte);
		
//		BitMap bitMap = null;
		if (dataMap.get(SimpleConstants.BIT_MAP).length() < 65) {
			bitMap = new BitMap(64);
		} else {
			pack.setBit64(false);
			bitMap = new BitMap(128);
		}
//		byte[] bitMapByte = bitMap.addBits(dataFieldList);
		pack.getIsoField(SimpleConstants.BIT_MAP).setByteValue(bitMapByte);
		
		compare(dataMap);

		// 设置BitMap的值
		IsoField BitField = pack.getIsoField(SimpleConstants.BIT_MAP);
		// BitField.setValue(dataMap.get(SimpleConstants.BIT_MAP));
		BitField.setByteValue(bitMapByte);

		for (IsoField field : pack) {
			String fieldValue;
			if (field.isAppData()) {
				fieldValue = dataMap.get(field.getId());
				if (fieldValue == null || field.getId().indexOf(".") > 0) {
					continue;
				}
				// 其他域值
				int index = Integer.valueOf(field.getId());
				if (index == 1) {
					continue;// 第一位不处理，只是标志位
				}
				if (bitMap.getBit(index - 1) == 1) {
					field.setValue(fieldValue);
					field.setByteValue(fieldValue.getBytes());
				}
			} else {
				fieldValue = dataMap.get(field.getId());
				if (fieldValue == null) {
					continue;
				}
				if (!SimpleConstants.BIT_MAP.equals(field.getId())) {
					field.setValue(fieldValue);
				}
				// field.setByteValue(fieldValue.getBytes());
			}
		}

		// 将数组合并
		switch ("") {
		case "MASTER":
			break;
		case "VISA":
			break;
		case "UnionPay":
			return merge(pack);
		}
		return merge(pack);
	}

	/**
	 * 将返回信息拆成Map返回
	 * 
	 * @param bts
	 * @param pack
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> unpack(byte[] bts, final IsoPackage pack) throws Exception {
		if (pack == null || pack.size() == 0) {
			throw new IllegalArgumentException("配置为空，请检查IsoPackage是否为空");
		}
		IsoPackage deepClone = pack.deepClone();
		Map<String, String> returnMap = new LinkedHashMap<String, String>();
		// 起判断的作用
		int offset = 0;
		// 获取到bitMap
		boolean hasBitMap = false;
		BitMap bitMap = null;
		// 0 - 60 1 - 90
		byte[] index_xx = new byte[2];

		// Map<IsoField, Integer> fldLensMap = new IdentityHashMap<IsoField,
		// Integer>();

		for (IsoField field : deepClone) {
			if (field.isAppData()) {
				if (hasBitMap) {
					// 子域在此拆分已存进returnMap 中父域
					if (field.getId().indexOf('.') > 0) {
						// 重复returnMap.get() 稍微影响性能
						if (addSonField("60", returnMap, field, index_xx, 0)) {
							offset += subByte(bts, offset, field);
						} else if (addSonField("90", returnMap, field, index_xx, 1)) {
							offset += subByte(bts, offset, field);
						}
						continue;
					}
					// 其他域值
					int index = Integer.valueOf(field.getId());
					if (index == 1) {
						continue;// 第一位不处理，只是标志位
					}
					if (bitMap.getBit(index - 1) == 1) {
						offset += subByte(bts, offset, field);
						if ("60".equals(field.getId())) {
							offset -= field.getLength() - 3;
						} else if ("90".equals(field.getId())) {
							offset -= field.getLength();
						}
						returnMap.put(field.getId(), field.getValue());
					}
				}
			} else {
				offset += subByte(bts, offset, field);
				returnMap.put(field.getId(), field.getValue());
				if (field.getId().equalsIgnoreCase(SimpleConstants.BIT_MAP)) {
					hasBitMap = true;
					// bitMap = BitMap.addBits(field.getByteValue());
					bitMap = BitMap.addBitmap(field.getByteValue());

					field.setLength(bitMap.getLength() / 64);
				}
			}
		}
		// MAC校验
		// macValidate(pack,returnMap);
		deepClone = null;

		return returnMap;
	}

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
	private boolean addSonField(String mapKey, Map<String, String> returnMap, IsoField field, byte[] index_xx,
			int index) {
		if (returnMap.get(mapKey) != null && mapKey.equals(field.getId().substring(0, 2))
				&& !mapKey.equals(field.getId())) {
			returnMap.put(field.getId(),
					returnMap.get(mapKey).substring(index_xx[index], index_xx[index] + field.getLength()));
			index_xx[index] += field.getLength();
			return true;
		}
		return false;
	}

	/**
	 * byte[] 分割
	 * 
	 * @param bts
	 * @param offset
	 * @param field
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private int subByte(byte[] bts, int offset, IsoField field) throws Exception {
		byte[] val = null;
		int length = field.getLength();

		switch (field.getIsoType()) {
		case CHAR:
			StringBuffer sBuffer = new StringBuffer();
			if (field.getId().equals(SimpleConstants.HEADERLENGTH)
					|| field.getId().equals(SimpleConstants.HEADERFLAGANDVERSION)
					|| field.getId().equals(SimpleConstants.USERINFORMATION)) {
				val = new byte[length];
				for (int i = 0; i < field.getLength(); i++) {
					switch (field.getId()) {
					case SimpleConstants.HEADERLENGTH:
						val[i] = (byte) (bts[offset + i] & 0xFF);
						break;
					case SimpleConstants.USERINFORMATION:
					case SimpleConstants.HEADERFLAGANDVERSION:
						val[i] = bts[offset + i];
						break;
					default:
					}
				}
				break;
			}
			/*
			 * val = new byte[field.getLength()]; System.arraycopy(bts, offset,
			 * val, 0, length);
			 */
		case NUMERIC:
		case BINARY:
			/**
			 * sthing special, like ascii mem in byte
			 */
			switch (field.getId()) {
			case "128":
				sBuffer = new StringBuffer();
				for (int i = 0; i < field.getLength(); i++) {
					int jj = bts[offset + i];
					sBuffer.append(Integer.toHexString(jj));
				}
				val = new byte[length];
				System.arraycopy(sBuffer.toString().getBytes(), 0, val, 0, length);
				break;
			default:
				val = new byte[field.getLength()];
				System.arraycopy(bts, offset, val, 0, length);
			}
			break;
		case LLVAR_NUMERIC:
			byte[] llvarNumLen = new byte[2];
			System.arraycopy(bts, offset, llvarNumLen, 0, 2);
			// llvarNumLen[0] = bts[offset];
			// 除以2的原因是LLVAR_NUMERIC前面的报文域长度是数字长度而非字节长度
			int firstNumLen = Integer.valueOf(EncodeUtil.toHex(llvarNumLen));
			val = new byte[firstNumLen];
			System.arraycopy(bts, offset + 2, val, 0, firstNumLen);
			length = 2 + firstNumLen;
			break;
		case LLVAR:
			byte[] llvarLen = new byte[2];
			// llvarLen[0] = bts[offset];
			System.arraycopy(bts, offset, llvarLen, 0, 2);
			int firstLen = Integer.valueOf(EncodeUtil.toHex(llvarLen));
			val = new byte[firstLen];
			System.arraycopy(bts, offset + 2, val, 0, firstLen);
			length = 2 + firstLen;
			break;
		case LLLVAR:
			byte[] lllvarLen = new byte[3];
			// lllvarLen[0] = bts[offset];
			// lllvarLen[1] = bts[offset + 1];
			System.arraycopy(bts, offset, lllvarLen, 0, 3);
			int first2Len = Integer.valueOf(EncodeUtil.toHex(lllvarLen));
			val = new byte[first2Len];
			System.arraycopy(bts, offset + 3, val, 0, first2Len);
			length = 3 + first2Len;
			break;
		case BITMAP:
			byte[] bitmap = getBitmap(bts, offset);
			val = new byte[bitmap.length];
			System.arraycopy(bitmap, 0, val, 0, bitmap.length);
			// 不知道为什么-3系列 bit 后接域2
			length = bitmap.length / 8;
			break;
		default:
			break;
		}

		field.setByteValue(val);
		return length;
	}

	/**
	 * Byte数组的合并，不同byte数组域将被整合为一个大的byte数组,
	 * 
	 * @param isoPackage
	 * @return
	 * @throws IOException
	 */
	private byte[] merge(IsoPackage isoPackage) throws IOException {
		ByteArrayOutputStream byteOutPut = new ByteArrayOutputStream(256);
		for (IsoField field : isoPackage) {
			if (field.isChecked()) {
				// Mac 值 暂时直接重发回，
				/*
				 * if (isoPackage.isMacPos(field.getId())) { try {
				 * byteOutPut.write(mac(isoPackage)); } catch (Exception e) {
				 * e.printStackTrace(); } continue; }
				 */
				switch (field.getIsoType()) {
				// 中有一坑，此处LLVAR_NUMERIC 为域2 等对应类型，(暂考虑均为账号)但账号可能有补零操作，
				// 暂时考虑 1.报文接报后可以 补/不补 零后发送给dubbo端，
				// 2.dubbo 返回map 中考虑没有补零情况，直接取Length
				case LLVAR_NUMERIC:
					byte[] lengthByte0;
					lengthByte0 = EncodeUtil.bcd(EncodeUtil.intToByte(field.getValue().length()), 2);
					byteOutPut.write(lengthByte0);
					break;
				case LLVAR:
					byte[] lengthByte;
					lengthByte = EncodeUtil.bcd(EncodeUtil.intToByte(field.getByteValue().length), 2);
					byteOutPut.write(lengthByte);
					break;
				case LLLVAR:
					byte[] lengthByte2;
					lengthByte2 = EncodeUtil.bcd(EncodeUtil.intToByte(field.getByteValue().length), 3);
					byteOutPut.write(lengthByte2);
					break;
				default:
					break;
				}
				// 解析出的域和值
				// System.out.println(this.getClass().getSimpleName());
				// System.out.println(field.getId() + ":" +
				// EncodeUtil.hex(field.getByteValue()));

				byteOutPut.write(field.getByteValue());
			}
		}
		byte[] beforeSend = byteOutPut.toByteArray();
		byte[] bts = new byte[beforeSend.length + 4];
		byte[] lenArr = msgLength(beforeSend.length);
		//
		System.arraycopy(beforeSend, 0, bts, 4, beforeSend.length);
		// 两次信息长度值
		System.arraycopy(lenArr, 0, bts, 0, 4);
		System.arraycopy(lenArr, 0, bts, 6, 4);
		return bts;
	}

	/**
	 * 动态获取报文中Bitmap，64/128位
	 * 
	 * @param code
	 * @param start
	 * @return
	 */
	private static byte[] getBitmap(byte[] code, int start) {

		StringBuffer binary = new StringBuffer();
		// String str = new String(binary);
		// 取前4字节Bitmap
		for (int i = 0; i < 16; i++) {
			// 内到外---byte 转ASCII 去掉高把位(/256) 转 Hex 左补0
			int iii = (int) ((char) code[start + i]) % 256;
			String string2 = Integer.toBinaryString(iii).toString();
			String string = EncodeUtil.addBlankLeft(string2, 8 - string2.length(), "0");
			binary.append(string);
			// Bitmap 第一个标识符判断Bitmap 长度， 0/1 --- 64/128
			if (i == 7 && binary.substring(0, 1).equals("0")) {
				break;
			}
		}

		return binary.toString().getBytes();
	}

	/**
	 * 获取mti状态码
	 * 
	 * @param buf
	 *            待处理数组
	 * @param start
	 *            开始
	 * @param step
	 *            步长
	 * @return
	 */
	public String getMti(byte[] buf, int start, int step) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < step; i++) {
			sb.append(String.valueOf((char) buf[start + i]));
		}
		return sb.toString();
	}

	// 生成前两个字节的长度位
	// 根据约定不同需要对此方法进行重写
	protected abstract byte[] msgLength(int length);

	// 生成最后一位的MAC加密
	protected abstract byte[] mac(IsoPackage isoPackage) throws Exception;

	// 对返回的数据进行MAC校验
	protected abstract void macValidate(IsoPackage isoPackage, Map<String, String> map);

	public void setMacKey(String macKey) {
		this.macKey = macKey;
	}

	// 子域合并
	private void compare(Map<String, String> map) {
		if (map.get("60") != null) {
			String[] son_60 = { "60.1", "60.2.1", "60.2.2", "60.2.3", "60.2.4", "60.2.5", "60.2.6", "60.2.7", "60.2.8",
					"60.2.9", "60.3.1", "60.3.2", "60.3.3", "60.3.4", "60.3.5", "60.3.6", "60.3.7", "60.3.8", "60.3.9",
					"60.3.10" };
			StringBuffer sb = new StringBuffer();
			for (String string : son_60) {
				if (map.get(string) != null) {
					sb.append(map.get(string));
				}
			}
			map.put("60", sb.toString());
		}
		if (map.get("90") != null) {
			String[] son_90 = { "90.1", "90.2", "90.3", "90.4", "90.5" };
			StringBuffer sb = new StringBuffer();
			for (String string : son_90) {
				if (map.get(string) != null) {
					sb.append(map.get(string));
				}
			}
			map.put("90", sb.toString());
		}
	}
}
