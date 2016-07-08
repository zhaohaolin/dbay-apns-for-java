/*
 * CopyRight (c) 2012-2015 Hikvision Co, Ltd. All rights reserved.
 * Filename:    ErrorProcessHandler.java
 * Creator:     joe.zhao(zhaohaolin@hikvision.com.cn)
 * Create-Date: 上午11:29:48
 */
package com.dbay.apns4j;

/**
 * TODO
 * 
 * @author joe.zhao(zhaohaolin@hikvision.com.cn)
 * @version $Id: ErrorProcessHandler, v 0.1 2016年7月8日 上午11:29:48 Exp $
 */
public interface ErrorProcessHandler {
	
	void process(int id, int status, String token);
	
}
