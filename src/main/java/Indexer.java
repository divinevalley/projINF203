import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	
	private static IndexWriter writer;
	public static List<Document> docs = new ArrayList<Document>();
	private static IndexWriterConfig config;
	public static String indexDir;
	
	public static void indexDoc() throws IOException {
		//TODO change path
		indexDir = "C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\corpus";
		
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		
		for(int i=0;i<docs.size();i++) {
			String lang = docs.get(i).get("langue");	
		if(lang.equals("eng")) {
				EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40, EnglishAnalyzer.getDefaultStopSet());
				config = new IndexWriterConfig(Version.LUCENE_40, analyzer);				  
			} else if (lang.equals("FRE")) {
				FrenchAnalyzer analyser = new FrenchAnalyzer(Version.LUCENE_40);
				config = new IndexWriterConfig(Version.LUCENE_40, analyser);
			}
		}
			
		writer = new IndexWriter(dir, config);
		
		for(int i=0;i<docs.size();i++) {
			writer.addDocument(docs.get(i));
		}
		
		writer.close();
	}
   
   public static List<Document> createDocLucene() {

        
        for(int i = 0; i<Article.l.size(); i++) {
        	Document doc = new Document();
            
            String id = Article.l.get(i).getId();
            String lang = Article.l.get(i).getLangue();
            String title = Article.l.get(i).getTitre();
            String texte = Article.l.get(i).getResume();
    		
    		doc.add(new StringField ("id", id, Field.Store.YES));
    		doc.add(new TextField("langue", lang, Field.Store.YES));
    		doc.add(new TextField ("titre", title, Field.Store.YES));
    		doc.add(new TextField ("content", texte, Field.Store.NO));
    		
    		docs.add(doc);
        }
        return docs;
	}
}
