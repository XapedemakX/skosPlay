package fr.sparna.rdf.sesame.toolkit.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sorts statements before sending them to a delegate RDFHandler. Groups the statements by
 * subject, then predicate, with RDF.TYPE first.
 * You can use a RDFXMLPrettyWriter as a delegate to produce a clean RDF/XML output.
 * Usage :
 * <code>
 * RDFHandler writer = new SortedWriter(new RDFXMLPrettyWriter(new FileOutputStream(myFile)));
 * repository.export(writer);
 * </code>
 * 
 * @author Thomas Francart
 *
 */
public class SortingRDFHandler implements RDFHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	private RDFHandler handler;
	
	// internal list of statements that will be sorted
	// before being sent to the delegate
	private List<Statement> statements = new ArrayList<Statement>();
	
	/**
	 * Constructs a SortingRDFHandler with a delegate handler
	 * 
	 * @param handler	The handler to delegate the calls to
	 */
	public SortingRDFHandler(RDFHandler handler) {
		super();
		this.handler = handler;
	}

	/**
	 * Calls <code>startRDF()</code> on the delegate handler
	 */
	@Override
	public void startRDF() throws RDFHandlerException {
		handler.startRDF();
	}

	/**
	 * Sorts all the statements gathered in <code>handleStatement</code>
	 * Before calling <code>handleStatement</code> on the delegate with sorted statements, and finally
	 * calls <code>endRDF()</code> on the delegate.
	 */
	@Override
	public void endRDF() throws RDFHandlerException {
		log.debug("Sorting statement list...");
		Collections.sort(statements, new Comparator<Statement>() {
			
			@Override
			public int compare(Statement s1, Statement s2) {
				int subjectComparison = s1.getSubject().stringValue().compareTo(s2.getSubject().stringValue());
				if(subjectComparison == 0) {
					// on fait remonter le rdf:type en premier pour etre sur qu'il soit mis comme nom de la balise
					if(s1.getPredicate().equals(RDF.TYPE)) return -1;
					else if (s2.getPredicate().equals(RDF.TYPE)) return 1;
					// sinon on tri par predicat pour regrouper ensemble les predicats du meme type
					else return s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());
				} else {
					return subjectComparison;
				}
			}
			
		});
		log.debug("Done sorting.");
		
		// send statements to delegate writer
		for (Statement aStatement : statements) {
			handler.handleStatement(aStatement);
		}
		
		// call the endRDF method on the delegate RDFHandler
		handler.endRDF();		
	}

	/**
	 * Calls <code>handleComment</code> on the delegate handler
	 */
	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		handler.handleComment(arg0);
	}

	/**
	 * Calls <code>handleNamespace</code> on the delegate handler
	 */
	@Override
	public void handleNamespace(String arg0, String arg1)
	throws RDFHandlerException {
		handler.handleNamespace(arg0, arg1);
	}

	/**
	 * Stores the statement internally for later sorting
	 */
	@Override
	public void handleStatement(Statement s) throws RDFHandlerException {
		statements.add(s);
	}
	
}
