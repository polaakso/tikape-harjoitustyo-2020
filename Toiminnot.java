package pakettiseuranta;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author petri.laakso
 */
public class Toiminnot {

    private Connection db;

    public Toiminnot() throws SQLException {
        this.db = DriverManager.getConnection("jdbc:sqlite:lahetys.db");
    }

// Toiminto 1
    public void alustaTietokanta() throws SQLException {
        Statement s = db.createStatement();

        try {
// Aloitetaan transaktio try-rakenteen sisällä
            s.execute("BEGIN TRANSACTION");
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("CREATE TABLE IF NOT EXISTS Asiakkaat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS Paikat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS Paketit (seurantakoodi TEXT PRIMARY KEY UNIQUE NOT NULL, asiakas_id INTEGER NOT NULL REFERENCES Asiakkaat)");
            s.execute("CREATE TABLE IF NOT EXISTS Tapahtumat (id INTEGER PRIMARY KEY, paketti_id INTEGER NOT NULL REFERENCES Paketit, paikka_id INTEGER NOT NULL REFERENCES Paikat, kuvaus TEXT DEFAULT('lisaa kuvaus'), pvm DATE NOT NULL, aika TEXT NOT NULL)");
// Indeksien luominen haluttaessa:
// s.execute("CREATE INDEX idx_asiakas_id ON Paketit (asiakas_id)");
// s.execute("CREATE INDEX idx_paketti_id ON Tapahtumat (paketti_id)");
            s.execute("COMMIT"); // Lopetetaan transaktio
        } catch (SQLException e) {
            System.out.println("VIRHE: Tietokannan luominen ei onnistunut");
        }
        System.out.println("Tietokanta luotu");
    }

// Toiminto 2
    public void lisaaPaikka(String lisattavanPaikanNimi) throws SQLException {

        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Paikat(nimi) VALUES(?)");
            p.setString(1, lisattavanPaikanNimi);
            p.executeUpdate();
            System.out.println("Paikka lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikan lisääminen ei onnistunut.\nPaikan nimi ei saa olla tyhjä eikä sama kuin toisen jo tietokannassa olevan paikan nimi");
        }
    }

// Toiminto 3
    public void lisaaAsiakas(String lisattavanAsiakkaanNimi) throws SQLException {

        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Asiakkaat(nimi) VALUES(?)");
            p.setString(1, lisattavanAsiakkaanNimi);
            p.executeUpdate();
            System.out.println("Asiakas lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakkaan lisääminen ei onnistunut.\nAsiakkaan nimi ei saa olla tyhjä eikä sama kuin toisen jo tietokannassa olevan asiakkaan nimi.");
        }
    }

// Toiminto 4
    public void lisaaPaketti(String paketinSeurantakoodi, String asiakkaanNimi) throws SQLException {

        try {
            PreparedStatement p1 = db.prepareStatement("SELECT * FROM Asiakkaat WHERE nimi=?");
            p1.setString(1, asiakkaanNimi);
            ResultSet rs = p1.executeQuery(); // Kyselyn tiedot talteen
            int id = rs.getInt("id");
            p1.close(); // Suljetaan, ettei tietokanta lukitu
            
            PreparedStatement p2 = db.prepareStatement("INSERT INTO Paketit(seurantakoodi,asiakas_id) VALUES (?,?)");
            p2.setString(1, paketinSeurantakoodi);
            p2.setInt(2, id);
            p2.executeUpdate();
            System.out.println("Paketti lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Paketin lisääminen ei onnistunut.\nPaketin seurantakoodi ei saa olla jo olemassa tietokannassa.\nAsiakkaan on oltava olemassa tietokannassa ennen paketin lisäämistä.");
        }
    }

// Toiminto 5
    public void lisaaTapahtuma(String paketinSeurantakoodi, String tapahtumanPaikka, String tapahtumanKuvaus) throws SQLException {
// Päivämäärän ja kellonajan hakeminen apumetodilta sekä niiden sijoittaminen muuttujiin
        String[] pvmaika = nyt();
        String pvm = pvmaika[0];
        String aika = pvmaika[1];

        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Tapahtumat(kuvaus, paketti_id, paikka_id, pvm, aika) VALUES (?, ?, (SELECT id FROM Paikat WHERE nimi=?), ?, ?)");
            p.setString(1, tapahtumanKuvaus);
            p.setString(2, paketinSeurantakoodi);
            p.setString(3, tapahtumanPaikka);
            p.setString(4, pvm);
            p.setString(5, aika);
            p.executeUpdate();
            System.out.println("Tapahtuma lisätty.");
        } catch (SQLException e) {
            System.out.println("VIRHE: Paketin lisääminen ei onnistunut.\nSeurantakoodin ja paikan on oltava olemassa tietokannassa ennen tapahtuman lisäämistä.");
        }
    }

// Toiminto 6
    public void haeTapahtumat(String paketinSeurantakoodi) throws SQLException {

        try {
            PreparedStatement p = db.prepareStatement("SELECT * FROM Tapahtumat JOIN Paikat ON Paikat.id=Tapahtumat.paikka_id WHERE Tapahtumat.paketti_id=?");
            p.setString(1, paketinSeurantakoodi);

            ResultSet rs = p.executeQuery(); // Kyselyn tiedot talteen        
            // Tulostetaan tapahtumat
            System.out.println("Paketin tapahtumat (ei tapahtumia jos tyhjä):");
            while (rs.next()) {
                System.out.println(rs.getString("pvm") + " "
                        + rs.getString("aika") + ", "
                        + rs.getString("nimi") + ", "
                        + rs.getString("kuvaus"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Tapahtumien hakeminen ei onnistunut.");
        }
    }

// Toiminto 7
    public void asiakkaanPaketit(String asiakkaanNimi) throws SQLException {

        try {

            PreparedStatement p = db.prepareStatement("SELECT seurantakoodi, COUNT(kuvaus) FROM Paketit"
                    + " LEFT JOIN Asiakkaat ON Asiakkaat.id=Paketit.asiakas_id LEFT JOIN Tapahtumat ON Tapahtumat.paketti_id=seurantakoodi WHERE nimi=?" + " GROUP BY seurantakoodi");
            p.setString(1, asiakkaanNimi);
            ResultSet rs = p.executeQuery(); // Kyselyn tiedot talteen
            System.out.println("Asiakkaan paketit (ei paketteja jos tyhjä):");
            while (rs.next()) {
                System.out.println(rs.getString("seurantakoodi") + ", "
                        + rs.getString("COUNT(kuvaus)") + " tapahtumaa");
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakkaan pakettitietojen hakeminen ei onnistunut.");
        }
    }

// Toiminto 8
    public void paikanTapahtumatValittunaPaivana(String paikanNimi, String valittuPvm) throws SQLException {

        try {
            PreparedStatement p = db.prepareStatement("SELECT COUNT(*) lkm FROM Tapahtumat JOIN Paikat on Tapahtumat.paikka_id=Paikat.id WHERE Tapahtumat.pvm=? AND Paikat.nimi=?");
            p.setString(1, valittuPvm);
            p.setString(2, paikanNimi);
            ResultSet rs = p.executeQuery(); // Kyselyn tiedot talteen
            System.out.print("Tapahtumien määrä: ");
            while (rs.next()) {
                System.out.print(rs.getInt("lkm") + "\n");
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikan tapahtumatietojen hakeminen ei onnistunut.");
        }
    }

// Aputoiminto: Aikaleiman hakeminen ja jakaminen päivämäärään sekä aikaan
    public static String[] nyt() {
        DateTimeFormatter pvmMuotoilija = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String aikaleima = pvmMuotoilija.format(now);
        String[] pvmJaAika = aikaleima.split(" ");
        return pvmJaAika;
    }
}
