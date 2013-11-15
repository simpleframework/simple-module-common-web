package net.simpleframework.module.common.web.team.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.module.common.team.ITeamService;
import net.simpleframework.module.common.team.Team;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.DictInput;
import net.simpleframework.mvc.common.element.EInputType;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.userselect.UserSelectBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractTeamMgrPage<T extends Team> extends OneTableTemplatePage {

	protected abstract ITeamService<T> getTeamService();

	protected abstract ID getOwnerId(PageParameter pp);

	protected String getOwnerIdKey() {
		return "ownerId";
	}

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addTablePagerBean(pp);

		addComponentBean(pp, "AbstractTeamMgrPage_userselect", UserSelectBean.class);

		// delete
		addDeleteAjaxRequest(pp, "AbstractTeamMgrPage_delete");
	}

	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			getTeamService().delete(ids);
		}
		return new JavascriptForward("$Actions['AbstractTeamMgrPage_tbl']();");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = addTablePagerBean(pp, "AbstractTeamMgrPage_tbl",
				TeamTable.class).setEditable(true).setShowEditableBtn(false).setDblclickEdit(false);
		tablePager
				.addColumn(
						new TablePagerColumn(COL_USERID, $m("AbstractTeamMgrPage.1"), 125)
								.setTextAlign(ETextAlign.left))
				.addColumn(new TablePagerColumn(COL_ROLE, $m("AbstractTeamMgrPage.2"), 125) {
					@Override
					protected Option[] getFilterOptions() {
						return Option.from(getTeamService().getTeamRoles());
					}
				}).addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(120));
		return tablePager;
	}

	protected IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
		final ID ownerId = getOwnerId(cp);
		if (ownerId != null) {
			cp.addFormParameter(getOwnerIdKey(), ownerId);
		}
		return getTeamService().queryByOwner(ownerId);
	}

	@SuppressWarnings("unchecked")
	protected JavascriptForward doRowSave(final PageParameter pp,
			final Map<String, Map<String, Object>> insertRows,
			final Map<String, Map<String, Object>> updateRows) {
		final ID ownerId = getOwnerId(pp);
		final ITeamService<T> tService = getTeamService();
		for (final Map<String, Object> row : insertRows.values()) {
			final T team = tService.createBean();
			final Object userId = row.get(COL_USERID);
			if (!StringUtils.hasObject(userId)) {
				continue;
			}
			team.setOwnerId(ownerId);
			team.setCreateDate(new Date());
			team.setUserId(ID.of(userId));
			team.setRole((String) row.get(COL_ROLE));
			team.setDescription((String) row.get(TablePagerColumn.DESCRIPTION));
			tService.insert(team);
		}
		for (final Map.Entry<String, Map<String, Object>> e : updateRows.entrySet()) {
			final T team = tService.getBean(e.getKey());
			if (team != null) {
				final Map<String, Object> row = e.getValue();
				team.setUserId(ID.of(row.get(COL_USERID)));
				team.setRole((String) row.get(COL_ROLE));
				team.setDescription((String) row.get(TablePagerColumn.DESCRIPTION));
				tService.update(team);
			}
		}
		return null;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(
				new LinkButton($m("AbstractTeamMgrPage.0"))
						.setOnclick("$Actions['AbstractTeamMgrPage_tbl'].add_row();"),
				SpanElement.SPACE,
				LinkButton.deleteBtn().setOnclick(
						"$Actions['AbstractTeamMgrPage_tbl'].doAct('AbstractTeamMgrPage_delete');"));
	}

	public static class TeamTable extends AbstractDbTablePagerHandler {

		protected AbstractTeamMgrPage<?> get(final PageParameter pp) {
			return (AbstractTeamMgrPage<?>) AbstractMVCPage.get(pp);
		}

		@Override
		public AbstractElement<?> toRowEditorHTML(final ComponentParameter cp,
				final TablePagerColumn column, final String rowId, final String elementName,
				final Object rowData) {
			final AbstractElement<?> element = super.toRowEditorHTML(cp, column, rowId, elementName,
					rowData);
			final String columnName = column.getColumnName();
			if (COL_USERID.equals(columnName)) {
				final InputElement hidden = InputElement.hidden().setName(elementName)
						.setId(ObjectUtils.hashStr(elementName));
				final DictInput tb = (DictInput) new DictInput(ObjectUtils.hashStr(elementName
						+ "-text")).setDictComponent("AbstractTeamMgrPage_userselect").setHiddenField(
						hidden);
				Object val;
				if (rowData != null && ((val = BeanUtils.getProperty(rowData, columnName)) != null)) {
					hidden.setText(column.objectToString(val));
					tb.setText(cp.getUser(val));
				}
				return tb;
			} else if (TablePagerColumn.DESCRIPTION.equals(columnName)) {
				final String id = ObjectUtils.hashStr(elementName);
				return new BlockElement().addElements(((InputElement) element)
						.setInputType(EInputType.textarea).setStyle("width: 98%; min-height: 36px;")
						.setRows(2).setAutoRows(true).setId(id));
			}
			return element;
		}

		@Override
		public JavascriptForward doRowSave(final ComponentParameter cp,
				final Map<String, Map<String, Object>> insertRows,
				final Map<String, Map<String, Object>> updateRows) {
			return get(cp).doRowSave(cp, insertRows, updateRows);
		}

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return get(cp).createDataObjectQuery(cp);
		}

		@Override
		public Object getRowBeanById(final ComponentParameter cp, final Object id) {
			return get(cp).getTeamService().getBean(id);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Team team = (Team) dataObject;
			final KVMap kv = new KVMap();
			kv.put(COL_USERID, toIconUser(cp, team.getUserId()));
			kv.put(COL_ROLE, get(cp).getTeamService().getTeamRole(team.getRole()));
			kv.put(TablePagerColumn.DESCRIPTION, HtmlUtils.convertHtmlLines(team.getDescription()));
			kv.put(
					TablePagerColumn.OPE,
					ButtonElement.editBtn().setOnclick(
							"$Actions['AbstractTeamMgrPage_tbl'].edit_row(this);")
							+ SpanElement.SPACE.toString()
							+ ButtonElement.deleteBtn().setOnclick(
									"$Actions['AbstractTeamMgrPage_delete']('id=" + team.getId() + "');"));
			return kv;
		}
	}

	public static final String COL_USERID = "userId";
	public static final String COL_ROLE = "role";
}
