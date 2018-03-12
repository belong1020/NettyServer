package com.worldline.isa.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.worldline.isa.key.SimpleConstants;
import com.worldline.isa.util.EncodeUtil;
import com.worldline.isa.util.SimpleUtil;

/**
 * <p>
 * 报文域详情.
 * </p>
 *
 * @author Magic Joey
 * @version IsoField.java 1.0 Created@2014-07-10 15:49 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IsoField implements Serializable {

	@XmlAttribute(name = "id", required = true)
	private String id;

	@XmlAttribute(name = "type", required = true)
	private String type;

	// 该属性为字节长度
	@XmlAttribute(name = "length")
	private int length = 0;

	// 字符值的value
	private String value;

	// 字节数组值的value
	private byte[] byteValue;

	// 域类型
	private IsoType isoType;

	// 该域是否被使用
	private boolean checked = false;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// 获取本域的IsoType
	public IsoType getIsoType() {
		if (this.isoType == null) {
			this.isoType = IsoType.valueOf(this.type);
		}
		return isoType;
	}

	public String getValue() {
		return value;
	}

	public boolean isChecked() {
		return checked;
	}

	public byte[] getByteValue() {
		return byteValue;
	}

	/**
	 * 保存报文byte value 类型数据,
	 * 
	 * @param bts
	 * @throws UnsupportedEncodingException
	 * @Return：void
	 */
	public void setByteValue(byte[] bts) throws UnsupportedEncodingException {
		this.byteValue = bts;
		this.checked = true;

		switch (this.getId()) {
		case SimpleConstants.BIT_MAP:
			this.value = EncodeUtil.ascii(bts);
			return;
		case SimpleConstants.HEADERFLAGANDVERSION:
			// byte 16进制值为real值，byteToInt 出口为十进制值，需再次转换
			this.value = Integer.toHexString(Integer.parseInt(EncodeUtil.byteToInt(bts)));
			return;
		case SimpleConstants.HEADERLENGTH:
		case SimpleConstants.RESERVEDFORUSE:
		case SimpleConstants.BATCHNUMBER:
		case SimpleConstants.USERINFORMATION:
			// byte 16进制非real值， 10进制值为real ，直转int即可，
			this.value = EncodeUtil.byteToInt(bts);
			return;
		case SimpleConstants.MSG_LENGTH:
			this.value = EncodeUtil.ascii(bts);
			this.checked = false;// 无效设置
			break;
		}
		switch (this.getIsoType()) {
		case BINARY:
			this.value = EncodeUtil.binary(bts);
			break;
		case NUMERIC:
		case LLVAR_NUMERIC:
			// 替换于20/Nov/2017
			// this.value = EncodeUtil.hex(bts);
			this.value = EncodeUtil.ascii(bts);
			break;
		case CHAR:
		case LLVAR:
		case LLLVAR:
			this.value = new String(bts, SimpleConstants.ENCODING);
			break;
		default:
			this.checked = false;// 无效设置
			break;
		}
		// 子域不直接参与的打包
		if (this.id.indexOf(".") > 0) {
			this.checked = false;
		}
	}

	/**
	 * 保存报文real value , String 类型数据,
	 * 
	 * @param value
	 * @throws UnsupportedEncodingException
	 * @Return：void
	 */
	public void setValue(String value) throws UnsupportedEncodingException {
		// 应用数据域被选中
		this.checked = true;
		this.value = value;
		// 格式化
		format(this.value, this.length);
		if (this.value != null) {
			switch (this.getIsoType()) {
			case BITMAP:
				this.byteValue = EncodeUtil.bcd(this.value);
				break;
			case BINARY:
				this.byteValue = EncodeUtil.binary(this.value);
				break;
			case CHAR:
				if (!this.isAppData()) {
					if (this.getId().equals(SimpleConstants.HEADERFLAGANDVERSION)) {
						this.byteValue = EncodeUtil.intToByte(this.value, 1);
					} else {
						this.byteValue = EncodeUtil.intToByte(this.value, 0);
					}
					break;
				}
			case NUMERIC:
				if (this.getId().equals(SimpleConstants.MSG_LENGTH)) {
					this.checked = false;
				}
			case LLVAR_NUMERIC:
			case LLVAR:
			case LLLVAR:
				this.byteValue = this.value.getBytes(SimpleConstants.ENCODING);
				break;
			default:
				this.checked = false;
				break;
			}
		}
		// 子域不直接参与的打包
		if (this.id.matches("(.*)\\.(.*)")) {
			this.checked = false;
		}

	}

	/**
	 * 
	 * @param value
	 *            value
	 * @param length
	 *            长度
	 * @throws UnsupportedEncodingException
	 * @Return：void
	 */
	public void format(String value, int length) throws UnsupportedEncodingException {
		if (this.isoType == null) {
			this.isoType = IsoType.valueOf(this.type);
		}
		switch (this.isoType) {
		case BITMAP: {
			// 报文解包后 可以在此补零
			if (value.length() > length) {
				this.value = this.value.substring(0, length);
			} else if (value.length() < length) {
				// 将缺少的部分补全空格
				this.value = EncodeUtil.addBlankLeft(this.value,
						this.length - value.getBytes(SimpleConstants.ENCODING).length, " ");
			}
			break;
		}
		case CHAR: {
			// 两字符打包为1字节byte数据
			if (value.length() / 2 > length) {
				this.value = this.value.substring(0, length);
			} else if (value.length() < length * 2) {
				// 将缺少的部分补全空格
				this.value = EncodeUtil.addBlankLeft(this.value,
						this.length - value.getBytes(SimpleConstants.ENCODING).length, " ");
			}
			break;
		}
			// LLVAR和LLLVAR类型的数据不格式化
		case LLVAR:
		case LLLVAR:
			break;
		case LLVAR_NUMERIC:
			if (this.value.length() % 2 == 1) {
				this.value = EncodeUtil.addBlankLeft(this.value, 1, "0");
			}
			break;
		case NUMERIC: {
			if (this.value.length() > length) {
				throw new IllegalArgumentException("数据域 " + this.id + "长度超出，值为:" + this.value + "，约定长度" + length);
			} else if (this.value.length() < length) {
				this.value = EncodeUtil.addBlankLeft(this.value, length - this.value.length(), "0");
			}
			break;
		}
		case BINARY: {
			int len = this.value.length();
			if (len < 8 * this.length) {
				this.value = EncodeUtil.addBlankLeft(this.value, 8 * length - len, "0");
			} else if (len > 8 * this.length) {
				this.value = this.value.substring(0, this.length * 8);
			}
			break;
		}
		default:
			throw new IllegalArgumentException("不支持的参数类型：" + this.isoType);
		}
	}

	@Override
	public String toString() {
		StringBuilder accum = new StringBuilder();
		accum.append("id=").append(this.id).append(",type=").append(this.type).append(",value=").append(this.value);
		return accum.toString();
	}

	// 该域是否为1~64/1~128的数据域
	public boolean isAppData() {
		return SimpleUtil.notLetterString(this.id);
	}

}
