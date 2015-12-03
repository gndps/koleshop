package com.koleshop.koleshopbackend.servlets;

import javax.servlet.http.HttpServlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String dirName = "";
	private String shopId = "";

	@Override
	public void init() throws ServletException {
		super.init();
		ServletContext ctx = getServletContext();
		dirName = ctx.getRealPath(ctx.getInitParameter("file_save_directory"));
		dirName = ctx.getInitParameter("hardcode_file_save_directory");
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		FileItem uploadItem = getFileItem(request);
		if (uploadItem == null) {
			response.setContentType("text/plain");
			response.getWriter().write("false");
			response.flushBuffer();
			return;
		}
		String fileName = uploadItem.getName();
		String file = dirName + "/" + fileName;
		File saveTo = new File(file);
		if(saveTo==null || !saveTo.exists()){
			
			if(!saveTo.getParentFile().exists()){
				saveTo.getParentFile().mkdirs();
			}
			saveTo.createNewFile();
		}
		try {
			uploadItem.write(saveTo);
			saveTo.setReadable(true, false);
			saveTo.setWritable(true, false);
			response.setContentType("text/plain");
			response.getWriter().write("{" + fileName + "}");
			response.flushBuffer();
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setContentType("text/plain");
			response.getWriter().write("false");
			response.flushBuffer();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		pw.println("testttt");
	}

	@SuppressWarnings("rawtypes")
	private FileItem getFileItem(HttpServletRequest request) {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		FileItem returnItem = null;
		try {
			upload.setProgressListener(new ProgressListener() {
				
				@Override
				public void update(long arg0, long arg1, int arg2) {
					float percentage = arg0/arg1*100;
					System.out.print(percentage+"% uploaded");
					int n=1;
					if(percentage>5*n){
						System.out.print("-");
						n++;
					}
					System.out.print(">");
				}
			});
			List items = upload.parseRequest(request);
			Iterator it = items.iterator();
			
			while (it.hasNext()) {
				FileItem item = (FileItem) it.next();
				if ("image".equals(item.getFieldName())) {
					returnItem = item;
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
			return null;
		}
		return returnItem;
	}
}
