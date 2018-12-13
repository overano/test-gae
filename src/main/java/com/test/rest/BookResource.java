package com.test.rest;


import com.test.dao.BookBeanDAO;
import com.test.data.BookBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/book")
@Produces("application/json;charset=utf-8")
@Api(value = "book", description = "Book service")
public class BookResource {

    private BookBeanDAO bookBeanDAO;

    public BookResource() {
        this.bookBeanDAO = new BookBeanDAO();
    }

    @GET
    @ApiOperation("list book objects")
    public Response list() {
        return Response.ok(this.bookBeanDAO.list()).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation("get book object")
    public Response get(@PathParam("id") Long id) {
        BookBean bean = this.bookBeanDAO.get(id);
        if (bean == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(bean).build();
    }

    @POST
    @Consumes("application/json;charset=utf-8")
    @ApiOperation("save book object")
    public Response save(BookBean bean) {
        this.bookBeanDAO.save(bean);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation("delete book object")
    public Response delete(@PathParam("id") Long id) {
        BookBean bean = this.bookBeanDAO.get(id);
        if (bean == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.bookBeanDAO.delete(bean);
        return Response.ok().build();
    }

    @GET
    @Path("/search/{query}")
    @ApiOperation("get books with that criteria")
    public Response search(@PathParam("query") String query){
        return Response.ok(this.bookBeanDAO.search(query)).build();
    }
}
