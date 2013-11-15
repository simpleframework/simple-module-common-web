package net.simpleframework.module.common.web.content;

import static net.simpleframework.common.I18n.$m;

import java.util.Arrays;
import java.util.Iterator;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx;
import net.simpleframework.ctx.common.bean.ETimePeriod;
import net.simpleframework.module.common.content.AbstractContentBean;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.ListRows;
import net.simpleframework.mvc.template.struct.Pagelet;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class PageletCreator<T extends AbstractContentBean> extends ObjectEx {

	protected abstract ListRowHandler<T> getDefaultListRowHandler();

	public ListRows create(final IDataQuery<?> dq, final ListRowHandler<T> handler) {
		return create(DataQueryUtils.toIterable(dq), handler);
	}

	public ListRows create(final IDataQuery<?> dq) {
		return create(DataQueryUtils.toIterable(dq));
	}

	public ListRows create(final Iterable<?> it) {
		return create(it, getDefaultListRowHandler());
	}

	public ListRows create(final Iterable<?> it, final ListRowHandler<T> handler) {
		final ListRows items = ListRows.of();
		if (it != null) {
			final Iterator<?> it2 = it.iterator();
			int i = 0;
			while (i++ < handler.getSize() && it2.hasNext()) {
				final T t = handler.toBean(it2.next());
				if (handler.isVisible(t)) {
					items.append(handler.toListRow(t));
				}
			}
		}
		return items;
	}

	public Pagelet getHistoryPagelet(final PageParameter pp, final String cookie) {
		final String[] arr = StringUtils.split(pp.getCookie(cookie), "|");
		return new Pagelet(new CategoryItem($m("PageletCreator.3")), create(
				arr == null ? null : Arrays.asList(arr), getDefaultListRowHandler()).setDotIcon(
				EImageDot.dot2));
	}

	public TabButtons createTimePeriodTabs() {
		return createTimePeriodTabs(null);
	}

	public TabButtons createTimePeriodTabs(String qs) {
		if (qs == null) {
			qs = "";
		}
		if (StringUtils.hasText(qs)) {
			qs = "&" + qs;
		}
		final TabButton tab1 = new TabButton($m("PageletCreator.0"))
				.setOnclick("$UI.doPageletTab('time=" + ETimePeriod.week.name() + qs + "', this);");
		final TabButton tab2 = new TabButton($m("PageletCreator.1"))
				.setOnclick("$UI.doPageletTab('time=" + ETimePeriod.month.name() + qs + "', this);");
		final TabButton tab3 = new TabButton($m("PageletCreator.2"))
				.setOnclick("$UI.doPageletTab('time=" + ETimePeriod.year.name() + qs + "', this);");
		return TabButtons.of(tab1, tab2, tab3);
	}
}
