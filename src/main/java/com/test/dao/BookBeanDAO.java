package com.test.dao;

import com.google.appengine.api.search.*;
import com.googlecode.objectify.ObjectifyService;
import com.test.data.BookBean;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BookBeanDAO {

    private static final Logger LOGGER = Logger.getLogger(BookBeanDAO.class.getName());
    private static final String SEARCH_INDEX = "searchIndex";

    /**
     * @return list of book beans
     */
    public List<BookBean> list() {
        LOGGER.info("Retrieving list of beans");
        return ObjectifyService.ofy().load().type(BookBean.class).order("author").list();
    }

    /**
     * @param id Book identification
     * @return book bean with given id
     */
    public BookBean get(Long id) {
        LOGGER.info("Retrieving bean " + id);
        return ObjectifyService.ofy().load().type(BookBean.class).id(id).now();
    }

    /**
     * Saves given bean
     * @param bean
     */
    public void save(BookBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("null book object");
        }
        LOGGER.info("Saving bean " + bean.getId());
        ObjectifyService.ofy().save().entity(bean).now();

        // save data to full text search
        try{
            Document doc = Document.newBuilder()
                    .setId(bean.getId().toString())
                    .addField(Field.newBuilder().setName("name").setText(getPieces(bean.getName())))
                    .addField(Field.newBuilder().setName("author").setText(getPieces(bean.getAuthor())))
                    .addField(Field.newBuilder().setName("year").setText(bean.getYear()))
                    .addField(Field.newBuilder().setName("genre").setText(bean.getGenre()))
                    .build();
            Index index = getIndex();
            index.put(doc);
        } catch (PutException e) {
            throw e;
        }

    }

    /**
     * Deletes given bean
     * @param bean
     */
    public void delete(BookBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("null book object");
        }
        LOGGER.info("Deleting bean " + bean.getId());
        ObjectifyService.ofy().delete().entity(bean);

        //delete data from full text search
        try{
            getIndex().delete(bean.getId().toString());
        }catch (DeleteException e){
            throw e;
        }

    }

    /**
     * @return list of book beans
     */
    public List<BookBean> search(String queryString) {
        LOGGER.info("Retrieving list of beans by query");

        //Build sortOptions
        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("author")
                        .setDirection(SortExpression.SortDirection.ASCENDING)
                        .setDefaultValue(""))
                .setLimit(1000)
                .build();

        //Build queryOptions
        QueryOptions options = QueryOptions.newBuilder()
                .setSortOptions(sortOptions)
                .build();

        Query query = Query.newBuilder().setOptions(options).build(queryString);

        List<BookBean> listBook = new ArrayList<BookBean>();
        Index index = getIndex();
        Results<ScoredDocument> results = index.search(query);

        for (ScoredDocument document : results) {
            LOGGER.info("document: " + document);
            BookBean book = get(Long.valueOf(document.getId()));
            LOGGER.info("book: " + book);
            listBook.add(book);
        }

        return listBook;
    }

    private String getPieces(String str){
        String result;

        List<String> pieces = new ArrayList<String>();
        for (String word : str.split("\\s+")){
            int cursor = 1;
            while(true){
                // this method produces pieces of 'TEXT' as 'T,TE,TEX,TEXT'
                pieces.add(word.substring(0, cursor));

//                optionally, you can do the following instead to procude
//                'TEXT' as 'T,E,X,T,TE,EX,XT,TEX,EXT,TEXT'
//                for (int i = 0, n = word.length() - cursor +1; i < n; i++){
//                    pieces.add(word.substring(i, i+cursor));
//                }

                if (cursor == word.length()){
                    break;
                }
                cursor++;
            }
        }
        result = pieces.toString().substring(1, pieces.toString().length()-1);

        return result;
    }

    private Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCH_INDEX).build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }
}
