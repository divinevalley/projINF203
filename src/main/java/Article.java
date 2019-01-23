

import java.util.ArrayList;
import java.util.List;

public class Article {
	
	public static List<Article> l = new ArrayList<Article>();
	public static int nbrInstances = 0;
	private String id;
	private String langue;
	private String titre;
	private String resume;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLangue() {
		return langue;
	}
	public void setLangue(String langue) {
		this.langue = langue;
	}
	
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	
	public String getResume() {
		return resume;
	}
	public void setResume(String resume) {
		this.resume = resume;
	}
	
	public Article() {
		nbrInstances ++;
	}
	
	public static int getNbrInstances() {
		return nbrInstances;
	}
	
}
