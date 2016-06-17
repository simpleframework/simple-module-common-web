package net.simpleframework.module.common.web.content;

import java.util.Date;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.service.ado.IADOBeanService;
import net.simpleframework.module.common.content.AbstractContentBean;
import net.simpleframework.module.common.content.AbstractContentBean.EContentStatus;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.struct.ListRow;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ListRowHandler<T extends AbstractContentBean> {

	protected boolean isVisible(final T bean) {
		return bean != null && bean.getStatus() == EContentStatus.publish;
	}

	public int getSize() {
		return 9;
	}

	public boolean isShowTip() {
		return true;
	}

	protected String[] getShortDesc(final T bean) {
		return null;
	}

	protected abstract String getHref(PageParameter pp, T bean);

	protected abstract IADOBeanService<T> getBeanService();

	@SuppressWarnings("unchecked")
	protected T toBean(final Object o) {
		if (o instanceof AbstractContentBean) {
			return (T) o;
		} else if (o instanceof Map) {
			return getBeanService().getBean(((Map<?, ?>) o).get("id"));
		} else {
			return getBeanService().getBean(o);
		}
	}

	public ListRow toListRow(final PageParameter pp, final T bean) {
		final String href = getHref(pp, bean);
		final ListRow lr = new ListRow(bean.getTopic()).setHref(href)
				.setShortDesc(getShortDesc(bean));
		if (isShowTip()) {
			String desc = bean.getDescription();
			if (StringUtils.hasText(desc)) {
				desc = StringUtils.substring(desc, 240, true);
				desc = HtmlUtils.convertHtmlLines(desc);
			} else {
				desc = HtmlUtils.truncateHtml(bean.doc(), 240);
			}
			lr.setTooltip(toTipHTML(lr, bean.getTopic(), desc, bean.getCreateDate()));
		}
		return lr;
	}

	protected String toTipHTML(final ListRow lr, final String topic, final String desc,
			final Date createDate) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<h4>").append(topic).append("</h4>");
		sb.append("<p>").append(desc).append("</p>");
		sb.append("<div style='text-align: right;'>");
		sb.append("<span style='margin-right: 6px;'>").append(Convert.toDateTimeString(createDate))
				.append("</span>");
		sb.append("<a target='_blank' href='").append(lr.getHref()).append("'>#(ListRowHandler.0)")
				.append(HtmlConst.NBSP).append(HtmlConst.RAQUO).append("</a></div>");
		return sb.toString();
	}
}
