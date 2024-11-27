package webpagesaver;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.net.httpserver.*;
import huffman.ArbreBinaire;
import java.nio.file.Files;
import java.nio.file.Paths;

	
public class WebPageSaver {
    static List<String> savedUrls = new ArrayList<>();
    static File cacheDirectory = new File("cache");
    static File FileToWrite = new File(cacheDirectory, "SavedURLFile.txt");
    static boolean serverRunning = false;
    static HttpServer server;

    public static void help() {
        System.out.println("usage: webpagesaver {add,remove,list,view,start,stop,test}");
    }

    public static void main(String[] args) throws IOException {
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs(); // Créer le dossier cache s'il n'existe pas
        }

        if (args.length == 2 && args[0].equals("add")) {
            String url = args[1];
            ensureServerRunning();
            sendRequestToServer("/add?url=" + URLEncoder.encode(url, "UTF-8"));
        } else if (args.length == 2 && args[0].equals("remove")) {
            String url = args[1];
            ensureServerRunning();
            sendRequestToServer("/remove?url=" + URLEncoder.encode(url, "UTF-8"));
        } else if (args.length == 1 && args[0].equals("list")) {
            ensureServerRunning();
            sendRequestToServer("/list");
        } else if (args.length == 2 && args[0].equals("view")) {
            String url = args[1];
            ensureServerRunning();
            sendRequestToServer("/view?url=" + URLEncoder.encode(url, "UTF-8"));
        } else if (args.length == 1 && args[0].equals("start")) {
            startServerInBackground();
        } else if (args.length == 1 && args[0].equals("stop")) {
            sendRequestToServer("/stop");
        } else if (args.length == 1 && args[0].equals("test")) {
            sendRequestToServer("/test");
        } else {
            help();
        }
    }

    private static void startServerInBackground() {
        new Thread(() -> {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void ensureServerRunning() throws IOException {
        if (!isServerRunning()) {
            startServerInBackground();
            try {
                // Attendre quelques secondes pour que le serveur démarre
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isServerRunning() {
        try {
            sendRequestToServer("/test");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
	private static void verifyFilesExist(String... fileNames) {
		for (String fileName : fileNames) {
			File file = new File(fileName);
			if (file.exists()) {
				System.out.println("Fichier trouvé : " + fileName);
			} else {
				System.out.println("Fichier NON trouvé : " + fileName);
			}
		}
	}

    private static void sendRequestToServer(String path) throws IOException {
        try {
            URL url = new URL("http://127.0.0.1:2024" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                }
                in.close();
            } else {
                System.out.println("Erreur: " + responseCode);
            }
            connection.disconnect();
        } catch (ConnectException e) {
            System.out.println("Erreur lors de l'envoi de la requête: Connexion refusée");
            throw e;
        }
    }

	private static void createViewPage(String fileNameHTML, String fileNameCSS, String fileNameImg) {
		try {
			// Vérifiez que les fichiers existent avant de les lire
			File htmlFile = new File(fileNameHTML);
			File cssFile = new File(fileNameCSS);
			File imgFile = new File(fileNameImg);
			verifyFilesExist(fileNameHTML, fileNameCSS, fileNameImg);


			if (!htmlFile.exists() || !cssFile.exists() || !imgFile.exists()) {
				System.out.println("Un ou plusieurs fichiers nécessaires n'existent pas.");
				return;
			}

			byte[] compressedHtml = Files.readAllBytes(Paths.get(fileNameHTML));
			ArbreBinaire huffmanTree = new ArbreBinaire("");
			String decompressedHtml = huffmanTree.decode(compressedHtml);

			byte[] compressedCss = Files.readAllBytes(Paths.get(fileNameCSS));
			String decompressedCss = huffmanTree.decode(compressedCss);

			byte[] compressedImg = Files.readAllBytes(Paths.get(fileNameImg));
			String decompressedImg = huffmanTree.decode(compressedImg);

			// Créer la page HTML de vue avec le contenu décompressé
			try (FileWriter fileWriter = new FileWriter(new File(cacheDirectory, "viewPage.html"))) {
				fileWriter.write("<html>");
				fileWriter.write("<head>");
				fileWriter.write("<title>Page Vue</title>");
				fileWriter.write("<style>");
				fileWriter.write(decompressedCss);
				fileWriter.write("</style>");
				fileWriter.write("</head>");
				fileWriter.write("<body>");
				fileWriter.write(decompressedHtml);
				fileWriter.write("<img src=\"" + decompressedImg + "\">");
				fileWriter.write("</body>");
				fileWriter.write("</html>");

				System.out.println("Page de vue créée avec succès : viewPage.html");
			} catch (IOException e) {
				System.out.println("Erreur lors de la création de la page de vue : " + e.getMessage());
			}
		} catch (IOException e) {
			System.out.println("Erreur lors de la lecture des fichiers compressés : " + e.getMessage());
		}
	}



    // Ouvre le fichier spécifié dans le navigateur par défaut
    private static void openFileInBrowser(String fileName) {
        try {
            File htmlFile = new File(cacheDirectory, fileName);
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Démarre le serveur HTTP sur le port 2024 s'il n'est pas déjà en cours d'exécution
    private static void startServer() throws IOException {
        if (serverRunning) {
            System.out.println("Le serveur est déjà en cours d'exécution.");
            return;
        }

        server = HttpServer.create(new InetSocketAddress(2024), 0);
        server.createContext("/add", new AddHandler());
        server.createContext("/remove", new RemoveHandler());
        server.createContext("/list", new ListHandler());
        server.createContext("/view", new ViewHandler());
        server.createContext("/stop", new StopHandler(server));
        server.createContext("/test", new TestHandler());
        server.start();
        serverRunning = true;
        System.out.println("Serveur démarré sur le port 2024.");
    }

    // Arrête le serveur HTTP s'il est en cours d'exécution
    private static void stopServer() {
        if (server != null) {
            server.stop(0);
            serverRunning = false;
            System.out.println("Serveur arrêté.");
        } else {
            System.out.println("Le serveur n'est pas en cours d'exécution.");
        }
    }

    // Teste le serveur local en envoyant une requête HTTP GET à l'URL "http://127.0.0.1:2024/test".
    private static void testServer() throws IOException {
        URL url = new URL("http://127.0.0.1:2024/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            System.out.println(response.toUpperCase());
            in.close();
        } else {
            System.out.println("X");
        }
        connection.disconnect();
    }

	private static void saveWebPage(String urlString) {
		HtmlExtractor(urlString);
		CssExtractor(urlString);

		// Compresser les fichiers HTML, CSS et images
		try {
			String htmlFileName = getFileNameFromURLHTML(new URL(urlString));
			String cssFileName = getFileNameFromURLCSS(new URL(urlString));
			String imgFileName = getFileNameFromURLImg(new URL(urlString));

			System.out.println("Compression du fichier HTML: " + htmlFileName);
			ArbreBinaire huffmanTree = new ArbreBinaire(htmlFileName);
			byte[] compressedHtml = huffmanTree.encode(htmlFileName);
			Files.write(Paths.get(htmlFileName), compressedHtml);
			System.out.println("Fichier HTML compressé avec succès.");

			System.out.println("Compression du fichier CSS: " + cssFileName);
			huffmanTree = new ArbreBinaire(cssFileName);
			byte[] compressedCss = huffmanTree.encode(cssFileName);
			Files.write(Paths.get(cssFileName), compressedCss);
			System.out.println("Fichier CSS compressé avec succès.");

			System.out.println("Compression du fichier image: " + imgFileName);
			huffmanTree = new ArbreBinaire(imgFileName);
			byte[] compressedImg = huffmanTree.encode(imgFileName);
			Files.write(Paths.get(imgFileName), compressedImg);
			System.out.println("Fichier image compressé avec succès.");
			verifyFilesExist(htmlFileName, cssFileName, imgFileName);

		} catch (IOException e) {
			e.printStackTrace();
		}

		appendToFile(urlString, FileToWrite);
	}


    private static void displaySavedUrls() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FileToWrite))) {
            String line;
            while ((line = reader.readLine()) != null) {
                savedUrls.add(line);
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameFromURLHTML(URL url) {
        String host = url.getHost();
        // suppression www pour le nom du fichier
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        return new File(cacheDirectory, "HTML" + host + ".txt").getPath();
    }

    private static String getFileNameFromURLCSS(URL url) {
        String host = url.getHost();
        // suppression www pour le nom du fichier
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        return new File(cacheDirectory, "CSS" + host + ".txt").getPath();
    }

    private static String getFileNameFromURLImg(URL url) {
        String host = url.getHost();
        // suppression www pour le nom du fichier
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        return new File(cacheDirectory, "IMG" + host + ".txt").getPath();
    }

    public static void appendToFile(String content, File filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
            writer.newLine(); // Ajoute une nouvelle ligne
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void HtmlExtractor(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();

            String fileNameHTML = getFileNameFromURLHTML(url);
            FileWriter fileWriterHTML = new FileWriter(fileNameHTML);
            BufferedReader readerHTML = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder htmlContent = new StringBuilder();

            while ((inputLine = readerHTML.readLine()) != null) {
                htmlContent.append(inputLine).append("\n");
            }

            fileWriterHTML.write(htmlContent.toString());
            readerHTML.close();
            fileWriterHTML.close();
            System.out.println("Page HTML sauvegardée: " + fileNameHTML);
        } catch (MalformedURLException e) {
            System.out.println("[HTML] url invalide: " + urlString);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[HTML] erreur avec: " + urlString);
            e.printStackTrace();
        }
    }

    public static void CssExtractor(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();

            String fileNameCSS = getFileNameFromURLCSS(url);
            String fileNameImg = getFileNameFromURLImg(url);
            BufferedReader readerCSS = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            FileWriter fileWriterCSS = new FileWriter(fileNameCSS);
            FileWriter fileWriterImg = new FileWriter(fileNameImg);
            StringBuilder CSSContent = new StringBuilder();
            StringBuilder ImgContent = new StringBuilder();
            String inputLine;

            while ((inputLine = readerCSS.readLine()) != null) {
                // Extraction du contenu des balises <style>
                CSSContent.append(extractContentWithTag(inputLine, "<style[^>]*>", "</style>"));
                // Extraction des liens images
                ImgContent.append(extractImageLinks(inputLine));
                // Extraction des balises <div style>
                CSSContent.append(extractContentWithTag(inputLine, "<div[^>]*style=\"([^\"]*)\"[^>]*>", ">"));
            }

            fileWriterCSS.append(CSSContent);
            fileWriterImg.append(ImgContent);
            readerCSS.close();
            fileWriterCSS.close();
            fileWriterImg.close();
            System.out.println("page CSS sauvegardée: " + fileNameCSS + " et liens des images sauvegardés :  " + fileNameImg);
        } catch (MalformedURLException e) {
            System.out.println("[CSS] URL invalide: " + urlString);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[CSS] Erreur avec: " + urlString);
            e.printStackTrace();
        }
    }

    public static String extractImageLinks(String input) {
        StringBuilder result = new StringBuilder();
        // Regex pour capturer les liens contenant des extensions d'images courantes
        String regex = "(?i)<[^>]*(src|href)=\"([^\"]*(png|jpg|jpeg|gif|bmp|svg)[^\"]*)\"[^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            result.append(matcher.group(2)).append("\n");
        }

        return result.toString();
    }

    private static String extractContentWithTag(String inputLine, String startPattern, String endPattern) {
        Pattern pattern = Pattern.compile(startPattern + "(.*?)" + endPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inputLine);
        StringBuilder content = new StringBuilder();
        while (matcher.find()) {
            content.append(matcher.group(1).trim()).append("\n"); // Extrait le contenu interne (excluant les balises) et ajoute une nouvelle ligne
        }
        return content.toString();
    }

    static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String url = getParameter(query, "url");
            if (url != null) {
                saveWebPage(url);
                String response = "Page ajoutée: " + url;
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            } else {
                String response = "URL manquante";
                exchange.sendResponseHeaders(400, response.getBytes("UTF-8").length);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        }
    }

    static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String url = getParameter(query, "url");
            if (url != null) {
                removeWebPage(url);
                String response = "Page supprimée: " + url;
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            } else {
                String response = "URL manquante";
                exchange.sendResponseHeaders(400, response.getBytes("UTF-8").length);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        }

        private void removeWebPage(String url) {
            // Suppression de la page
            System.out.println("Suppression de la page " + url);
            // Implémenter la suppression réelle du fichier et de l'entrée dans le fichier de sauvegarde
        }
    }

    static class ListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebPageGenerator.generatePage("SavedURLs.html");
            String response = "Liste générée. Ouvrez SavedURLs.html pour voir les pages sauvegardées.";
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }

    static class ViewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String url = getParameter(query, "url");
            if (url != null) {
                String fileNameHTML = getFileNameFromURLHTML(new URL(url));
                String fileNameCSS = getFileNameFromURLCSS(new URL(url));
                String fileNameImg = getFileNameFromURLImg(new URL(url));

                // Vérifier si les fichiers HTML, CSS et images existent déjà
                File htmlFile = new File(fileNameHTML);
                File cssFile = new File(fileNameCSS);
                File imgFile = new File(fileNameImg);

                if (htmlFile.exists() && cssFile.exists() && imgFile.exists()) {
                    System.out.println("Les fichiers existent déjà. Création de la page HTML locale pour afficher le contenu...");
                    createViewPage(fileNameHTML, fileNameCSS, fileNameImg);
                    System.out.println("Page HTML locale créée avec succès.");
                    String response = "Page affichée avec succès: viewPage.html";
                    exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes("UTF-8"));
                    os.close();
                    openFileInBrowser("viewPage.html");
                } else {
                    String response = "Les fichiers pour cette URL n'ont pas été téléchargés.";
                    exchange.sendResponseHeaders(404, response.getBytes("UTF-8").length);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes("UTF-8"));
                    os.close();
                }
            } else {
                String response = "URL manquante";
                exchange.sendResponseHeaders(400, response.getBytes("UTF-8").length);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        }
    }

    static class StopHandler implements HttpHandler {
        private final HttpServer server;

        StopHandler(HttpServer server) {
            this.server = server;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            server.stop(0);
            serverRunning = false; // Marquer le serveur comme arrêté
            String response = "Serveur arrêté.";
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            if (serverRunning) {
                response = "OK";
            } else {
                response = "X";
            }
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }

    private static String getParameter(String query, String parameterName) {
        if (query == null) {
            return null;
        }
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(parameterName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    public static class WebPageGenerator {
        public static void generatePage(String fileName) throws IOException {
            File file = new File(cacheDirectory, fileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(FileToWrite));
                 FileWriter fileWriter = new FileWriter(file)) {
                // Page HTML avec le titre et le corps
                fileWriter.write("<html>");
                fileWriter.write("<head><title>Liste des fichiers sauvegardés</title></head>");
                fileWriter.write("<body>");
                fileWriter.write("<h1>Liste des fichiers sauvegardés</h1>");
                fileWriter.write("<ul>");
                String line;
                // Lecture de chaque ligne du fichier SavedURLFile.txt et création d'un lien HTML pour chaque ligne
                while ((line = reader.readLine()) != null) {
                    fileWriter.write("<li><a href=\"http://127.0.0.1:2024/view?url=" + URLEncoder.encode(line, "UTF-8") + "\">" + line + "</a></li>");
                }
                fileWriter.write("</ul>");
                fileWriter.write("</body>");
                fileWriter.write("</html>");
                System.out.println("Page mise à jour avec succès : " + file.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Erreur lors de la mise à jour de la page : " + e.getMessage());
            }
        }
    }
}
