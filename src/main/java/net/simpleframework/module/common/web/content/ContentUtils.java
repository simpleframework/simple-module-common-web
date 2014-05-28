package net.simpleframework.module.common.web.content;

import java.io.IOException;
import java.util.ArrayList;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.select.Elements;
import net.simpleframework.module.common.bean.IViewsBeanAware;
import net.simpleframework.module.common.content.AbstractContentBean;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.AttachmentLob;
import net.simpleframework.module.common.content.ContentException;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.ImageCache;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.ckeditor.Toolbar;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ContentUtils {

	// updateViews

	@SuppressWarnings("unchecked")
	public static <T extends IViewsBeanAware> void updateViews(final PageParameter pp, final T bean,
			final IDbBeanService<T> service) {
		if (bean == null) {
			return;
		}
		final String key = "views_"
				+ (bean instanceof IIdBeanAware ? ((IIdBeanAware) bean).getId() : ObjectUtils
						.hashStr(bean));
		if (pp.getSessionAttr(key) == null) {
			synchronized (bean) { // 写数据时，阻塞页面读
				bean.setViews(bean.getViews() + 1);
				service.update(new String[] { "views" }, bean);
				pp.setSessionAttr(key, Boolean.TRUE);
			}
		}
	}

	public static void addViewsCookie(final PageParameter pp, final String key, final Object val) {
		addViewsCookie(pp, key, val, 10);
	}

	public static void addViewsCookie(final PageParameter pp, final String key, final Object val,
			final int maxQueue) {
		final ArrayList<String> al = new ArrayList<String>();
		al.addAll(ArrayUtils.asList(StringUtils.split(pp.getCookie(key), "|")));
		final String sId = Convert.toString(val);
		al.remove(sId);
		al.add(0, sId);
		final int size = al.size();
		if (size > maxQueue) {
			al.remove(size - 1);
		}
		pp.addCookie(key, StringUtils.join(al, "|"), (int) (DateUtils.DAY_PERIOD * 30));
	}

	public static String getContent(final PageParameter pp,
			final IAttachmentService<Attachment> attachService, final AbstractContentBean content) {
		final Document doc = content.doc();
		final Elements eles = doc.select("img[viewer_id]");
		if (eles != null) {
			for (int i = 0; i < eles.size(); i++) {
				final Element img = eles.get(i);
				final String attachId = img.attr("viewer_id");
				try {
					final Attachment attach = attachService.getBean(attachId);
					final AttachmentLob lob;
					if (attach != null && (lob = attachService.getLob(attach)) != null) {
						img.addClass("viewer_img").attr("src",
								new ImageCache(lob, attach.getFileExt(), 0, 0).getPath(pp));
						img.removeAttr("viewer_id");
					}
				} catch (final IOException e) {
					throw ContentException.of(e);
				}
			}
		}
		return doc.html();
	}

	public static String getImagePath(final ComponentParameter cp,
			final IAttachmentService<Attachment> attachService, final Element img) throws IOException {
		return getImagePath(cp, attachService, img, 0, 0);
	}

	public static String getImagePath(final ComponentParameter cp,
			final IAttachmentService<Attachment> attachService, final Element img, final int width,
			final int height) throws IOException {
		ImageCache iCache = null;
		if (img != null) {
			final String viewerId = img.attr("viewer_id");
			Attachment attach;
			AttachmentLob lob;
			if (StringUtils.hasText(viewerId)
					&& (lob = attachService.getLob(attach = attachService.getBean(viewerId))) != null) {
				iCache = new ImageCache(lob, attach.getFileExt(), width, height);
			} else {
				iCache = new ImageCache(img.attr("src"), width, height);
			}
		}
		if (iCache == null) {
			iCache = new ImageCache();
		}
		return iCache.getPath(cp);
	}

	public static Toolbar HTML_TOOLBAR_BASE = Toolbar.of(new String[] { "Source" }, new String[] {
			"Bold", "Italic", "Underline", "Strike" }, new String[] { "PasteText", "PasteFromWord" },
			new String[] { "Find", "Replace", "-", "RemoveFormat" }, new String[] { "NumberedList",
					"BulletedList", "-", "Outdent", "Indent", "Blockquote" }, new String[] {
					"JustifyLeft", "JustifyCenter", "JustifyRight", "JustifyBlock" }, new String[] {
					"Link", "Unlink", "Anchor" }, new String[] {}, new String[] { "Styles", "Format",
					"Font", "FontSize" }, new String[] { "TextColor", "BGColor" }, new String[] {
					"Image", "Table", "HorizontalRule", "Smiley", "SpecialChar" },
			new String[] { "Attach" }, new String[] { "Maximize" });
}
