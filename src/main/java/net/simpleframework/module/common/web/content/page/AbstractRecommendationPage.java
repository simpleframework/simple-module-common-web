package net.simpleframework.module.common.web.content.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;

import net.simpleframework.common.NumberUtils;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.common.LogDesc;
import net.simpleframework.module.common.content.AbstractRecommendContentBean;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractRecommendationPage<T extends AbstractRecommendContentBean> extends
		FormTableRowTemplatePage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.number, "#r_recommendationDuration"),
				new Validator(EValidatorMethod.required, "#r_description"));
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 65;
	}

	protected abstract IDbBeanService<T> getBeanService();

	protected abstract T getBean(PageParameter pp);

	@SuppressWarnings("unchecked")
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final T bean = getBean(cp);
		final int r = cp.getIntParameter("r_recommendation");
		if (bean != null && r != bean.getRecommendation()) {
			bean.setRecommendation(r);
			if (r > 0) {
				bean.setRecommendationDate(new Date());
				bean.setRecommendationDuration((int) (cp.getDoubleParameter("r_recommendationDuration") * 60 * 60));
			} else {
				// 取消推荐
				bean.setRecommendationDate(null);
				bean.setRecommendationDuration(0);
			}
			LogDesc.set(bean, cp.getParameter("r_description"));
			getBeanService().update(
					new String[] { "recommendation", "recommendationDate", "recommendationDuration" },
					bean);
		}
		return super.onSave(cp);
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final T bean = getBean(pp);

		final ArrayList<Option> al = new ArrayList<Option>();
		for (int i = 0; i <= 5; i++) {
			al.add(new Option(String.valueOf(i)).setSelected(bean.getRecommendation() == i));
		}
		final InputElement topicId = InputElement.hidden("topicId").setText(bean.getId());
		final InputElement r_recommendation = InputElement.select("r_recommendation").addElements(
				al.toArray(new Option[al.size()]));
		final InputElement r_recommendationDuration = new InputElement("r_recommendationDuration")
				.setText(NumberUtils.format((double) bean.getRecommendationDuration() / (60 * 60)))
				.addStyle("width: 40px;");
		final InputElement r_description = InputElement.textarea("r_description").setRows(3);
		final InputElement r_recommendationDate = new InputElement().setText(
				bean.getRecommendationDate()).setReadonly(true);

		final TableRow r1 = new TableRow(new RowField($m("AbstractRecommendationPage.0"),
				r_recommendation, topicId), new RowField($m("AbstractRecommendationPage.1"),
				r_recommendationDuration, new SpanElement($m("AbstractRecommendationPage.2"))));
		final TableRow r2 = new TableRow(new RowField($m("Description"), r_description));
		final TableRow r3 = new TableRow(new RowField($m("AbstractRecommendationPage.4"),
				r_recommendationDate));
		return TableRows.of(r1, r2, r3);
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(new BlockElement().addStyle("color: #888;").addElements(
				new BlockElement().setText($m("AbstractRecommendationPage.3"))));
	}
}
