package com.worldline.isa.factory;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.worldline.isa.model.IsoField;
import com.worldline.isa.model.IsoPackage;
import com.worldline.isa.util.EncodeUtil;
import com.worldline.isa.util.encrypt.TripleDES;


/**
 * <p>报文组装抽象类.</p>
 *
 * @author Magic Joey
 * @version IsoMsgFactory.java 1.0 Created@2014-07-09 17:41 $
 */
public class IsoMsgFactory extends AbstractIsoMsgFactory{
	
	private  static IsoMsgFactory isoMsgFactory=new IsoMsgFactory();
	
	protected IsoMsgFactory(){
		
	}

	public static IsoMsgFactory getInstance(){
		return isoMsgFactory;
	}


	// 编码规则
	// 2 Primary Account Number
	// 3 Processing Code
	// 4 Amount, Transaction
	// 11 System Trace Audit Number
	// 12 Time, Local Transaction
	// 13 Date, Local Transaction
	// 49 Currency Code, Transaction
	// 38 Authorization Identification Response
	// 39 Response Code
	// 41 Card Acceptor Terminal Identification
	@Override
	protected byte[] mac(IsoPackage isoPackage) throws Exception {
		String[] md5Array = { "2", "3", "4", "11", "12", "13", "49", "38",
				"39", "41" };
		StringBuffer accum = new StringBuffer();
		for (String key : md5Array) {
			IsoField field = isoPackage.getIsoField(key);
			if (field == null) {
				continue;
			}
			if (field.getValue() == null) {
				continue;
			}
			if (field.getId().equals("49")) {
				accum.append(field.getValue().substring(1));
			} else {
				accum.append(field.getValue());
			}
		}
		String original = accum.toString();
		String val = TripleDES.getMac(macKey, original);
		return EncodeUtil.bcd(val);
	}


	//生成前两个字节的长度位
	//根据约定不同需要对此方法进行重写
	@Override
	protected byte[] msgLength(int length) {
		byte[] rByte = new byte[4];
		for (int i = 3; i >= 0 ; i--, length /= 10) {
			try {
				rByte[i] = String.valueOf(length % 10).getBytes("GBK")[0];
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return rByte;
	}

	//重写MAC校验方法，验证失败则抛出运行时异常
	@Override
	protected void macValidate(IsoPackage isoPackage, Map<String, String> map) {
		String mac = null;
		try {
			mac =  EncodeUtil.hex(mac(isoPackage));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String returnMac = EncodeUtil.hex(EncodeUtil.binary(map.get("64")));//Binary编码转换为hex编码
		if(!(mac.substring(0,8).equals(returnMac.substring(0,8)))){
			throw new RuntimeException("MAC校验失败，返回值"+returnMac+",计算值"+mac);
		}
	}

}