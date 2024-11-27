package huffman;

public class Noeud {
	String nom = "";
	int poids = 0;
	
	public Noeud(String nom, int poids) {
		this.nom = nom;
		this.poids = poids;
	}
	public Noeud(Character nom, int poids) {
		this.nom = nom.toString();
		this.poids = poids;
	}
	public String getNom() {
		return nom;
	}

	public int getPoids() {
		return poids;
	}
}
