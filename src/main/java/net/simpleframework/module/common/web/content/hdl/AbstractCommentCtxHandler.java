package net.simpleframework.module.common.web.content.hdl;

import net.simpleframework.ado.bean.ITreeBeanAware;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.module.common.content.AbstractComment;
import net.simpleframework.module.common.content.ICommentService;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.comments.ctx.CommentCtxHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractCommentCtxHandler<T extends AbstractComment>
		extends CommentCtxHandler<T> {

	@Override
	protected abstract ICommentService<T> getBeanService();

	@Override
	public IDataQuery<?> comments(final ComponentParameter cp) {
		return getBeanService().queryComments(getOwnerId(cp));
	}

	protected T createComment(final ComponentParameter cp) {
		final T t = getBeanService().createBean();
		t.setContentId(getOwnerId(cp));
		t.setUserId(cp.getLoginId());
		t.setCcomment(cp.getParameter(PARAM_COMMENT));
		final AbstractComment parent = getBeanService().getBean(cp.getParameter(PARAM_PARENTID));
		if (parent instanceof ITreeBeanAware) {
			((ITreeBeanAware) t).setParentId(parent.getId());
		}
		return t;
	}
}
