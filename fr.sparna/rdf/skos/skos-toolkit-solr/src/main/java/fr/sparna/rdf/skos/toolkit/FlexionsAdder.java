package fr.sparna.rdf.skos.toolkit;

import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.ling.dictionary.DelaDictionary;
import fr.sparna.rdf.sesame.toolkit.query.Perform;
import fr.sparna.rdf.sesame.toolkit.query.SparqlPerformException;
import fr.sparna.rdf.sesame.toolkit.util.RepositoryTransaction;

/**
 * Adds french flexions in a given predicate (typically skos:hiddenLabel) from existing skos:prefLabels of
 * skos:Concept.
 * 
 * @author Thomas Francart
 *
 */
public class FlexionsAdder {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	protected java.net.URI flexionsProperty = java.net.URI.create(SKOS.HIDDEN_LABEL);
	
	public FlexionsAdder() {
		super();
	}

	public FlexionsAdder(java.net.URI flexionsProperty) {
		super();
		this.flexionsProperty = flexionsProperty;
	}
	
	public void addFlexions(Repository repository, List<java.net.URI> conceptSchemesToExclude) throws RepositoryException, SparqlPerformException {
		DelaDictionary dico = new DelaDictionary(DelaDictionary.class.getClassLoader().getResourceAsStream("dela/dela_fr.lst"), "UTF-8", "fr");
		FlexionsAdderHelper helper = new FlexionsAdderHelper(dico, repository, conceptSchemesToExclude);
		Perform.on(repository).select(helper);
	}	
	
	public void addFlexions(Repository repository) throws RepositoryException, SparqlPerformException {
		addFlexions(repository, null);
	}

	class FlexionsAdderHelper extends GetLabelsHelper {

		protected DelaDictionary dico;
		protected Repository repository;
		protected RepositoryTransaction transaction;
		
		public FlexionsAdderHelper(
				DelaDictionary dico,
				Repository repository,
				List<java.net.URI> conceptSchemesToExclude) 
		throws RepositoryException {
			super(null, true, true, true, null);
			this.dico = dico;
			this.repository = repository;
			
			this.transaction = new RepositoryTransaction(repository.getConnection());
			// exclure certains concepts schemes
			((QueryBuilder)this.builder).setConceptSchemesToExclude(conceptSchemesToExclude);
		}

		@Override
		protected void handleLabel(
				Resource concept,
				org.openrdf.model.URI labelType,
				String label,
				String lang)
		throws TupleQueryResultHandlerException {
			log.trace("Handling flexions of label "+label+"...");
			if(lang.equals("fr")) {
				Set<String> flexions = dico.getFlexion(label, false, false);
				log.trace("Found "+flexions.size()+" flexions for this label");
				if(flexions != null) {
					for (String aFlexion : flexions) {
						try {
							this.transaction.add(repository.getValueFactory().createStatement(
									concept,
									repository.getValueFactory().createURI(flexionsProperty.toString()),
									repository.getValueFactory().createLiteral(aFlexion, lang))
							);
						} catch (RepositoryException e) {
							throw new TupleQueryResultHandlerException(e);
						}
					}
				}
			}
		}

		@Override
		public void endQueryResult() throws TupleQueryResultHandlerException {
			super.endQueryResult();
			try {
				this.transaction.commit();
			} catch (RepositoryException e) {
				throw new TupleQueryResultHandlerException(e);
			}
		}
		
	}

}
