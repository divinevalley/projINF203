

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyXMLHandler extends DefaultHandler {
	//les"ou" dans les if servent Ã  traiter 2 structures XML differentes: attention Ã  ce qu'il n'y ait pas de balises communes
	//-------->plutot faire 2 handler differents (via classe ou constructeur)
	
	private String node = null;
	public String pmid;
	public String langue;
	public String titre;
	public String resume;
	private Article article;


	   //début du parsing
	   public void startDocument() throws SAXException {
	      System.out.println("Début du parsing");
	   }
	   //fin du parsing
	   public void endDocument() throws SAXException {
	      System.out.println("Fin du parsing");
	   }
	   
	   //crée un objet article des qu'il voit la balise article
	   public void startElement(String namespaceURI, String lname, String qname, Attributes attrs) throws SAXException {
	      node = qname;
	      
	      if(qname.equals("PubmedArticle") | qname.equals("record")) {
	    	  article = new Article();  
	      }
	   }
	   
	   //stocke l'objet des qu'il voit la fin de la balise
	   public void endElement(String uri, String localName, String qName) throws SAXException{
		   if(qName.equals("PubmedArticle") | qName.equals("record")) {
			   Article.l.add(article);
		   }
		   
	   }
	   
	   //permet de récupérer la valeur d'un noeud
	   public void characters(char[] data, int start, int end) {  
	      
	      String str = new String(data, start, end);
	      
	      	  if(node.equals("PMID") | node.equals("REF")) {
		    	  article.setId(str);
		      } else if (node.equals("Language") | node.equals("LangResu")) {
		    	  article.setLangue(str);
		      } else if (node.equals("ArticleTitle") | node.equals("TitOrigA")) {
		    	  article.setTitre(str);
		      } else if(node.equals("AbstractText") | node.equals("Resum")) {
		    	  article.setResume(str);
		      }
	      }
	   
	   
	   
	   
	   public static void parserXml() throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory factory = SAXParserFactory.newInstance();
	        SAXParser parser = factory.newSAXParser();
	        
	        
	        MyXMLHandler handler = new MyXMLHandler();
	        //TODO change path
	        parser.parse("C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\pubmed_result.xml", handler);
		} 
	      
	   
}
