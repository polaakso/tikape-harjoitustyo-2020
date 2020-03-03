package pakettiseuranta;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
import java.sql.*;
import java.util.Random;

/**
 *
 * @author petri.laakso
 */
public class TehoTesti {

    private Connection db;

    public TehoTesti() throws SQLException {
// Testit testikannalla "testi.db"
        this.db = DriverManager.getConnection("jdbc:sqlite:testi.db");

    }

    public void suoritaTesti() throws SQLException {
// Tyhjennetään tietokanta ja luodaan se uudelleen
        Statement s = db.createStatement();
        try {
// Aloitetaan transaktio
            s.execute("BEGIN TRANSACTION");
            s.execute("DROP TABLE IF EXISTS Asiakkaat");
            s.execute("DROP TABLE IF EXISTS Paikat");
            s.execute("DROP TABLE IF EXISTS Paketit");
            s.execute("DROP TABLE IF EXISTS Tapahtumat");
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("CREATE TABLE Asiakkaat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE NOT NULL)");
            s.execute("CREATE TABLE Paikat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE NOT NULL)");
            s.execute("CREATE TABLE Paketit (seurantakoodi TEXT PRIMARY KEY UNIQUE NOT NULL, asiakas_id INTEGER NOT NULL REFERENCES Asiakkaat)");
            s.execute("CREATE TABLE Tapahtumat (id INTEGER PRIMARY KEY, paketti_id INTEGER NOT NULL REFERENCES Paketit, paikka_id INTEGER NOT NULL REFERENCES Paikat, kuvaus TEXT DEFAULT('lisaa kuvaus'), pvm DATE NOT NULL, aika TEXT NOT NULL)");
            // Indeksien luominen
            //        s.execute("CREATE INDEX idx_asiakas_id ON Paketit (asiakas_id)");
            //        s.execute("CREATE INDEX idx_paketti_id ON Tapahtumat (paketti_id)");
            s.execute("COMMIT"); // Lopetetaan transaktio

            s.execute("BEGIN TRANSACTION"); // Aloitetaan transaktio vaiheille 1-4
            PreparedStatement p1 = db.prepareStatement("INSERT INTO Paikat (nimi) VALUES (?)");
            PreparedStatement p2 = db.prepareStatement("INSERT INTO Asiakkaat (nimi) VALUES (?)");
            PreparedStatement p3 = db.prepareStatement("INSERT INTO Paketit (seurantakoodi, asiakas_id) VALUES (?, (SELECT id FROM Asiakkaat WHERE nimi=?))");
            PreparedStatement p4 = db.prepareStatement("INSERT INTO Tapahtumat (paketti_id, paikka_id, kuvaus, pvm, aika) VALUES (?, (SELECT id FROM Paikat WHERE nimi=?),?,?,?)");

// Vaihe 1 - Lisätään 1000 x Paikat.nimi
            long aloitusAika1 = System.nanoTime();
            for (int i = 0; i < 1e3; i++) {
                p1.setString(1, "P" + i);
                p1.executeUpdate();
            }
            long lopetusAika1 = System.nanoTime();

// Vaihe 2 - Lisätään 1000 x Asiakkaat.nimi
            long aloitusAika2 = System.nanoTime();
            for (int i = 0; i < 1e3; i++) {
                p2.setString(1, "A" + i);
                p2.executeUpdate();
            }
            long lopetusAika2 = System.nanoTime();

// Vaihe 3 - Lisätään 1000 x Paketit.seurantakoodi, Paketit.asiakas_id
            long aloitusAika3 = System.nanoTime();
            for (int i = 0; i < 1e3; i++) {
                p3.setString(1, "K" + i);
                p3.setString(2, "A" + i);
                p3.executeUpdate();
            }
            long lopetusAika3 = System.nanoTime();

// Vaihe 4 -Lisätään 1e6 riviä Tapahtumat-taulun kaikkiin kenttiin, joista jokaiseen liittyy paketti, jonka seurantakoodi on muotoa [Ki], jossa i on satunnaisLuku välillä 0-999
            Random arvoLuku = new Random(); // Satunnaisluku 
            int satunnaisLuku;

            long aloitusAika4 = System.nanoTime();
            for (int i = 0; i < 1e6; i++) {
                satunnaisLuku = arvoLuku.nextInt(999);
                p4.setString(1, "K" + satunnaisLuku); // Luvun oltava välillä 0-999, jotta seurantakoodi löytyy
                p4.setString(2, "P" + satunnaisLuku); // Luvun oltava välillä 0-999, jotta paikka löytyy
                p4.setString(3, "Testitapahtuma " + i); // Havainnollisuuden vuoksi laitettu +i, jotta voi koodia testatessa tarkistaa tuloksen helposti tietokannasta
                p4.setString(4, "2020/02/29"); // Kovakoodattu, koska ei väliä testin kannalta
                p4.setString(5, "10:00:00"); // Kovakoodattu, koska ei väliä testin kannalta
                p4.executeUpdate();
            }
            long lopetusAika4 = System.nanoTime();
            s.execute("COMMIT"); // Vaiheiden 1-4 transaktio päättyy
// Vaihe 5
            PreparedStatement p5 = db.prepareStatement("SELECT COUNT(*) FROM Paketit WHERE asiakas_id=(SELECT id FROM Asiakkaat WHERE nimi=?)");
            long aloitusAika5 = System.nanoTime();
            for (int i = 0; i < 1e3; i++) {
                p5.setString(1, "A" + i); // Yksinkertaisuuden vuoksi haetaan kukin tehotestin luoma paketti
                p5.executeQuery();
            }
            long lopetusAika5 = System.nanoTime();
// Vaihe 6
            PreparedStatement p6 = db.prepareStatement("SELECT COUNT(*) FROM Tapahtumat WHERE paketti_id=?");
            long aloitusAika6 = System.nanoTime();
            for (int i = 0; i < 1e3; i++) {
                p6.setString(1, "K" + i); // Yksinkertaisuuden vuoksi haetaan kukin tehotestin luoma paketti
                p6.executeQuery();
            }
            long lopetusAika6 = System.nanoTime();
            System.out.println("Tehotestin tulokset:");
            System.out.println("Vaihe 1 kesti " + ((lopetusAika1 - aloitusAika1) / 1e9) + " sekuntia");
            System.out.println("Vaihe 2 kesti " + ((lopetusAika2 - aloitusAika2) / 1e9) + " sekuntia");
            System.out.println("Vaihe 3 kesti " + ((lopetusAika3 - aloitusAika3) / 1e9) + " sekuntia");
            System.out.println("Vaihe 4 kesti " + ((lopetusAika4 - aloitusAika4) / 1e9) + " sekuntia");
            System.out.println("Vaihe 5 kesti " + ((lopetusAika5 - aloitusAika5) / 1e9) + " sekuntia");
            System.out.println("Vaihe 6 kesti " + ((lopetusAika6 - aloitusAika6) / 1e9) + " sekuntia");
        } catch (SQLException e) {
            System.out.println("VIRHE: Testin suorittaminen ei onnistunut" + e);
        }
    }
}
