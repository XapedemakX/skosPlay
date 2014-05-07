package fr.sparna.rdf.sesame.toolkit.query.builder;

import java.util.Collections;
import java.util.List;


/**
 * Encapsulates another query builder to add OFFSET, LIMIT and ORDER BY clauses to the SPARQL query returned.
 * 
 * <p>Exemple usage scenario :
 * <code>
 * Repository repository = ...;
 * SparqlQueryBuilderIfc builder = ...;
 * PagingSparqlQueryBuilder pagingBuilder = new PagingSparqlQueryBuilder(builder, 0, 10);
 * 
 * TupleQueryResultHandler handler = ...;
 * Perform.on(repository).select(new SPARQLHelper(pagingBuilder, handler));
 * </code>
 * 
 * @author Thomas Francart
 */
public class PagingSparqlQueryBuilder implements SparqlQueryBuilderIfc {

	private final static String OFFSET = "OFFSET";
	private final static String LIMIT = "LIMIT";
	private final static String ORDER_BY = "ORDER BY";

	protected SparqlQueryBuilderIfc builder;
	// paging
	protected Integer offset;
	protected Integer limit;
	// ordering
	protected List<OrderBy> orderBy;
	
	/**
	 * Wraps the given builder to add LIMIT, OFFSET, and ORDER BY criteria to it
	 * 
	 * @param builder
	 * @param offset
	 * @param limit
	 * @param orderBy
	 */
	public PagingSparqlQueryBuilder(
			SparqlQueryBuilderIfc builder,
			Integer offset,
			Integer limit,
			List<OrderBy> orderBy
	) {
		super();
		this.builder = builder;
		this.offset = offset;
		this.limit = limit;
		this.orderBy = orderBy;
	}
	
	/**
	 * Convenience constructor with a single OrderBy
	 * 
	 * @param builder
	 * @param offset
	 * @param limit
	 * @param orderBy
	 */
	public PagingSparqlQueryBuilder(
			SparqlQueryBuilderIfc builder,
			Integer offset,
			Integer limit,
			OrderBy orderBy
	) {
		this(builder, offset, limit, Collections.singletonList(orderBy));
	}
	
	/**
	 * Wraps the given builder to add OFFSET and LIMIT criterias to it.
	 * 
	 * @param builder
	 * @param offset
	 * @param limit
	 */
	public PagingSparqlQueryBuilder(SparqlQueryBuilderIfc builder, Integer offset, Integer limit) {
		this(builder, offset, limit, (List<OrderBy>)null);
	}
	
	/**
	 * Wraps the given builder to add ORDER BY criterias to it.
	 * 
	 * @param builder
	 * @param orderBy
	 */
	public PagingSparqlQueryBuilder(SparqlQueryBuilderIfc builder, List<OrderBy> orderBy) {
		this(builder, null, null, orderBy);
	}	

	/**
	 * Convenience constructor with a single OrderBy
	 * 
	 * @param builder
	 * @param orderBy
	 */
	public PagingSparqlQueryBuilder(SparqlQueryBuilderIfc builder, OrderBy orderBy) {
		this(builder, null, null, Collections.singletonList(orderBy));
	}
	
	@Override
	public String getSPARQL() {
		String sparql = builder.getSPARQL();
		
		// sort
		if(orderBy != null && orderBy.size() > 0) {
			sparql += " " + ORDER_BY;
			for (OrderBy order : orderBy) {
				sparql += " " + (!order.isAscending()?("DESC(?" + order.getOrder() + ")"):("?" + order.getOrder()));
			}
		}
		
		// paging
		if(offset != null) {
			sparql += " " + OFFSET + " " + offset.intValue();
		}
		if(limit != null) {
			sparql += " " + LIMIT + " " + limit.intValue();
		}
		
		return sparql;
	}

}