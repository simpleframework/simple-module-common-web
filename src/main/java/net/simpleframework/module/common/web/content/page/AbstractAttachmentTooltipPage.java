package net.simpleframework.module.common.web.content.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttachmentTooltipPage extends AbstractTemplatePage {

	protected abstract AttachmentFile getAttachment(PageParameter pp);

	@Override
	public IForward forward(final PageParameter pp) {
		final AttachmentFile attachment = _getAttachment(pp);
		if (attachment == null) {
			return new TextForward($m("AbstractAttachmentTooltipPage.0"));
		}
		return super.forward(pp);
	}

	protected AttachmentFile _getAttachment(final PageParameter pp) {
		AttachmentFile attachment = (AttachmentFile) pp.getRequestAttr("@attachment");
		if (attachment == null) {
			attachment = getAttachment(pp);
		}
		if (attachment != null) {
			pp.setRequestAttr("@attachment", attachment);
		}
		return attachment;
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		final AttachmentFile attachment = _getAttachment(pp);
		final KVMap kv = ((KVMap) super.createVariables(pp)).add("topic", getTopic(pp, attachment))
				.add("size", getSize(pp, attachment)).add("date", getDate(pp, attachment))
				.add("downloads", getDownloads(pp, attachment)).add("md5", attachment.getMd5());
		final Object desc = getDescription(pp, attachment);
		if (StringUtils.hasObject(desc)) {
			kv.add("desc", desc);
		}
		final String type = attachment.getType();
		LinkButton preview;
		if (type != null && type.equalsIgnoreCase("pdf") && (preview = getPreviewButton(pp)) != null) {
			kv.add("preview", preview);
		}
		return kv;
	}

	protected Object getSize(final PageParameter pp, final AttachmentFile attachment) {
		return FileUtils.toFileSize(attachment.getSize());
	}

	protected Object getDate(final PageParameter pp, final AttachmentFile attachment) {
		return Convert.toDateString(attachment.getCreateDate());
	}

	protected Object getTopic(final PageParameter pp, final AttachmentFile attachment) {
		String topic = attachment.getTopic();
		final String type = attachment.getType();
		if (StringUtils.hasText(type)) {
			topic += "." + type;
		}
		return topic;
	}

	protected Object getDescription(final PageParameter pp, final AttachmentFile attachment) {
		return HtmlUtils.convertHtmlLines(attachment.getDescription());
	}

	protected Object getDownloads(final PageParameter pp, final AttachmentFile attachment) {
		return attachment.getDownloads();
	}

	protected LinkButton getPreviewButton(final PageParameter pp) {
		return null;
	}

	protected LinkButton createPreviewButton(final PageParameter pp) {
		return LinkButton.corner($m("AbstractAttachmentTooltipPage.7")).setTarget("_blank");
	}
}