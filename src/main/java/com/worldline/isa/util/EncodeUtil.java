package com.worldline.isa.util;

import java.io.UnsupportedEncodingException;

import com.worldline.isa.Listener.Simple8583Exception;

/**
 * <p>
 * 编码转换工具类.如:BCD和HEX
 * </p>
 * 
 * @author Magic Joey
 * @version EncodeUtil.java 1.0 @2014-07-10 09:51 $
 */
public class EncodeUtil {

	protected static final char[] HEX = new char[] { '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	protected static final char[] BINARY = new char[] { '0', '1' };

	private EncodeUtil() {

	}

	// 传入参数为只有01的字符串
	public static byte[] binary(String binaryStr) {
		// 长度不是8倍数的话，无法知道在左边或右边补零，会引起歧义，导致结果不正确
		if (binaryStr.length() % 8 != 0) {
			throw new IllegalArgumentException("传入的参数长度必须是8的倍数");
		}
		StringBuffer accum = new StringBuffer();
		for (int i = 0; i < binaryStr.length(); i += 4) {
			String temp = binaryStr.substring(i, i + 4);
			int value = 0;
			for (int j = 0; j < 4; j++) {
				if (temp.charAt(j) == '1') {
					value += Math.pow(2, 3 - j);// 计算值
				}
			}
			accum.append(HEX[value]);
		}
		return bcd(accum.toString());
	}

	public static String hex(byte[] bts, int offset, int length) {//
		if (offset < 0 || length < 0 || bts.length < offset + length - 1) {
			throw new IllegalArgumentException("参数非法：offset:" + offset
					+ ",length:" + length + ",字节长度：" + bts.length);
		}
		byte[] returnBt = new byte[length];
		System.arraycopy(returnBt, 0, bts, offset, length);
		return hex(returnBt);
	}

	/**
	 * 将byte数组转化为String类型的十六进制编码格式 本方法实现的思路是： 1）每位byte数组转换为2位的十六进制数
	 * 2）将字节左移4位取得高四位字节数值，获取对应的char类型数组编码 3）将字节与0x0F按位与，从而获取第四位的字节，同样获取编码
	 */
	public static String hex(byte[] bParam) {
		StringBuilder accum = new StringBuilder();
		for (byte bt : bParam) {
			accum.append(HEX[bt >> 4 & 0x0F]);// &0x0F的目的是为了转换负数
			accum.append(HEX[bt & 0x0F]);
		}
		return accum.toString();
	}

	public static byte[] bcd(int code, int len) {
		// 上行为源代码 下行修改版
		return EncodeUtil.bcd(String.valueOf(code), len, "0");
		// return EncodeUtil.bcdLL(String.valueOf(code), len);
	}

	public static String binary(byte[] bts) {
		StringBuffer accum = new StringBuffer();
		for (byte bt : bts) {
			accum.append(binary(bt));
		}
		return accum.toString();
	}

	// 本方法修改于Integer.toBinaryString
	// 参数的每个字节都会转化为8位2进制字符串，如1会转换为00000001
	private static String binary(byte bt) {
		int num = bt & 0xFF;
		char[] arrayOfChar = new char[8];
		int i = 8;
		for (int times = 0; times < 8; times++) {
			arrayOfChar[(--i)] = BINARY[(num & 0x01)];
			num >>>= 1;
		}
		return new String(arrayOfChar);
	}

	/**
	 * BCD编码(8421码)为一个4位表示一个10进制数，即每个字节表示两个数
	 * 本方法中的code为10进制数（本方法支持16进制数编码，每两位编为1字节）
	 */
	public static byte[] bcd(String code) {
		// 控制byte数组的大小
		int len = code.length() % 2 == 0 ? code.length() / 2
				: code.length() / 2 + 1;
		return bcd(code, len, "0");
	}

	/**
	 * 上一个的adapter,
	 * 
	 * @param code
	 * @return
	 */
	public static String bcd(byte[] code) {
		return new String(bcd(new String(code)));
	}

	/**
	 * 打包时使用方法
	 * @param code
	 * @param length
	 * @return
	 */
	public static byte[] bcd(String code, int length){
		return bcd(code, length, "30");
	}
	
	/**
	 * String to byte like code = "303130" -> result =
	 * (byte)'30''31''30'-(int)'48''49''48'
	 * 
	 * @param code		like "303130"
	 * @param length	byte字节长度
	 * @param head		需要byte字节长度 > code 时，byte添加head value
	 * @return
	 */
	public static byte[] bcd(String code, int length, String head) {
		if (length < 0) {
			throw new IllegalArgumentException("参数length不能小于0,length:" + length);
		} else if (length == 0) {
			return new byte[0];
		}
		byte[] bt = new byte[length];
		// 指示当前位置
		int point = 0;
		//添0 (byte:30)补齐
		if (code.length() < 2 * length) {
			code = addBlankLeft(code, length - code.length() / 2, head);
		}

		// 每两位合并为一个字节
		for (; point < code.length(); point += 2) {
			// (point+1)/2计算当前指向的值
			// Character.digit将对应的Char转为数字，如'8'-> 8
			// <<4左移四位：即为→_→（右边）的数字让开位置
			bt[(point + 1) / 2] = (byte) (Character.digit(code.charAt(point),16) << 4 | Character.digit(code.charAt(point + 1), 16));
			/*bt[(point / 2)] = ((byte) ((Character.digit(code.charAt(point), 16) << 4) + Character
    				.digit(code.charAt(point + 1), 16)));*/
		}
		return bt;
	}

	/**
	 * byte array to int, two ASCII in array, and array length is dynamic.
	 * 
	 * @param bParam
	 * @return
	 */
	public static String toHex(byte[] bParam) {
		StringBuilder accum = new StringBuilder();
		for (byte bt : bParam) {
			accum.append(HEX[(int) bt & 0x0F]);// &0x0F的目的是为了转换负数
			// accum.append(HEX[bt >> 4 & 0x0F]);// &0x0F的目的是为了转换负数
			// accum.append(HEX[bt & 0x0F]);
		}
		return accum.toString();
	}

	/**
	 * code 为value值而非length值，忽略串前可能出现的"0" 取realLength
	 * 
	 * @param code
	 * @param len
	 * @return
	 */
	public static byte[] bcdWithRealLength(String code, int len) {
		int i = 0;
		while (code.substring(i).startsWith("0")) {
			i++;
		}
		return bcdLL(String.valueOf(code.length() - i), len);
	}

	/**
	 * 后添加的卡号长度计算 15/11/2017
	 * 
	 * @param code
	 *            左补零后的卡号
	 * @param length
	 *            卡号长度的长度
	 * @return
	 */
	public static byte[] bcdLL(String code, int length) {
		byte[] bt = new byte[length];
		int i = 0;
		while (code.substring(i).startsWith("0")) {
			i++;
		}
		int strSize = code.length() - i;
		for (int j = length - 1; j >= 0; j--) {
			try {
				bt[j] = String.valueOf(strSize % 10).getBytes("GBK")[0];
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strSize = strSize / 10;
		}
		return bt;
	}

	/**
	 * 十进制code 转对应ascii字符串 16/11/2017
	 * 
	 * @param code
	 * @param length
	 * @return
	 */
	public static String ascii(byte[] code) {
		StringBuffer str = new StringBuffer();
		for (byte b : code) {
			str.append(String.valueOf((char) b));
		}
		return str.toString();
		// return new String(ascii(new String(code)));
	}

	/**
	 * 十进制code 转对应ascii字符串 16/11/2017
	 * 
	 * @param code
	 * @param length
	 * @return
	 */
	public static byte[] ascii(String code) {
		StringBuffer str = new StringBuffer();
		char[] chars = code.toCharArray();
		for (char c : chars) {
			str.append(Integer.toBinaryString((int) c));
		}
		// return str.toString().toCharArray();
		return str.toString().getBytes();
	}

	/**
	 * byte to int
	 * 
	 * @param bys
	 * @return
	 */
	public static String byteToInt(byte[] bys) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bys) {
			int temp = b >= 0 ? b : 256 + b;
			// 若byte小于10 添0补齐，因为byte长度可能大于1
			if (temp < 10)
				sb.append("0");
			sb.append(temp);
		}
		return sb.toString();
	}
	
	/**
	 * real int to byte , 123 -> ['31','32','33']
	 * @param in
	 * @return
	 */
	public static String intToByte(int in){
		StringBuffer sb = new StringBuffer();
		try {
			 for (byte by : String.valueOf(in).getBytes("GBK")) {
				 sb.append(String.valueOf(Integer.toHexString(by)));
			}
		} catch (UnsupportedEncodingException e) {
			throw new Simple8583Exception("int 转换 byte 时遇到错误。");
		} finally {
			return sb.toString();
		}
	}

	/**
	 * int to byte 打包时方法，
	 * 
	 * @param in
	 *            待处理数
	 * @param by
	 *            标识符,1时属性为HeaderFlagandVersion,0为其他属性
	 * @return
	 */
	public static byte[] intToByte(String in, int by) {
		byte[] byt;
		if (by == 0) {
			byt = new byte[in.length() / 2];
			for (int i = 0; i < in.length() / 2; i++) {
				try{
				byt[i] = (byte) Integer
						.parseInt(in.substring(i * 2, i * 2 + 2));
				} catch (NumberFormatException e) {
					
				}
			}
		} else {
			byt = new byte[1];
			byt[0] = (byte) (Integer.parseInt(in, 16));
		}
		return byt;
	}

	/**
	 * add fill to origStr in left, add length times.
	 * 
	 * @param origStr
	 * @param length
	 * @param fill
	 * @return
	 */
	public static String addBlankLeft(String origStr, int length, String fill) {
		if (length <= 0) {
			return origStr;
		}
		StringBuffer accum = new StringBuffer();
		for (int i = 0; i < length; i++) {
			accum.append(fill);
		}
		accum.append(origStr);
		return accum.toString();
	}

	/**
	 * add fill to origStr in right, add length times.
	 * 
	 * @param origStr
	 * @param length
	 * @param fill
	 * @return
	 */
	public static String addBlankRight(String origStr, int length, String fill) {
		if (length <= 0) {
			return origStr;
		}
		StringBuffer accum = new StringBuffer(origStr);
		for (int i = 0; i < length; i++) {
			accum.append(fill);
		}
		return accum.toString();
	}
}
