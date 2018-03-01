package com.worldline.isa.service;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.worldline.isa.Listener.Simple8583Exception;
import com.worldline.isa.factory.IsoMsgFactory;
import com.worldline.isa.factory.XmlReader;
import com.worldline.isa.model.IsoPackage;

/**
 * 报文解包封包处理类
 * 
 * @author Belong.
 */
public class PackageAdapter {

	private static Logger logger = Logger.getLogger(PackageAdapter.class);  
	
	private static IsoMsgFactory factory;
	private static XmlReader xmlReader;
	protected static String macKey;
	
	static {
		macKey = "8F9B831A51FB7908";
		factory = IsoMsgFactory.getInstance();
		String xmlpath = "iso8583-DPI.xml";
		try {
			xmlReader = new XmlReader(xmlpath);
		} catch (JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 报文封包方法适配器
	 * 
	 * @param dataMap	待封包数据
	 * @Return：byte[]	返回byte数组
	 */
	public static byte[] packAdapter(Map<String, String> dataMap) {
		byte[] sendData = null;
		try {
			factory.setMacKey(macKey);

			IsoPackage pack = xmlReader.getIsoConfig().get("iso8583");
			sendData = factory.pack(dataMap, pack);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			new Simple8583Exception("PackageAdapter : "
					+ "ClassNotFoundException");
		} catch (IOException e) {
			e.printStackTrace();
			new Simple8583Exception("PackageAdapter : " + "IOException");
		}

		return sendData;
	}

	/**
	 * 报文解包方法适配器
	 * 
	 * @param buf			待解包byte数据
	 * @param xmlpath		报文域规则
	 * @Return：Map<String,String>	返回解包后map数据
	 */
	public static Map<String, String> unpackAdapter(byte[] buf, String xmlpath) throws Exception{
		/*try {*/
			// 获取报文长度 -4 报文中包含两个 报文长度tip
			int length = buf.length - 4;

			// 获取mti 需提前知道mti 位置 @ArrayIndexOutOfBoundsException
			String mti = factory.getMti(buf, 50, 4);
			
			// 获取xml 中mti 对应信息
			xmlReader = new XmlReader(xmlpath);

			// 预留get ,
			IsoPackage pack = xmlReader.getIsoConfig().get("iso8583");
			return factory.unpack(buf, pack);

		/*} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("异常消息，无法识别bitmap 信息。");
//			throw new Simple8583Exception();
		} catch (Exception e) {
			// e.printStackTrace();
//			System.err.println(e.getMessage());
			logger.error("通讯异常");
//			throw new Simple8583Exception("通讯异常");
		}
		
		return null;*/
	}

}
