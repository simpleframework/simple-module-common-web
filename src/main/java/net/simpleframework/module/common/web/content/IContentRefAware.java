package net.simpleframework.module.common.web.content;

import net.simpleframework.ctx.IModuleRef;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IContentRefAware {

	/**
	 * 获取收藏的引用
	 * 
	 * @return
	 */
	IModuleRef getFavoriteRef();

	IModuleRef getLogRef();

	IModuleRef getPDFRef();
}
