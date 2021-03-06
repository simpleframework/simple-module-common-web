package net.simpleframework.module.common.web.page;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectBean;
import net.simpleframework.mvc.template.lets.Tabs_BlankPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AbstractMgrTPage extends Tabs_BlankPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		if (pp.isLmanager()) {
			addComponentBean(pp, "AbstractMgrTPage_orgSelect", DeptSelectBean.class).setOrg(true)
					.setClearAction("false")
					.setJsSelectCallback("$Actions.reloc('orgId=' + selects[0].id); return false;");
		}
	}

	protected SpanElement createOrgElement(final PageParameter pp) {
		final PermissionDept org = getPermissionOrg(pp);
		return new SpanElement(org.exists() ? org.getText() : $m("AbstractMgrTPage.0"))
				.setStyle("color: #654; font-size: 11.5pt;");
	}

	protected LinkButton createOrgCancelBtn(final PageParameter pp) {
		return LinkButton.of($m("AbstractMgrTPage.2")).setOnclick("$Actions.reloc('orgId=none');");
	}

	protected LinkButton createOrgSelectBtn(final PageParameter pp) {
		return LinkButton.of($m("AbstractMgrTPage.1"))
				.setOnclick("$Actions['AbstractMgrTPage_orgSelect']();");
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ElementList el = ElementList.of(createOrgElement(pp));
		if (pp.isLmanager()) {
			LinkButton cancelBtn;
			if (getPermissionOrg(pp).exists() && (cancelBtn = createOrgCancelBtn(pp)) != null) {
				el.append(SpanElement.SPACE).append(cancelBtn);
			}
			final LinkButton selectBtn = createOrgSelectBtn(pp);
			if (selectBtn != null) {
				el.append(SpanElement.SPACE).append(selectBtn);
			}
		}
		return el;
	}
}
