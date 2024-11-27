package huffman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * La classe ArbreBinaire implémente un arbre binaire strict,
 * généré à partir d'un texte, et servant de base au codage
 * de Huffman.
 * L'implémentation faite ici est récursive : chaque branche est elle-même 
 * un arbre binaire.
 */
@SuppressWarnings("unused")
public class ArbreBinaire implements Comparator<Noeud>{
	/**
    * Les branches de l'arbre binaire sont elles-mêmes des arbres binaire.
    * Comme l'arbre est strictement binaire, il peut y avoir soit deux branches,
    * soit aucune (en ce cas, l'arbre est composée juste d'une feuille).
	 */
	private ArbreBinaire[] branches;

	/**
	 * Le nœud stocke le poids total de l'arbre, et le plus petit caractère
     * (pour l'ordre lexicographique) parmi toutes ses feuilles.
	 */
	private Noeud noeud;
	
	/**
     * Ce constructeur sert à créer un arbre constitué d'une seule feuille.
     * @param noeud Le nœud feuille de l'arbre binaire
     */
	public ArbreBinaire(Noeud noeud) {
		this.branches = new ArbreBinaire[0];
		this.noeud = noeud;
	}

	/**
     * Ce constructeur crée un arbre avec 2 branches.
     * @param noeud Le nœud parent de l'arbre binaire
     * @param branches Les branches de l'arbre binaire
     */
	public ArbreBinaire(Noeud noeud, ArbreBinaire[] branches) {
		this.branches = branches;
		this.noeud = noeud;
	}
	
	
	/**
     * Ce constructeur génère un arbre binaire à partir d'un fichier texte.
     * @param fichier Le chemin du fichier texte
     */
	public ArbreBinaire(String fichier) {
		
		String texte = getContentFichier(fichier);
		
		// On stocke les lettres et leur nombre d'apparition
		HashMap<Character, Integer> lettres = new HashMap<>();
		
		// On stoque les noeuds qu'on a crée dans un priority queue 
		// pour les trier par la suite
		PriorityQueue<Noeud> noeuds = new PriorityQueue<>(this);
		
		HashMap<Noeud, ArbreBinaire> arbreBinaires = new HashMap<>();
		// On converti le String texte en tableau de char pour le parcourir
		for(char c : texte.toCharArray()) {
			if(lettres.containsKey(c)) {
				lettres.replace(c, lettres.get(c) + 1);
			}
			else {
				lettres.put(c, 1);
			}
		}
		
		// On remplit la priority queue avec les noeuds
		for(Character key : lettres.keySet()) {
			noeuds.add(new Noeud(key, lettres.get(key)));
		}
		
		// On crée l'arbre en combinant les noeuds existants
		while(noeuds.size() > 1) {
			
			// On récupere les deux noeuds enfants
			Noeud noeudGauche = noeuds.poll();
			Noeud noeudDroit = noeuds.poll();

			// On crée le noeud parent avec les deux noeuds enfants
			Noeud noeudParent = new Noeud(noeudGauche.getNom(),noeudGauche.getPoids()+noeudDroit.getPoids());
			noeuds.add(noeudParent);
			
			ArbreBinaire branche1;
			ArbreBinaire branche2;
			
			// Si l'arbre existe deja on remplit la branche avec l'arbre existant
			if(arbreBinaires.containsKey(noeudGauche)) {
				branche1 = arbreBinaires.get(noeudGauche);
			}
			else { // Sinon on crée un arbre et on le stock dans branche
				branche1 = new ArbreBinaire(noeudGauche);
			}
			
			if(arbreBinaires.containsKey(noeudDroit)) {
				branche2 = arbreBinaires.get(noeudDroit);
			}
			else {
				branche2 = new ArbreBinaire(noeudDroit);
			}
			
			// On crée un nouvel arbre avec les deux branches d'en dessous
			arbreBinaires.put(noeudParent, new ArbreBinaire(noeudParent,new ArbreBinaire[] {branche1,branche2}));
			
		}
			
		this.noeud = noeuds.poll();
		this.branches = arbreBinaires.get(this.noeud).branches;
	}
					
	 /**
     * Lit le contenu d'un fichier.
     * @param fichier Le chemin du fichier texte
     * @return Le contenu du fichier sous forme de chaîne de caractères
     */
	public String getContentFichier(String fichier) {
			
		String texte = "";
		
		try {
			// Création d'un fileReader pour lire le fichier
			FileReader fileReader = new FileReader(fichier);
			
			// Création d'un bufferedReader qui utilise le fileReader
			BufferedReader reader = new BufferedReader(fileReader);
			
			// une fonction à essayer pouvant générer une erreur
			String line = reader.readLine();
			
			while (line != null) {
				// On remplit la variable texte avec la ligne en cours
				texte = texte + line;
				// lecture de la prochaine ligne
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return texte;
	}
 	
	
	
	/**
        * Écrit un code dans un fichier.
        * @param tab Le code à écrire dans le fichier
        */
	public void writeCode(byte[] tab) {
		
		try {
			
			String fichier = "fichiers/texteCompresse";
			
			// Création d'un fileWriter pour écrire dans un fichier
			FileOutputStream sortie = new FileOutputStream(fichier);
			
			// ajout d'un texte à notre fichier
			sortie.write(tab);
			
			sortie.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Écrit un code dans un fichier.
     * @param tab Le code à écrire dans le fichier
     */
	public void writeCode(String texte) {
		
		try {
			
			String fichier = "fichiers/texteDecode.txt";
			
			// Création d'un fileWriter pour écrire dans un fichier
			FileWriter fileWriter = new FileWriter(fichier);
			
			// Création d'un bufferedWriter qui utilise le fileWriter
			BufferedWriter writer = new BufferedWriter(fileWriter);
						
			// ajout d'un texte à notre fichier
			writer.write(texte);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 /**
         * Crée un dictionnaire d'encodage pour l'arbre binaire.
    	 * @param arbreBinaire L'arbre binaire
     	 * @param dictionnaire Le dictionnaire d'encodage à remplir
     	 * @param code Le code en cours de création
     	 */
	public void creerDictionnaire(ArbreBinaire arbreBinaire, HashMap<String, String> dictionnaire, String code) {
	
		
		if(arbreBinaire.branches.length == 0) {
		  // Si l'arbre est une feuille, ajoute son caractère avec son code dans le dictionnaire
			dictionnaire.put(arbreBinaire.noeud.getNom(), code);
		}
		else {
		 // Si l'arbre a des branches, récursivement, continue à construire le code pour chaque branche
			creerDictionnaire(arbreBinaire.branches[0], dictionnaire, code + "0");
			creerDictionnaire(arbreBinaire.branches[1], dictionnaire, code + "1");
		}
			
	}
	
	
	public static byte[] binaryStringToByteArray(String binaryString) {
        int longueur = binaryString.length();
        int byteLength = (longueur + 7) / 8; // Calculer la longueur du tableau de bytes (arrondir vers le haut)
        byte[] byteArray = new byte[byteLength];

        for (int i = 0; i < longueur; i += 8) {
            String byteString;
            if (i + 8 <= longueur) {
                byteString = binaryString.substring(i, i + 8);
            } else {
                // Si la longueur n'est pas un multiple de 8, remplir les bits manquants avec des '0'
                byteString = binaryString.substring(i) + "00000000".substring(binaryString.substring(i).length());
            }
            byteArray[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }

        return byteArray;
    }
	
	
	public static String byteArrayToBinaryString(byte[] byteArray) {
        StringBuilder binaryString = new StringBuilder();

        for (byte b : byteArray) {
            // Convertir chaque byte en une chaîne binaire de 8 bits
            String byteString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binaryString.append(byteString);
        }

        return binaryString.toString();
    }
	
	

	/**
	 * Encode un texte en utilisant l'algorithme de Huffman.
	 *
	 * Cette méthode lit un fichier texte, construit un dictionnaire d'encodage basé sur un arbre de Huffman, et 
	 * encode le texte en une chaîne de bits. Le texte encodé et le dictionnaire sont ensuite concaténés et écrits 
	 * dans un fichier.
	 *
	 * @param fichier Le chemin du fichier texte à encoder.
	 * @return Le texte encodé sous forme d'une chaîne de 0 et de 1, suivi du dictionnaire d'encodage.
	 * 
	 * Étapes de la méthode :
	 * 1. Lire le contenu du fichier spécifié.
	 * 2. Créer un dictionnaire d'encodage basé sur l'arbre de Huffman.
	 * 3. Encoder le texte en utilisant le dictionnaire d'encodage.
	 * 4. Concaténer le texte encodé et le dictionnaire avec un séparateur.
	 * 5. Écrire le code final dans un fichier.
	 */
	public byte[] encode(String fichier) {
	    // Lire le contenu du fichier texte
	    String texte = getContentFichier(fichier);
	    
	    // StringBuilder possede des methodes plus rapides pour y ajouter du texte
	    StringBuilder texteEncode = new StringBuilder();
	    StringBuilder dic = new StringBuilder();
	    
	    String separateur = "1000000000000001";  // Séparateur unique pour distinguer le texte encodé du dictionnaire
	    String separateurElementsDictionnaire = "011111111110";
	    String separateurCleValeur = "01111111110";
	    String codeFinal;

	    // Créer le dictionnaire d'encodage
	    HashMap<String, String> dictionnaire = new HashMap<>();
	    creerDictionnaire(this, dictionnaire, "");

	    // Formatage du dictionnaire
	    for (Map.Entry<String, String> dictionnaireTemp : dictionnaire.entrySet()) {
	    	dic.append(String.format("%8s", Integer.toBinaryString(dictionnaireTemp.getKey().charAt(0))).replace(" ", "0") + separateurCleValeur + dictionnaireTemp.getValue() + separateurElementsDictionnaire);
	    }
	    

	    // Encodage du texte
	    for (char c : texte.toCharArray()) {
	    	texteEncode.append(dictionnaire.get(Character.toString(c)));
	    }

	    // Construction du code final
	    codeFinal = dic + separateur + texteEncode;

	    // Écriture dans un fichier
	    writeCode(binaryStringToByteArray(codeFinal));
	    
	    System.out.println(codeFinal);

	    return binaryStringToByteArray(codeFinal);
	}


	/**
     * Transforme une chaîne de caractères en un dictionnaire d'encodage.
     * @param dictionnaireString La chaîne de caractères représentant le dictionnaire
     * @return Le dictionnaire d'encodage sous forme de HashMap
     */
	public HashMap<String, String> stringToHashMap(String dictionnaireString){
		
		String separateurElementsDictionnaire = "011111111110";
		String separateurCleValeurDictionnaire = "01111111110";
		
		HashMap<String, String> dictionnaireHashMap = new HashMap<>();
		// Sépare la chaîne de caractères pour récupérer chaque entrée de dictionnaire
		String[] caracteres = dictionnaireString.split(separateurElementsDictionnaire);
		String[] temp = null;
		
		for(String s: caracteres) {
		// Pour chaque entrée, sépare la clé et la valeur pour les ajouter au dictionnaire
			temp = s.split(separateurCleValeurDictionnaire);
			if(temp.length == 2) {
				dictionnaireHashMap.put(Character.toString((char)Integer.parseInt(temp[0], 2)), temp[1]);
			}
		}
		
		return dictionnaireHashMap;
	}
	
	
	
	
	
	
	/**
	 * Décode un texte encodé en utilisant l'algorithme de Huffman.
	 *
	 * Cette méthode prend une chaîne de caractères encodée avec Huffman, extrait le dictionnaire d'encodage et 
	 * reconstruit le texte original. Le dictionnaire et le texte encodé sont séparés par un séparateur unique.
	 *
	 * @param code Le texte encodé sous forme d'une chaîne de 0 et de 1, suivi du dictionnaire d'encodage.
	 * @return Le texte décodé.
	 * 
	 * Étapes de la méthode :
	 * 1. Trouver la position du séparateur unique.
	 * 2. Séparer le texte encodé et le dictionnaire.
	 * 3. Convertir le dictionnaire de sa représentation en chaîne de caractères à une HashMap.
	 * 4. Parcourir le texte encodé et reconstruire le texte original en utilisant le dictionnaire.
	 */
	public StringBuilder decode(byte[] byteArray) {
	    
		String code = byteArrayToBinaryString(byteArray);
		String separateurCodeDictionnaire = "1000000000000001";
		// Trouver la position du séparateur
	    int index = code.indexOf(separateurCodeDictionnaire);

	    // Extraire le dictionnaire et le code
	    String[] dictionnaireEtCode = { code.substring(0, index), code.substring(index + separateurCodeDictionnaire.length()) };

	    // Convertir le dictionnaire en HashMap
	    HashMap<String, String> dictionnaire = stringToHashMap(dictionnaireEtCode[0]);

	    // Initialiser les variables pour le décodage
	    String texteEncode = dictionnaireEtCode[1];
	    String temp = "";
	    StringBuilder texteDecode = new StringBuilder();

	    // Décodage du texte
	    for (Character car : texteEncode.toCharArray()) {
			temp = temp.concat(car.toString());
			if (dictionnaire.containsValue(temp)) {
			    for (Map.Entry<String, String> m : dictionnaire.entrySet()) {
			        if (temp.equals(m.getValue())) {
			        	texteDecode.append(m.getKey());
			            break;
			        }
			    }
			    temp = "";
			}
	    }

	    System.out.println(texteDecode);
	    
	    writeCode(texteDecode.toString());
	    return texteDecode;
	}

	
	 /**
     * Génère le dictionnaire d'encodage à partir de l'arbre binaire.
     * @return Le dictionnaire d'encodage sous forme de HashMap
     */
	public HashMap<String, String> dictionnaireEncodage() {
		
		HashMap<String, String> dictionnaire = new HashMap<>();
		
		creerDictionnaire(this, dictionnaire, "");
		
		return dictionnaire;
	}

   /**
     * Compare deux nœuds en fonction de leur poids. Si les poids sont égaux, compare les nœuds en fonction de leur nom.
     * @param noeud1 Le premier nœud à comparer
     * @param noeud2 Le second nœud à comparer
     * @return Une valeur négative, zéro ou une valeur positive si le premier nœud est respectivement moins que, égal à ou supérieur au second nœud
     */
	@Override
	public int compare(Noeud noeud1, Noeud noeud2) {
		if(noeud1.getPoids() == noeud2.getPoids()) {
			return noeud1.getNom().compareTo(noeud2.getNom());
		}
		else {
			return noeud1.getPoids() - noeud2.getPoids();
		}
	}


}


