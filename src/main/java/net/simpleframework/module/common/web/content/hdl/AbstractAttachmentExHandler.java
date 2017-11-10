package net.simpleframework.module.common.web.content.hdl;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.bean.AbstractIdBean;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.ImageUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.page.AbstractAttachmentTooltipPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.common.ImageCache;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.attachments.AbstractAttachmentHandler;
import net.simpleframework.mvc.component.ext.attachments.AttachmentUtils;
import net.simpleframework.mvc.component.ext.attachments.IAttachmentHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttachmentExHandler<T extends Attachment, M extends AbstractIdBean>
		extends AbstractAttachmentHandler {

	protected abstract IAttachmentService<T> getAttachmentService();

	protected abstract IDbBeanService<M> getOwnerService();

	protected String getOwnerIdParameterKey() {
		return "ownerId";
	}

	@SuppressWarnings("unchecked")
	public M owner(final PageRequestResponse rRequest) {
		final String pKey = getOwnerIdParameterKey();
		M o = (M) rRequest.getRequestAttr(pKey);
		if (o == null) {
			o = getOwnerService().getBean(rRequest.getParameter(pKey));
			if (o != null) {
				rRequest.setRequestAttr(pKey, o);
			}
		}
		return o;
	}

	@Override
	protected String getCachekey(final ComponentParameter cp) {
		String key = super.getCachekey(cp);
		final M m = owner(cp);
		if (m != null) {
			key += "_" + String.valueOf(m.getId());
		}
		return key;
	}

	@Override
	public ID getOwnerId(final ComponentParameter cp) {
		final M o = owner(cp);
		return o != null ? o.getId() : null;
	}

	@Override
	public Map<String, Object> getFormParameters(final ComponentParameter cp) {
		final Map<String, Object> parameters = super.getFormParameters(cp);
		final M o = owner(cp);
		if (o != null) {
			parameters.put(getOwnerIdParameterKey(), o.getId());
		}
		return parameters;
	}

	@Override
	public AttachmentFile getAttachmentById(final ComponentParameter cp, final String id)
			throws IOException {
		AttachmentFile attachmentFile = getUploadCache(cp).get(id);
		if (attachmentFile == null) {
			final IAttachmentService<T> attachmentService = getAttachmentService();
			final T attachment = attachmentService.getBean(id);
			attachmentFile = attachmentService.createAttachmentFile(attachment);
		}
		return attachmentFile;
	}

	@Override
	public Map<String, AttachmentFile> attachments(final ComponentParameter cp) throws IOException {
		final Map<String, AttachmentFile> attachmentFiles = new LinkedHashMap<>(
				super.attachments(cp));
		final ID ownerId = getOwnerId(cp);
		if (ownerId != null) {
			final IAttachmentService<T> attachmentService = getAttachmentService();
			final int attachtype = getAttachtype(cp);
			final IDataQuery<T> dq = attachtype > -1
					? attachmentService.queryByContent(ownerId, attachtype)
					: attachmentService.queryByContent(ownerId);
			T attachment;
			while ((attachment = dq.next()) != null) {
				final AttachmentFile attachmentFile = attachmentService
						.createAttachmentFile(attachment);
				if (attachmentFile == null) {
					continue;
				}
				attachmentFiles.put(String.valueOf(attachment.getId()), attachmentFile);
			}
		}
		return attachmentFiles;
	}

	@Override
	public void doExchange(final ComponentParameter cp, final String... ids) {
		final List<T> list = new ArrayList<>();
		final IAttachmentService<T> attachService = getAttachmentService();
		for (final String id : ids) {
			final T t = attachService.getBean(id);
			if (t != null) {
				list.add(t);
			}
		}
		@SuppressWarnings("unchecked")
		final T[] arr = (T[]) Array.newInstance(attachService.getBeanClass(), list.size());
		attachService.exchange(list.toArray(arr));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSave(final ComponentParameter cp, final String id, final String topic,
			final int attachtype, final String description) throws Exception {
		final IAttachmentService<T> attachmentService = getAttachmentService();
		final T t = attachmentService.getBean(id);
		if (t != null) {
			t.setTopic(topic);
			if (attachtype > 0) {
				t.setAttachtype(attachtype);
			}
			t.setDescription(description);
			attachmentService.update(t);
		} else {
			super.doSave(cp, id, topic, attachtype, description);
		}
	}

	@Override
	public String getTooltipPath(final ComponentParameter cp) {
		return AbstractMVCPage.url(AttachmentTooltipPage.class,
				AttachmentUtils.BEAN_ID + "=" + cp.hashId());
	}

	@Override
	public IDataQuery<?> queryAttachmentHistory(final ComponentParameter cp) {
		String[] arr = null;
		final String types = cp.getParameter("types");
		if (StringUtils.hasText(types)) {
			cp.addFormParameter("types", types);
			arr = StringUtils.split(types, ";");
		}
		return getAttachmentService().queryByUser(cp.getLoginId(), true, arr);
	}

	@Override
	public Map<String, Object> getAttachmentHistoryRowData(final ComponentParameter cp,
			final Object dataObject) {
		final Attachment attach = (Attachment) dataObject;
		final KVMap row = new KVMap();
		final StringBuilder topic = new StringBuilder();
		topic.append("<div class='l1'>").append(attach.getTopic()).append("</div>");
		topic.append("<div class='l2 clearfix'>");
		topic.append(" <div class='left'>");
		topic.append($m("AbstractAttachmentExHandler.0")).append(" - ")
				.append(FileUtils.toFileSize(attach.getAttachsize())).append("<br>");
		topic.append($m("AbstractAttachmentExHandler.1")).append(" - ")
				.append(StringUtils.blank(attach.getFileExt()).toUpperCase());
		topic.append(" </div>");
		topic.append(" <div class='right'>");
		topic.append(Convert.toDateTimeString(attach.getCreateDate()));
		topic.append(" </div>");
		topic.append("</div>");
		row.add("topic", topic.toString());

		final StringBuilder ope = new StringBuilder();
		ope.append(new ButtonElement($m("AbstractAttachmentExHandler.2")).setOnclick("$Actions['"
				+ cp.getComponentName() + "_history_selected']('attachId=" + attach.getId() + "');"));
		row.add(TablePagerColumn.OPE, ope.toString());
		return row;
	}

	@Override
	public void doAttachmentHistorySelected(final ComponentParameter cp) throws IOException {
		final IAttachmentService<T> aService = getAttachmentService();
		final Attachment attach = aService.getBean(cp.getParameter("attachId"));

		@SuppressWarnings("unchecked")
		final AttachmentFile af = aService.createAttachmentFile((T) attach).setId(null)
				.setCreateDate(null).setType(getAttachtype(cp)).setDownloads(0);
		aService.insert(getOwnerId(cp), cp.getLoginId(), Arrays.asList(af));
	}

	@Override
	public AbstractElement<?> getDownloadLinkElement(final ComponentParameter cp,
			final AttachmentFile attachmentFile, final String id) throws IOException {
		return null;
	}

	protected ImageElement createImageViewer(final PageParameter pp,
			final AttachmentFile attachmentFile, final String id) {
		try {
			if (ImageUtils.isImage(attachmentFile.getExt())) {
				return new ImageElement().addAttribute("viewer_id", id)
						.setSrc(new ImageCache().getPath(pp, attachmentFile));
			}
		} catch (final IOException e) {
			getLog().warn(e);
		}
		return null;
	}

	public static class AttachmentTooltipPage extends AbstractAttachmentTooltipPage {

		@Override
		protected AttachmentFile getAttachment(final PageParameter pp) {
			final ComponentParameter cp = AttachmentUtils.get(pp);
			final IAttachmentHandler aHandler = (IAttachmentHandler) cp.getComponentHandler();
			try {
				return aHandler.getAttachmentById(cp, pp.getParameter("id"));
			} catch (final Exception e) {
				return null;
			}
		}
	}
}
