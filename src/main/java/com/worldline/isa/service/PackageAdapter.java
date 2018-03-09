package com.worldline.isa.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBException;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static Logger logger = LoggerFactory.getLogger(PackageAdapter.class);

	static final PackageAdapter _instance = new PackageAdapter();

	public static PackageAdapter instance() {
		return _instance;
	}

	private IsoMsgFactory factory;

	protected String macKey;

	ConcurrentMap<String, XmlReader> configMap = new ConcurrentHashMap<String, XmlReader>();

	static final String DEFAULT_CONFIG = "iso8583-DPI";

	public PackageAdapter() {
		macKey = "8F9B831A51FB7908";
		factory = IsoMsgFactory.getInstance();
		getConfig(DEFAULT_CONFIG);
	}

	XmlReader getConfig(String configName) {
		return configMap.computeIfAbsent(configName, this::loadConfig);
	}

	String getConfigPath(String configName) {
		return configName + ".xml";
	}

	XmlReader loadConfig(String configName) {
		try {
			String xmlpath = getConfigPath(configName);
			return new XmlReader(xmlpath);
		} catch (JAXBException | IOException e) {
			throw new RuntimeException("isa.err_invalid_config::" + configName, e);
		}
	}

	/**
	 * 报文封包方法适配器
	 * 
	 * @param dataMap
	 *            待封包数据
	 * @param configName
	 *            卡商规则 @Return：byte[] 返回byte数组
	 */
	public static byte[] packAdapter(Map<String, String> dataMap, String configName, IsoPackage pendPack) {
		return instance().packByConfig(dataMap, configName, pendPack);
	}

	public static byte[] packAdapter(Map<String, String> dataMap, IsoPackage pendPack) {
		return instance().packByConfig(dataMap, DEFAULT_CONFIG, pendPack);
	}

	public byte[] packByConfig(Map<String, String> dataMap, String configName, IsoPackage pendPack) {
		byte[] sendData = null;
		try {
			// factory.setMacKey(macKey);
			 XmlReader config = this.getConfig(configName);
			 IsoPackage pack = config.getIsoConfig().get("iso8583");
			 pendPack = pack.deepClone();
			
			sendData = factory.pack(dataMap, pendPack);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

		return sendData;
	}

	/**
	 * 报文解包方法适配器
	 * 
	 * @param buf
	 *            待解包byte数据
	 * @param xmlpath
	 *            报文域规则
	 * @Return：Map<String,String> 返回解包后map数据
	 */
	public static Map<String, String> unpackAdapter(byte[] buf, String configName) throws Exception {
		return instance().unpackByConfig(buf, configName);
	}

	public Map<String, String> unpackByConfig(byte[] buf, String configName) throws Exception {
		XmlReader config = this.getConfig(configName);

		// 获取mti 需提前知道mti 位置 @ArrayIndexOutOfBoundsException
		String mti = factory.getMti(buf, 50, 4);
		// 预留get ,
		IsoPackage pack = config.getIsoConfig().get("iso8583");
		
		return factory.unpack(buf, pack);
	}
}
