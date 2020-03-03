package pakettiseuranta;

/**
 * To change this license header, choose License Headers in Project Properties.*
 * To change this template file, choose Tools | Templates* and open the template
 * in the editor.
 */
import java.sql.*;

/**
 * ** @author petri.laakso
 */
public class Main {

    public static void main(String[] args) throws SQLException {
// Alustetaan käyttoliittyma ja käynnistetään se
        Tekstikayttoliittyma kayttoliittyma = new Tekstikayttoliittyma();
        kayttoliittyma.aloita();
    }

}
