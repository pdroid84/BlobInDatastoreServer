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
    	System.out.println("UploadMovieServlet POST method is called");
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
    	
        String title = request.getParameter("title");
        System.out.println("Image Title: " + title);
         
        InputStream inputStream = null; // input stream of the upload file
         
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
        try {
        	// Create a factory for disk-based file items
        	DiskFileItemFactory factory = new DiskFileItemFactory();

        	// Configure a repository (to ensure a secure temp location is used)
        	ServletContext servletContext = this.getServletConfig().getServletContext();
        	File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        	factory.setRepository(repository);

        	// Create a new file upload handler
        	ServletFileUpload upload = new ServletFileUpload(factory);
            
         // Parse the request
            List<FileItem> items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();
			// Following line givning null pointer exception
			//		System.out.println("Total number of items = " + (upload.parseRequest(request).size()));
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
					
					// You now have the filename (item.getName() and the
			          // contents (which you can read from stream). Here we just
			          // print them back out to the servlet output stream, but you
			          // will probably want to do something more interesting (for
			          // example, wrap them in a Blob and commit them to the
			          // datastore).
					
					//  inputStream.mark(1024*1024);
			        //  System.out.println("Total length by available function= " + inputStream.available());
			          			          
			   /*       int length = 0 ;
			          InputStream iStream = item.openStream();
			          while(iStream.read() != -1) {
			        	  length = length + 1;
			          } 
			          System.out.println("Total length by using logic = " + length); */
			          //inputStream.reset();
			          int len;
			          while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
			            response.getOutputStream().write(buffer, 0, len);
			            mReadBuffer.write(buffer, 0, len);
			            System.out.println("In the while inputstream loop, len = " + len);
			          }
			          System.out.println("After the while inputstream loop, len = " + len);
				}
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ServletException(e);
		} 
        finally {
        	inputStream.close();
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
