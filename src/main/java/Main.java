import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import gov.nih.nlm.uts.webservice.AtomDTO;
import gov.nih.nlm.uts.webservice.ConceptDTO;
import gov.nih.nlm.uts.webservice.Psf;
import gov.nih.nlm.uts.webservice.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.UtsWsContentController;
import gov.nih.nlm.uts.webservice.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsMetadataControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.UtsWsSecurityControllerImplService;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import gov.nih.nlm.uts.webservice.UiLabel;
import gov.nih.nlm.uts.webservice.UtsWsFinderController;
import gov.nih.nlm.uts.webservice.UtsWsFinderControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsMetadataController;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/***************************************************************************
 *
 * A FAIRE avant de lancer le programme  : 
 * 
1- Créer 3 dossiers en local sur ordi et changer les chemins dans Searcher, FrenchIndexer et MyXMLHandler  
(commentés en "TO DO") qui pointent vers ces dossiers : 
  - corpus (corpus eng)
  - corpusfr (corpus français) 
  - articles fr (contenant les fichiers txt)
  - pubmed_result.xml 
  
2- Modifier le buildpath pour  mettre les 4 jar (3 jar de lucene, 1 jar pour sql)
 
3- Modifier le url de la BDD pour la connexion en local

4- Préparer l'environnement pour que l'API umls (projet Maven) fonctionne 
(installer pom.xml, vérifier que le fichier settings.xml conforme à ce qui est décrit
par la documentation umls api soap)
https://documentation.uts.nlm.nih.gov/soap/installation/maven-installation.html
*
*
****************************************************************************/

public class Main {

	private static String ticketGrantingTicket = null;
	private static UtsWsSecurityController utsSecurityService;
	private static UtsWsMetadataController utsMetadataService;
	private static UtsWsContentController utsContentService;
	private static UtsWsFinderController utsFinderService;
	private static String singleUseTicket;
	private static String serviceName = "http://umlsks.nlm.nih.gov";
	private static String username = "deannawung";   //for UMLS API
	private static String password = "m2sitistermino#";
	private static List<AtomDTO> atoms = new ArrayList<AtomDTO>(); //this arraylist is built by the search 
	private static Psf myPsf = new Psf();	

	private static String url = "jdbc:mysql://localhost:3306/wung";  //TODO: change url
	private static String user = "root";
	private static String passwordBDD = ""; 
	private static String codeCIMSQL = null;
	private static String codeCIMfinal = null;
	

	//	String sql1 = "SELECT  DISTINCT tab_hospitalisation.NumPatient, ths_cim10.CodeCIM10 FROM (`tab_hospitalisation`INNER JOIN tab_diagnostic on tab_hospitalisation.NumHospitalisation = tab_diagnostic.NumHospitalisation ) INNER JOIN ths_cim10 on ths_cim10.CodeCIM10 = tab_diagnostic.CodeCIM10 ";

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, UtsFault_Exception {	


		MyXMLHandler.parserXml();
		System.out.println(Article.getNbrInstances()+" articles ajoutés (corpus anglais)");

		//creation des documents lucene
		Indexer.createDocLucene();

		//indexation des documents
		Indexer.indexDoc();
		
		FrenchIndexer.indexFrenchDoc();


		//Connexion UMLS et base de donnees__________________________________________________________________________
		try {
			utsFinderService = (new UtsWsFinderControllerImplService()).getUtsWsFinderControllerImplPort();
			utsSecurityService = (new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();
			utsMetadataService = (new UtsWsMetadataControllerImplService()).getUtsWsMetadataControllerImplPort();
			utsContentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
			ticketGrantingTicket = utsSecurityService.getProxyGrantTicket(username, password); //Proxy Grant ticket
			Class.forName("com.mysql.jdbc.Driver").newInstance ( ) ; 
		} catch (Exception e) {
			System.out.println("Error!!! problem with umls ticket or driver" + e.getMessage());
			e.printStackTrace(); 
		}
		//__________________________________________________________________________________________________________


		String scAnotherPat = "O"; 
		while (scAnotherPat.equalsIgnoreCase("o"))  {

			Scanner sc = new Scanner(System.in);
			//QUESTION 1: lancer une requete sql avec nextline in scanner 
			System.out.println("Veuillez entrer le numéro du patient dont vous voulez le code CIM10 : ");
			while (!sc.hasNextInt()) sc.next(); 
			String scCodePat = Integer.toString(sc.nextInt());

			ArrayList<String> codesCIMforPat = SQLgetCodes(scCodePat); //finds all CIM10 codes for that patient

			//QUESTION 2: which code CIM10? 
			System.out.println("\n\nChoisir un code CIM10 (1, 2, 3...) :");

			
			while (!sc.hasNextInt()) sc.next(); 
			int opt=sc.nextInt();
			String chosenCodeCIM = codesCIMforPat.get(opt-1);
			System.out.println ( "Option " + opt + " :  " + chosenCodeCIM + "\nRecherche des synonymes. Merci de patienter...");  // show which code CIM10 # was chosen 

			// print synonyms 
			ArrayList<HashSet<String>> synonyms = searchUMLS(findUMLScode(chosenCodeCIM));
			System.out.println ( "\nSynonymes :  " + "\n\nPreferred Term :" + synonyms.get(0) 
					+ "\n\nENG :" +synonyms.get(1) + "\n\nFR :" +synonyms.get(2) + "\n\n");  


			//searcher
			Searcher.Recherche(synonyms);

			//QUESTION 3: another patient or quit? 
			System.out.println("\nContinuer avec un autre patient ? (O/N) :");  
			sc.nextLine();
			scAnotherPat = sc.nextLine();

		}

		System.out.println("Au revoir !");

	}

	//___________FUNCTIONS____________________________________________________________________________________________________


	// Question 1 do SQL to get CIM10 codes for a certain numPat  
	private static ArrayList<String> SQLgetCodes(String numPat) {
		ArrayList<String> listcodesCIMPat = new ArrayList<String>(); 
		String sqlgetCodes = "SELECT  DISTINCT tab_hospitalisation.NumPatient, ths_cim10.CodeCIM10 FROM "
				+ "(`tab_hospitalisation`INNER JOIN tab_diagnostic on "
				+ "tab_hospitalisation.NumHospitalisation = tab_diagnostic.NumHospitalisation ) "
				+ "INNER JOIN ths_cim10 on ths_cim10.CodeCIM10 = tab_diagnostic.CodeCIM10 "
				+ "where tab_hospitalisation.NumPatient = ?";
		Connection conn;
		try {
			conn = DriverManager.getConnection(url, user, passwordBDD);
			PreparedStatement phrase= conn.prepareStatement(sqlgetCodes);
			phrase.setString(1, numPat);
			ResultSet res = phrase.executeQuery();
			int resultsiteration = 1;

			//get all CIM10 codes from patient, add a dot, and put it in arraylist listcodesCIMPat
			while (res.next()){
				codeCIMSQL = res.getString("CodeCIM10"); 
				StringBuffer sb = new StringBuffer();
				if (codeCIMSQL.length()>3) {  //if code needs a dot
					for(int i = 0; i < codeCIMSQL.length(); i += 6) { //add dot to CIM10 code for umls search compatability
						sb.append(codeCIMSQL.substring(i, i + 3));
						sb.append(".");	
					}
					codeCIMfinal = sb.toString() + codeCIMSQL.substring(3);
				} else { //if code doesn't need a dot, leave code as is
					codeCIMfinal = codeCIMSQL; 
				}
				listcodesCIMPat.add(codeCIMfinal);  // fill list of CIM10 codes for pat 
				System.out.println(resultsiteration + " : " + codeCIMfinal);
				resultsiteration++; //traquer itération pour numéroter les codes récupérés pour le menu
			}

		} catch (SQLException e) {
			System.out.println("requete impossible\n");
			e.printStackTrace();
		}
		return listcodesCIMPat;
	}

	//search UMLS to generate Arraylist of synonyms  
	private static ArrayList<HashSet<String>> searchUMLS(String codecui){

		HashSet<String> result_preferred = new HashSet<String>(); //will contain preferred term (prioritized term) 
		HashSet<String> result_ENG = new HashSet<String>(); //will contain synonyms EN
		HashSet<String> result_FR = new HashSet<String>(); //will contain synonyms FR

		//First we'll get the preferred concept term in umls and put it in result_preferred
		String preferredterm = searchConcept(codecui);
		result_preferred.add(preferredterm);

		//Now we'll get other terms (synonyms)    ENG
		atoms = searchSynonENG(codecui);
		for (AtomDTO atom:atoms) {
			String name = atom.getTermString().getName();			    
			result_ENG.add(name.replace(" NOS","").replaceAll("[\\p{Punct}]","").toLowerCase()); //add to results hashset result_ui after removing punctuation and irrelevant words like NOS / SAI 
		} 

		//Again, we'll get more terms (synonyms)    FR
		atoms = searchSynonFR(codecui);
		for (AtomDTO atom:atoms) {
			String name = atom.getTermString().getName();			    
			result_FR.add(name.replace(" NOS","").replace(" SAI","").replaceAll("[\\p{Punct}]","").toLowerCase()); //add to results hashset result_ui after removing punctuation and irrelevant words like NOS / SAI 
		} 

		//put the 3 hashsets together to return results in
		ArrayList<HashSet<String>> resultslist = new ArrayList<HashSet<String>>();  
		resultslist.add(result_preferred);
		resultslist.add(result_ENG);
		resultslist.add(result_FR);
		return resultslist;

	}

	//for umls connection
	private static  String getProxyTicket(String ticket, String serviceName){	 
		try {
			return utsSecurityService.getProxyTicket(ticket, serviceName);
		} catch (Exception e) {
			return "";
		}
	}

	//for umls connection
	private static void newTicket() {
		singleUseTicket = getProxyTicket(ticketGrantingTicket, serviceName);
	}

	//for umls connection
	private static void newPsf() {
		myPsf = new Psf();
		myPsf.setIncludeObsolete(false);
		myPsf.setIncludeSuppressible(false);	
	}

	//search umls to get preferred term
	private static String searchConcept(String codecui) {
		try {
			newTicket();
			ConceptDTO concept = utsContentService.getConcept(singleUseTicket, "2011AB", codecui);
			String name = concept.getDefaultPreferredName();
			return name;
		} catch (Exception e) {
			return null;
		}
	}


	//search umls to get ENG synonyms
	private static List<AtomDTO> searchSynonENG(String codecui) {
		try {
			newTicket();
			String currentUmlsRelease = utsMetadataService.getCurrentUMLSVersion(singleUseTicket);		
			newPsf();
			myPsf.getIncludedSources().add("MSH");
			myPsf.getIncludedSources().add("ICD10");
			myPsf.getIncludedSources().add("ICD10CM");	
			myPsf.getIncludedSources().add("SNOMEDCT_US");	
			myPsf.getIncludedSources().add("SNMI");		
			myPsf.getIncludedSources().add("MEDLINEPLUS");
			newTicket();
			return utsContentService.getConceptAtoms(singleUseTicket, currentUmlsRelease, codecui, myPsf);
		} catch (Exception e) {
			return null;
		}
	}

	//search UMLS to get FR synonyms
	private static List<AtomDTO> searchSynonFR(String codecui) {
		try {
			newTicket();
			String currentUmlsRelease = utsMetadataService.getCurrentUMLSVersion(singleUseTicket);		
			newPsf();
			myPsf.getIncludedSources().add("MDRFRE");	
			myPsf.getIncludedSources().add("MSHFRE");	
			myPsf.getIncludedSources().add("WHOFRE");	
			newTicket();
			return utsContentService.getConceptAtoms(singleUseTicket, currentUmlsRelease, codecui, myPsf);
		} catch (Exception e) {
			return null;
		}
	}

	//search UMLS code from a CIM10 code    
	private static String findUMLScode(String codeCIM10) {
		String ui ="empty ui";
		try {
			newTicket();
			String currentUmlsRelease = utsMetadataService.getCurrentUMLSVersion(singleUseTicket);
			newPsf();
			myPsf.getIncludedSources().add("ICD10");
			newTicket();
			List<UiLabel> results = new ArrayList<UiLabel>();
			int pageNum = 1;
			do {
				myPsf.setPageNum(pageNum);
				results = utsFinderService.findConcepts(singleUseTicket, currentUmlsRelease, "code", codeCIM10, "words", myPsf);
				for (UiLabel result:results) {
					ui = result.getUi();
				}
				pageNum++;
			} while (results.size() > 0);
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
		return ui;	
	}
}
