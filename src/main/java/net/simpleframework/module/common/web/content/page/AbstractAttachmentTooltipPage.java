package net.simpleframework.module.common.web.content.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.Convert;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.ApplicationContextFactory;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttachmentTooltipPage extends AbstractTemplatePage {

	protected abstract AttachmentFile getAttachment(PageParameter pp);

	@Override
	public IForward forward(final PageParameter pp) throws Exception {
		final AttachmentFile attachment = _getAttachment(pp);
		if (attachment == null) {
			return new TextForward($m("AbstractAttachmentTooltipPage.0"));
		}
		return super.forward(pp);
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ANONYMOUS;
	}

	protected AttachmentFile _getAttachment(final PageParameter pp) {
		return pp.getRequestCache("@attachment", new CacheV<AttachmentFile>() {
			@Override
			public AttachmentFile get() {
				return getAttachment(pp);
			}
		});
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		final AttachmentFile attachment = _getAttachment(pp);
		final KVMap kv = ((KVMap) super.createVariables(pp)).add("date", getDate(pp, attachment))
				.add("downloads", getDownloads(pp, attachment)).add("md5", attachment.getMd5());
		try {
			kv.add("topic", getTopic(pp, attachment)).add("size", getSize(pp, attachment));
		} catch (final IOException e) {
			getLog().warn(e);
		}
		final Object desc = getDescription(pp, attachment);
		if (StringUtils.hasObject(desc)) {
			kv.add("desc", desc);
		}
		final String ext = attachment.getExt();
		LinkButton preview;
		if (ext.equalsIgnoreCase("pdf") && (preview = getPreviewButton(pp)) != null) {
			kv.add("preview", preview);
		}
		return kv;
	}

	protected Object getSize(final PageParameter pp, final AttachmentFile attachment)
			throws IOException {
		return FileUtils.toFileSize(attachment.getSize());
	}

	protected Object getDate(final PageParameter pp, final AttachmentFile attachment) {
		return Convert.toDateString(attachment.getCreateDate());
	}

	protected Object getTopic(final PageParameter pp, final AttachmentFile attachment) {
		return attachment.toFilename();
	}

	protected Object getDescription(final PageParameter pp, final AttachmentFile attachment) {
		return HtmlUtils.convertHtmlLines(attachment.getDescription());
	}

	protected Object getDownloads(final PageParameter pp, final AttachmentFile attachment) {
		return attachment.getDownloads();
	}

	protected LinkButton getPreviewButton(final PageParameter pp) {
		final Object context = ApplicationContextFactory.ctx();
		IModuleRef ref;
		if (!(context instanceof IApplicationContext)
				|| (ref = ((IApplicationContext) context).getPDFRef()) == null) {
			return null;
		}

		final AttachmentFile attachment = _getAttachment(pp);
		if (attachment == null) {
			return null;
		}
		try {
			final Method method = ref.getClass().getMethod("getPreviewUrl", PageParameter.class,
					AttachmentFile.class);
			return createPreviewButton(pp).setHref(
					(String) ClassUtils.invoke(method, ref, pp, attachment));
		} catch (final Exception e) {
			getLog().error(e);
		}
		return null;
	}

	protected LinkButton createPreviewButton(final PageParameter pp) {
		return LinkButton.corner($m("AbstractAttachmentTooltipPage.7")).setTarget("_blank");
	}
}