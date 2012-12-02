package edu.utdallas.fdaf.model;

import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;

import jpl.Atom;
import jpl.Query;
import jpl.Term;

/**
 * Class for transforming program facts into architectural models.
 * @author Jeffrey Koch
 *
 */
public class AbstractionEngine {

	/*
	 * Here's what I'm thinking:  a static var that tells us if we've loaded
	 * the prolog file yet, plus a static method that (if necessary) loads the
	 * prolog file, then runs the engine and returns the model file.
	 */

	private static boolean needToLoadEngine = true;

	/**
	 * Generates an architectural model for an AspectJ/Java system.
	 * @param factsFile XMI file with program facts for the AspectJ/Java system to be analyzed.
	 * @return an XMI File with the architectural model based on the program facts in factsFile.
	 * @throws ModelPrologLoadException if unable to load the Prolog abstraction-engine 
	 * rules.
	 * @throws IOException if unable to create the architectural model file.
	 * @throws CreateModelException if unable to transform program facts into an 
	 * architectural model.
	 */
	public static void generateModel(IFile factsFile, IFile archFile) throws ModelPrologLoadException, IOException, CreateModelException  {
		if (needToLoadEngine) {
			Query query =
			//	new Query("consult('C:/Users/BALAJIS/workspace/edu.utdallas.fdaf.aspectj.reverse/arch.pl')");
				new Query("consult('C:/Users/BALAJIS/Desktop/SEP/Modified code/Yiahos arch/arch.pl')");
		
//'/Users/Jeff/workspace/edu.utdallas.fdaf.aspectj.reverse/arch.pl'
			if ( !query.hasSolution() ){
				throw new ModelPrologLoadException();
			}
			query.close();
			needToLoadEngine = false;
		}

		Atom factsFileAtom = new Atom(factsFile.getLocation().toString());
		Atom modelFileAtom = new Atom(archFile.getLocation().toString());

		Query loadQuery = 
			new Query("load_sgml_term",
					new Term[] {factsFileAtom, modelFileAtom});



		Hashtable[] solutions = loadQuery.allSolutions();

		if ( solutions.length < 1 ){
			throw new CreateModelException();
		}

		loadQuery.close();
		return;
	}
}

