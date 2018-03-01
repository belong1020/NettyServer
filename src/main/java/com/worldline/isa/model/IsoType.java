package com.worldline.isa.model;

/**
 * <p>报文域类型,该类待修改，以使本框架更通用.</p>
 *  TODO
 * @author Magic Joey
 * @version Simple8583Test.java 1.0 @2014-07-10 13:59 $
 */
public enum IsoType {

    //定义了8583协议中的几种数据格式，注：时间类型以及金额都当做NUMERIC类型处理
    //acs编码字符串为CHAR,
	//二进制类型为BINARY
	//,数字及bcd编码的格式为NUMERIC,
	//LLVAR为前置2字节长度字段,
	//LLLVAR为前置3字节长度字段
	//LLVAR_NUMERIC：2字节变长的数字类型(BCD编码)
	//BITMAP:BITMAP专用类型,因iso8583 Bitmap打包长度8/16字节， 4*2*8(*2) 如 F2 3E 64 C1 A8 E0 98 18 00 00 00 00 10 00 00 01
    BINARY,CHAR,NUMERIC,LLVAR,LLLVAR,LLVAR_NUMERIC,BITMAP;

}




