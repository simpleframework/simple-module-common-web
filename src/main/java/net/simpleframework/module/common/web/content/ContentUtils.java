package net.simpleframework.module.common.web.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.select.Elements;
import net.simpleframework.module.common.bean.IViewsBeanAware;
import net.simpleframework.module.common.content.AbstractContentBean;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.AttachmentLob;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.ImageCache;
import net.simpleframework.mvc.common.ImageCache.ImageStream;
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

	public static void doContent(final PageParameter pp,
			final IAttachmentService<? extends Attachment> attachService, final Document doc) {
		final Elements eles = doc.select("img");
		if (eles != null) {
			for (int i = 0; i < eles.size(); i++) {
				final Element img = eles.get(i);
				final String path = getImagePath(pp, attachService, img);
				if (path != null) {
					img.addClass("viewer_img").attr("src", path);
				}
			}
		}
	}

	public static <T extends Attachment> String getContent(final PageParameter pp,
			final IAttachmentService<T> attachService, final String content) {
		final Document doc = HtmlUtils.createHtmlDocument(content);
		doContent(pp, attachService, doc);
		return doc.html();
	}

	public static <T extends Attachment> String getContent(final PageParameter pp,
			final IAttachmentService<T> attachService, final AbstractContentBean content) {
		final Document doc = content.doc();
		doContent(pp, attachService, doc);
		return doc.html();
	}

	public static String getImagePath(final PageParameter pp,
			final IAttachmentService<? extends Attachment> attachService, final Element img) {
		return getImagePath(pp, attachService, img, 0, 0);
	}

	public static <T extends Attachment> String getImagePath(final PageParameter pp,
			final IAttachmentService<T> attachService, final Element img, final int width,
			final int height) {
		if (img == null) {
			return null;
		}
		final ImageCache iCache = new ImageCache().setWidth(width).setHeight(height);

		String path = null;
		final String attachId = img.attr("viewer_id");
		if (StringUtils.hasText(attachId)) {
			final T attach = attachService.getBean(attachId);
			if (attach != null) {
				path = iCache.setFiletype(attach.getFileExt()).getPath(pp,
						createImageStream(attachService, attach));
			}
		} else {
			final String src = img.attr("src");
			if (HttpUtils.isAbsoluteUrl(src)) {
				if (width > 0 || height > 0) {
					return iCache.getPath(pp, img.attr("src"));
				} else {
					return src;
				}
			} else {
				String filename = FileUtils.getFilename(src);
				final int p = filename.lastIndexOf(".");
				String ext = null;
				if (p > 0) {
					ext = filename.substring(p + 1);
					filename = filename.substring(0, p);
				}
				if (src.startsWith("/")) {
					path = iCache.setFiletype(ext).getPath(pp,
							createImageStream(attachService, filename));
				}
			}
		}
		return path;
	}

	public static <T extends Attachment> ImageStream createImageStream(
			final IAttachmentService<T> aService, final String md) {
		return new ImageStream(md) {
			@Override
			protected InputStream getInputStream() throws IOException {
				final AttachmentLob lob = aService.getLob(md);
				return lob != null ? lob.getAttachment() : null;
			}
		};
	}

	public static <T extends Attachment> ImageStream createImageStream(
			final IAttachmentService<T> aService, final T attach) {
		return new ImageStream(attach.getMd5()) {
			@Override
			protected InputStream getInputStream() throws IOException {
				final AttachmentLob lob = aService.getLob(attach);
				return lob != null ? lob.getAttachment() : null;
			}
		};
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
