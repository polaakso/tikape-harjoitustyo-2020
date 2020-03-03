package pakettiseuranta;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
import java.util.Scanner;
import java.sql.*;

/**
 *
 * @author petri.laakso
 */
public class Tekstikayttoliittyma {

    private Scanner lukija;
    private Toiminnot toiminnot;

    public Tekstikayttoliittyma() throws SQLException {
        this.lukija = new Scanner(System.in);
        this.toiminnot = new Toiminnot();
    }

    public void aloita() throws SQLException {
        OUTER:
        while (true) {
            System.out.println("Valitse toiminto (1-9), v = valikko: ");
            String toiminto = lukija.nextLine();
            switch (toiminto) {
                case "-1":
                    System.out.println("Ohjelma suljettiin.");
                    break OUTER;
                case "v":
                    System.out.println("1 = Luo tietokantataulut");
                    System.out.println("2 = Lisää uusi paikka");
                    System.out.println("3 = Lisää uusi asiakas");
                    System.out.println("4 = Lisää uusi paketti");
                    System.out.println("5 = Skannaa paketti");
                    System.out.println("6 = Hae paketin tapahtumat");
                    System.out.println("7 = Hae asiakkaan paketit tapahtumamäärineen");
                    System.out.println("8 = Hae paikan tapahtumat valittuna päivänä");
                    System.out.println("9 = Tietokannan tehokkuustesti");
                    System.out.println("-1 = Lopeta ohjelma");
                    break;
                case "1":
                    this.toiminnot.alustaTietokanta();
                    break;
                case "2":
                    System.out.println("Anna paikan nimi: ");
                    String lisattavanPaikanNimi = lukija.nextLine();
                    this.toiminnot.lisaaPaikka(lisattavanPaikanNimi);
                    break;
                case "3":
                    System.out.println("Anna asiakkaan nimi: ");
                    String lisattavanAsiakkaanNimi = lukija.nextLine();
                    this.toiminnot.lisaaAsiakas(lisattavanAsiakkaanNimi);
                    break;
                case "4": {
                    System.out.println("Anna paketin seurantakoodi: ");
                    String paketinSeurantakoodi = lukija.nextLine();
                    System.out.println("Anna asiakkaan nimi: ");
                    String asiakkaanNimi = lukija.nextLine();
                    this.toiminnot.lisaaPaketti(paketinSeurantakoodi, asiakkaanNimi);
                    break;
                }
                case "5": {
                    System.out.println("Anna paketin seurantakoodi: ");
                    String paketinSeurantakoodi = lukija.nextLine();
                    System.out.println("Anna tapahtuman paikka: ");
                    String tapahtumanPaikka = lukija.nextLine();
                    System.out.println("Anna tapahtuman kuvaus: ");
                    String tapahtumanKuvaus = lukija.nextLine();
                    this.toiminnot.lisaaTapahtuma(paketinSeurantakoodi, tapahtumanPaikka, tapahtumanKuvaus);
                    break;
                }
                case "6": {
                    System.out.println("Anna paketin seurantakoodi: ");
                    String paketinSeurantakoodi = lukija.nextLine();
                    this.toiminnot.haeTapahtumat(paketinSeurantakoodi);
                    break;
                }
                case "7": {
                    System.out.println("Anna asiakkaan nimi: ");
                    String asiakkaanNimi = lukija.nextLine();
                    this.toiminnot.asiakkaanPaketit(asiakkaanNimi);
                    break;
                }
                case "8":
                    System.out.println("Anna paikan nimi: ");
                    String paikanNimi = lukija.nextLine();
                    System.out.println("Anna päivämäärä muodossa VVVV/KK/PP: ");
                    String valittuPvm = lukija.nextLine();
                    this.toiminnot.paikanTapahtumatValittunaPaivana(paikanNimi, valittuPvm);
                    break;
                case "9":
                    String[] pvmaika;
                    pvmaika = Toiminnot.nyt();
                    String aika = pvmaika[1];
                    System.out.println("Tehotesti alkoi klo " + aika);
                    TehoTesti testi = new TehoTesti();
                    testi.suoritaTesti();
                    break;
                default:
                    System.out.println("Tuntematon toiminto '" + toiminto
                            + "'\nValitse jokin valikon toiminnoista.");
                    break;
            }
        }
    }
}
