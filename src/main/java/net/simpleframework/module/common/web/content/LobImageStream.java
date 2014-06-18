package net.simpleframework.module.common.web.content;

import java.io.IOException;
import java.io.InputStream;

import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.AttachmentLob;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.mvc.common.ImageCache.ImageStream;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class LobImageStream extends ImageStream {

	private final IAttachmentService<Attachment> aService;

	private final Attachment attach;

	public LobImageStream(final IAttachmentService<Attachment> aService, final Attachment attach) {
		super(attach.getMd5());
		this.aService = aService;
		this.attach = attach;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		final AttachmentLob lob = aService.getLob(attach);
		return lob != null ? lob.getAttachment() : null;
	}
}
