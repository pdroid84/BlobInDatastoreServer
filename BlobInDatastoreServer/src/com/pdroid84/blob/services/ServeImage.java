package com.pdroid84.blob.services;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pdroid84.blob.dao.Movie;
import com.pdroid84.blob.master.PMF;

/**
 * GET requests return the promotional image associated with the movie with the
 * title specified by the title query string parameter.
 */
public class ServeImage extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String title = req.getParameter("title");
        System.out.println("The value req.getParameter(title):" +req.getParameter("title").toString());
        Movie movie = getMovie(title);

        if (movie != null && movie.getImageType() != null &&
                movie.getImage() != null) {
            // Set the appropriate Content-Type header and write the raw bytes
            // to the response's output stream
            resp.setContentType(movie.getImageType());
            resp.getOutputStream().write(movie.getImage());
        } else {
            // If no image is found with the given title, redirect the user to
            // a static image
            resp.sendRedirect("/static/noimage.jpg");
        }
    }
    
    /**
     * Queries the datastore for the Movie object with the passed-in title. If
     * found, returns the Movie object; otherwise, returns null.
     *
     * @param title movie title to look up
     */
    private Movie getMovie(String title) {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        // Search for any Movie object with the passed-in title; limit the number
        // of results returned to 1 since there should be at most one movie with
        // a given title
        Query query = pm.newQuery(Movie.class, "title == titleParam");
        query.declareParameters("String titleParam");
        query.setRange(0, 1);

        try {
            List<Movie> results = (List<Movie>) query.execute(title);
            if (results.iterator().hasNext()) {
            	System.out.println("Image is found in the datastore");
                // If the results list is non-empty, return the first (and only)
                // result
                return results.get(0);
            } else {
            	System.out.println("Image is NOT found in the datastore");
            }
        } finally {
            query.closeAll();
            pm.close();
        }

        return null;
    }
}

