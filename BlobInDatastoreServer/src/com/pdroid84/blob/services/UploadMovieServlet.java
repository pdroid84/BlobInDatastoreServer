package com.pdroid84.blob.services;

import com.pdroid84.blob.dao.Movie;
import com.pdroid84.blob.master.PMF;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


/**
 * GET requests fetch the image at the URL specified by the url query string
 * parameter, then persist this image along with the title specified by the
 * title query string parameter as a new Movie object in App Engine's
 * datastore.
 */
@MultipartConfig(maxFileSize = 1048576)    // upload file's size up to 1MB
public class UploadMovieServlet extends HttpServlet {

    
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // gets values of text fields
    	byte[] buffer = new byte[1024];
    	ByteArrayOutputStream mReadBuffer = new ByteArrayOutputStream();
    	FileItem item = null;
    	long maxFileSize = 1024 * 1024;
        int maxMemSize = 1024 * 1024;
        List<FileItem> items = null;
        InputStream inputStream = null; // input stream of the upload file
    	
    	System.out.println("UploadMovieServlet POST method is called");
    	
    	if(ServletFileUpload.isMultipartContent(request)) {
    		System.out.println("Request has multi-part body");
    	} 
    	else {
    		System.out.println("Request has NOT multi-part body");
    	}
    	//Get all the header names and associated values
    	Enumeration<String> headerNames = request.getHeaderNames();
    	while (headerNames.hasMoreElements()) {
    		String headerName = headerNames.nextElement();
    		System.out.println("Header Name = " + headerName);
    		Enumeration<String> headers = request.getHeaders(headerName);
    		while (headers.hasMoreElements()) {
    			String headerValue = headers.nextElement();
    			System.out.println("Header Values = " +headerValue);
    		}
    	}
    	
    	Enumeration<String> parameterNames = request.getParameterNames();
    	while (parameterNames.hasMoreElements()) {
    		String parameterName = parameterNames.nextElement();
    		System.out.println("Parameter Name = " + parameterName);
    		String[] parameters = request.getParameterValues(parameterName);
    		for(int i = 0;i<parameters.length;i++) {
    			System.out.println("Parameter value = " + parameters[i]);
    		}
    	}
    	
        //String title = request.getParameter("title");
        //System.out.println("Image Title: " + title); 
        // obtains the upload file part in this multipart request
        // mulitpart (i.e. getPart is not working in Jetty 6.0 which is in current JAE
    /*   Part filePart = request.getPart("photo");
        if (filePart != null) {
            // prints out some information for debugging
            System.out.println(filePart.getName());
            System.out.println(filePart.getSize());
            System.out.println(filePart.getContentType());
             
            // obtains input stream of the upload file
            inputStream = filePart.getInputStream();
        }  */
        
        response.setContentType("text/plain");
        
        
    	// Create a factory for disk-based file items
    	DiskFileItemFactory factory = new DiskFileItemFactory();

    	// Configure a repository (to ensure a secure temp location is used). This is required if seSizeThreshold is not used
    //	ServletContext servletContext = this.getServletConfig().getServletContext();
   // 	File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
    	        	
    	// Set factory constraints
    	//use this one when repository option is not used
    	factory.setSizeThreshold(maxMemSize);
    	//Do not use repository if setSizeThreshold is used
    //	factory.setRepository(repository);
    	
    	// Create a new file upload handler
    	System.out.println("Going to create a new ServletFielUpload handler");
    	ServletFileUpload upload = new ServletFileUpload(factory);
        
    	// maximum file size to be uploaded.
    	//Console is throwing junks when called from android so commenting the below line
        upload.setSizeMax(maxFileSize);
    	
     // Parse the request
    	System.out.println("Going to parse the request");
        
		try {
			items = upload.parseRequest(request);
			System.out.println("The value of item size = " + items.size());
		} catch (FileUploadException e1) {
			// TODO Auto-generated catch block
			System.out.println("An exception occurred while parsing the request");
			e1.printStackTrace();
		}
		Iterator<FileItem> iter = items.iterator();
		int i = 0;
		while (iter.hasNext()) {
			System.out.println("Iterator is called " + i++);
			item = iter.next();
			
			if (item.isFormField()) {
				System.out.println("Got a form Field:" + item.getFieldName() + ", value = " + item.getString());
			} else {
				System.out.println("Got an uploaded file: " + item.getFieldName() + ", name : " + item.getName());
				System.out.println("Content type =" + item.getContentType());
				System.out.println("Item Size =" + item.getSize());
				
				inputStream = item.getInputStream();
				
		          int len;
		          while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
		            response.getOutputStream().write(buffer, 0, len);
		            mReadBuffer.write(buffer, 0, len);
		            System.out.println("In the while inputstream loop, len = " + len);
		          }
		          System.out.println("After the while inputstream loop, len = " + len);
			}
		}
                
        String ImageName = item.getName().substring(0, item.getName().indexOf("."));
        System.out.println("The name without file extension = " + ImageName);
        
        
        Movie movie = new Movie();
        movie.setTitle(ImageName);
        movie.setImageType(item.getContentType());
       // movie.setImageType(filePart.getContentType());
        movie.setImage(mReadBuffer.toByteArray());
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
         //    Store the image in App Engine's datastore
            pm.makePersistent(movie);
            System.out.println("Image persisted!!");
        } finally {
            pm.close();
        } 
 }
}
