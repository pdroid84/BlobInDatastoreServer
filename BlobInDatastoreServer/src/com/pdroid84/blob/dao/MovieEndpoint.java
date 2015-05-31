package com.pdroid84.blob.dao;

import com.pdroid84.blob.master.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "movieendpoint", namespace = @ApiNamespace(ownerDomain = "pdroid84.com", ownerName = "pdroid84.com", packagePath = "blob.dao"))
public class MovieEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listMovie")
	public CollectionResponse<Movie> listMovie(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Movie> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Movie.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Movie>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Movie obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Movie> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getMovie")
	public Movie getMovie(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Movie movie = null;
		try {
			movie = mgr.getObjectById(Movie.class, id);
		} finally {
			mgr.close();
		}
		return movie;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param movie the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertMovie")
	public Movie insertMovie(Movie movie) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsMovie(movie)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(movie);
		} finally {
			mgr.close();
		}
		return movie;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param movie the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateMovie")
	public Movie updateMovie(Movie movie) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsMovie(movie)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(movie);
		} finally {
			mgr.close();
		}
		return movie;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeMovie")
	public void removeMovie(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Movie movie = mgr.getObjectById(Movie.class, id);
			mgr.deletePersistent(movie);
		} finally {
			mgr.close();
		}
	}

	private boolean containsMovie(Movie movie) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Movie.class, movie.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
