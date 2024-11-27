package huffman;

public class testArbreBinaire {
	public static void main(String[] args) {
		String fichier = "fichiers/texte.txt";
		ArbreBinaire arbre = new ArbreBinaire(fichier);
		arbre.encode(fichier);
		arbre.decode(arbre.encode(fichier));
	}
}
