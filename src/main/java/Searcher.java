
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Searcher {

	private static void RequeteEn(int nb, String term) throws ParseException, IOException {
		// Requete avec l'analyzer anglais
		//TODO change path
		String indexLocation="C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\corpus";
		EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40, EnglishAnalyzer.getDefaultStopSet());
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
		IndexSearcher searcher= new IndexSearcher(reader);
		TopScoreDocCollector collector ;
		collector = TopScoreDocCollector.create(nb, true);
		Query q = new QueryParser(Version.LUCENE_40, "content", analyzer).parse(term);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		// Affichage des resultats
		if(hits.length>0) {
		System.out.println(hits.length + " résultat(s) :");
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			//System.out.println((i + 1) + " - " + d.get("filename") + " (score=" + hits[i].score + ")");
			System.out.println((i+1) + " - " + d.get("titre"));
			System.out.println("\t"+ "Accessible sur https://www.ncbi.nlm.nih.gov/pubmed/" + d.get("id"));
		}
		} else {System.out.println("Pas de résultats");}
		
		}
	
	private static void RequeteFr(int nb, String term) throws ParseException, IOException {
		// Requete avec l'analyzer francais
		//TODO change path
		String indexLocation="C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\corpusfr";
		FrenchAnalyzer analyzer = new FrenchAnalyzer(Version.LUCENE_40, FrenchAnalyzer.getDefaultStopSet());
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
		IndexSearcher searcher= new IndexSearcher(reader);
		TopScoreDocCollector collector ;
		collector = TopScoreDocCollector.create(nb, true);
		Query q = new QueryParser(Version.LUCENE_40, "contents", analyzer).parse(term);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		// Affichage des resultats
		if(hits.length>0) {
		System.out.println(hits.length + " resultat(s) :");
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println((i + 1) + " - " + d.get("filename"));
//			System.out.println((i+1) + " - " + d.get("titre"));
//			System.out.println("\t"+ "Accessible sur https://www.ncbi.nlm.nih.gov/pubmed/" + d.get("d"));
		}
		} else {System.out.println("Pas de resultats");}
		
		}
	
	public static void Recherche(ArrayList<HashSet<String>> synonymsUmls) {
		try{
			
			// Extraction et stockage des termes issus de l'UMLS
			Iterator<String> iterPT = synonymsUmls.get(0).iterator();
			String pt = "'" + iterPT.next() + "'" ;
			
			Iterator<String> iterEN = synonymsUmls.get(1).iterator();
			String syn_en = pt ;
			while (iterEN.hasNext()) {
				String i = iterEN.next();
				syn_en = syn_en + " '" + i + "'";
			}
			
			Iterator<String> iterFR = synonymsUmls.get(2).iterator();
			String syn_fr = null ;
			while (iterFR.hasNext()) {
				String i = iterFR.next();
				syn_fr = syn_fr + " '" + i + "'";
			}
			
		    		
			Scanner sc = new Scanner(System.in);
			String s = "";
			
			System.out.println("Combien de résultats voulez-vous afficher au maximum ?");
			int nb = sc.nextInt();
			String lang = sc.nextLine();
					
			while (!lang.equalsIgnoreCase("q")) {
				System.out.println("\nEn quelle langue souhaitez-vous faire la recherche ? (fr=français , en=anglais, q=quitter)");
				lang = sc.nextLine();
				
				if (lang.equalsIgnoreCase("q")) { 
					System.out.println("Fin de la recherche");
					break;
				}
				
				if (lang.equalsIgnoreCase("en")) {
				
					while (!s.equalsIgnoreCase("q")) {
						
						System.out.println("\nQuelle recherche souhaitez-vous faire ? "
								+ "\n\t1=requête simple \n\t2=requête étendue \n\tq=quitter");
						s = sc.nextLine();
						
						switch (s) {
						
						case "q": 
							break;
									
						case "1" :
							RequeteEn(nb, pt);
							break;
							
						case "2" :
							RequeteEn(nb, syn_en);
							break;
							
						default :
							System.out.println("\nErreur de saisie");
							
						}
						
					}
				} else if (lang.equalsIgnoreCase("fr")) {
						RequeteFr(nb, syn_fr);
				} else {
					System.out.println("\nErreur de saisie");
					
				}
			
			}
		}catch (Exception e) {
			System.out.println("Error searching : " + e.getMessage());
		} 
	}
	
	

	

	
}
