package net.simpleframework.module.common.web.page;

import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse.IVal;
import net.simpleframework.mvc.ctx.permission.IPagePermissionHandler;
import net.simpleframework.mvc.template.lets.Tabs_BlankPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AbstractMgrTPage extends Tabs_BlankPage {

	public static PermissionDept getOrg(final PageParameter pp) {
		return pp.getCache("@org", new IVal<PermissionDept>() {
			@Override
			public PermissionDept get() {
				PermissionDept org = null;
				final IPagePermissionHandler hdl = pp.getPermission();
				final PermissionUser login = pp.getLogin();
				if (login.isManager()) {
					org = hdl.getDept(pp.getParameter("orgId"));
				}
				if (org == null) {
					org = hdl.getDept(login.getDept().getDomainId());
				}
				return org;
			}
		});
	}
}
