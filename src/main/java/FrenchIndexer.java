
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

public class FrenchIndexer {
	
	public static FrenchAnalyzer analyser = new FrenchAnalyzer(Version.LUCENE_40, FrenchAnalyzer.getDefaultStopSet());
	  public static IndexWriter writer;
	public static String indexLocation;
	  public ArrayList<File> queue = new ArrayList<File>();

	  public static void indexFrenchDoc() throws IOException {
		  

			 FrenchIndexer indexer = null;

			  indexLocation = "C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\corpusfr";
//			  TODO: change path
			  try {
				indexer = new FrenchIndexer(indexLocation);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

				  try {
					  //try to add file into the index
			  			String articlesfr = "C:\\Users\\Deanna\\Dropbox\\ISPED\\Sem1\\INF203 recherche dinfo\\projet\\articlesfr";
					  indexer.indexFileOrDirectory(articlesfr);
				  } catch (Exception e) {
					  System.out.println("Error indexing " + " : " + e.getMessage());
				  }

			  try {
				indexer.closeIndex();
			} catch (IOException e) {
				e.printStackTrace();
			}

}
	  
	  FrenchIndexer(String indexDir) throws IOException {
		    // the boolean true parameter means to create a new index everytime, 
		    // potentially overwriting any existing files there.
		    FSDirectory dir = FSDirectory.open(new File(indexDir));


		    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyser);

		    writer = new IndexWriter(dir, config);
		  }
	  
	  /**
	   * Indexes a file or directory
	   * @param fileName the name of a text file or a folder we wish to add to the index
	   * @throws java.io.IOException when exception
	   */
	  public void indexFileOrDirectory(String fileName) throws IOException {
	    //===================================================
	    //gets the list of files in a folder (if user has submitted
	    //the name of a folder) or gets a single file name (is user
	    //has submitted only the file name) 
	    //===================================================
	    addFiles(new File(fileName));
	    
	    int originalNumDocs = writer.numDocs();
	    for (File f : queue) {
	      FileReader fr = null;
	      try {
	        Document doc = new Document();

	        //===================================================
	        // add contents of file
	        //===================================================
	        fr = new FileReader(f);
	        doc.add(new TextField("contents", fr));
	        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
	        doc.add(new StringField("filename", f.getName(), Field.Store.YES));

	        writer.addDocument(doc);
//	        System.out.println("Added: " + f);
	      } catch (Exception e) {
	        System.out.println("Could not add: " + f);
	      } finally {
	        fr.close();
	      }
	    }
	    
	    int newNumDocs = writer.numDocs();
	    System.out.println((newNumDocs - originalNumDocs) + " articles ajoutés (corpus français).");
	    queue.clear();
	  }

	  private void addFiles(File file) {

	    if (!file.exists()) {
	      System.out.println(file + " does not exist.");
	    }
	    if (file.isDirectory()) {
	      for (File f : file.listFiles()) {
	        addFiles(f);
	      }
	    } else {
	      String filename = file.getName().toLowerCase();
	      //===================================================
	      // Only index text files
	      //===================================================
	      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
	              filename.endsWith(".xml") || filename.endsWith(".txt")) {
	        queue.add(file);
	      } else {
	        System.out.println("Skipped " + filename);
	      }
	    }
	  }

	  /**
	   * Close the index.
	   * @throws java.io.IOException when exception closing
	   */
	  public void closeIndex() throws IOException {
	    writer.close();
	  }

}
