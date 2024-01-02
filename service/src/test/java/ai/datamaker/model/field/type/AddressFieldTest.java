/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.model.field.type;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.model.field.type.AddressField;
import com.google.common.collect.Sets;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AddressFieldTest {

    @Test
    void generateData_full_canada() {
        AddressField field = new AddressField("full-address-ca", Locale.forLanguageTag("en-CA"));
        String address = field.generateData();
        assertNotNull(address);
    }

    @Test
    void generateData_full_us() {
        AddressField field = new AddressField("full-address-us", Locale.forLanguageTag("en-US"));
        String address = field.generateData();
        assertNotNull(address);
    }

    @Test
    void generateData_zipcode_us() {
        AddressField field = new AddressField("full-address-us", Locale.forLanguageTag("en-US"));
        field.getConfig().put(AddressField.ADDRESS_TYPE_PROPERTY.getKey(), AddressField.AddressType.ZIP_CODE.toString());
        assertTrue(field.generateData().matches("[0-9]{5}(?:-[0-9]{4})?"));
    }

    @Test
    void generateData_zipcode_canada() {
        AddressField field = new AddressField("full-address-ca", Locale.forLanguageTag("en-CA"));
        field.getConfig().put(AddressField.ADDRESS_TYPE_PROPERTY.getKey(), AddressField.AddressType.ZIP_CODE.toString());
        assertTrue(field.generateData().matches("[A-CEJ-NPR-TVXY][0-9][A-CEJ-NPR-TV-Z] ?[0-9][A-CEJ-NPR-TV-Z][0-9]"));
    }

    @Test
    void generateData_country() {
        Set<String> countries = Sets.newHashSet("Andorre", "Émirats arabes unis", "Afghanistan", "Antigua-et-Barbuda", "Anguilla", "Albanie", "Arménie", "Angola", "Antarctique", "Argentine", "Samoa américaines", "Autriche", "Australie", "Aruba", "Îles Åland", "Azerbaïdjan", "Bosnie-Herzégovine", "Barbade", "Bangladesh", "Belgique", "Burkina Faso", "Bulgarie", "Bahreïn", "Burundi", "Bénin", "Saint-Barthélemy", "Bermudes", "Brunéi Darussalam", "Bolivie", "Pays-Bas caribéens", "Brésil", "Bahamas", "Bhoutan", "Île Bouvet", "Botswana", "Biélorussie", "Belize", "Canada", "Îles Cocos", "Congo-Kinshasa", "République centrafricaine", "Congo-Brazzaville", "Suisse", "Côte d’Ivoire", "Îles Cook", "Chili", "Cameroun", "Chine", "Colombie", "Costa Rica", "Cuba", "Cap-Vert", "Curaçao", "Île Christmas", "Chypre", "Tchéquie", "Allemagne", "Djibouti", "Danemark", "Dominique", "République dominicaine", "Algérie", "Équateur", "Estonie", "Égypte", "Sahara occidental", "Érythrée", "Espagne", "Éthiopie", "Finlande", "Fidji", "Îles Malouines", "États fédérés de Micronésie", "Îles Féroé", "France", "Gabon", "Royaume-Uni", "Grenade", "Géorgie", "Guyane française", "Guernesey", "Ghana", "Gibraltar", "Groenland", "Gambie", "Guinée", "Guadeloupe", "Guinée équatoriale", "Grèce", "Géorgie du Sud et îles Sandwich du Sud", "Guatemala", "Guam", "Guinée-Bissau", "Guyana", "R.A.S. chinoise de Hong Kong", "Îles Heard et McDonald", "Honduras", "Croatie", "Haïti", "Hongrie", "Indonésie", "Irlande", "Israël", "Île de Man", "Inde", "Territoire britannique de l’océan Indien", "Irak", "Iran", "Islande", "Italie", "Jersey", "Jamaïque", "Jordanie", "Japon", "Kenya", "Kirghizistan", "Cambodge", "Kiribati", "Comores", "Saint-Christophe-et-Niévès", "Corée du Nord", "Corée du Sud", "Koweït", "Îles Caïmans", "Kazakhstan", "Laos", "Liban", "Sainte-Lucie", "Liechtenstein", "Sri Lanka", "Libéria", "Lesotho", "Lituanie", "Luxembourg", "Lettonie", "Libye", "Maroc", "Monaco", "Moldavie", "Monténégro", "Saint-Martin", "Madagascar", "Îles Marshall", "Macédoine", "Mali", "Myanmar (Birmanie)", "Mongolie", "R.A.S. chinoise de Macao", "Îles Mariannes du Nord", "Martinique", "Mauritanie", "Montserrat", "Malte", "Maurice", "Maldives", "Malawi", "Mexique", "Malaisie", "Mozambique", "Namibie", "Nouvelle-Calédonie", "Niger", "Île Norfolk", "Nigéria", "Nicaragua", "Pays-Bas", "Norvège", "Népal", "Nauru", "Niue", "Nouvelle-Zélande", "Oman", "Panama", "Pérou", "Polynésie française", "Papouasie-Nouvelle-Guinée", "Philippines", "Pakistan", "Pologne", "Saint-Pierre-et-Miquelon", "Îles Pitcairn", "Porto Rico", "Territoires palestiniens", "Portugal", "Palaos", "Paraguay", "Qatar", "La Réunion", "Roumanie", "Serbie", "Russie", "Rwanda", "Arabie saoudite", "Îles Salomon", "Seychelles", "Soudan", "Suède", "Singapour", "Sainte-Hélène", "Slovénie", "Svalbard et Jan Mayen", "Slovaquie", "Sierra Leone", "Saint-Marin", "Sénégal", "Somalie", "Suriname", "Soudan du Sud", "Sao Tomé-et-Principe", "Salvador", "Saint-Martin (partie néerlandaise)", "Syrie", "Swaziland", "Îles Turques-et-Caïques", "Tchad", "Terres australes françaises", "Togo", "Thaïlande", "Tadjikistan", "Tokélaou", "Timor oriental", "Turkménistan", "Tunisie", "Tonga", "Turquie", "Trinité-et-Tobago", "Tuvalu", "Taïwan", "Tanzanie", "Ukraine", "Ouganda", "Îles mineures éloignées des États-Unis", "États-Unis", "Uruguay", "Ouzbékistan", "État de la Cité du Vatican", "Saint-Vincent-et-les-Grenadines", "Venezuela", "Îles Vierges britanniques", "Îles Vierges des États-Unis", "Vietnam", "Vanuatu", "Wallis-et-Futuna", "Samoa", "Yémen", "Mayotte", "Afrique du Sud", "Zambie", "Zimbabwe");
        AddressField field = new AddressField("country", Locale.forLanguageTag("fr-CA"));
        field.getConfig().put(AddressField.ADDRESS_TYPE_PROPERTY.getKey(), AddressField.AddressType.COUNTRY.toString());
        String country = field.generateData();
        assertTrue(countries.contains(country));
    }

    @Test
    void generateData_city_france() {
        Set<String> cities = Sets.newHashSet("Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier", "Bordeaux", "Lille", "Rennes", "Reims", "Le Havre", "Saint-Étienne", "Toulon", "Grenoble", "Dijon", "Angers", "Saint-Denis", "Villeurbanne", "Le Mans", "Aix-en-Provence", "Brest", "Nîmes", "Limoges", "Clermont-Ferrand", "Tours", "Amiens", "Metz", "Perpignan", "Besançon", "Orléans", "Boulogne-Billancourt", "Mulhouse", "Rouen", "Caen", "Nancy", "Saint-Denis", "Saint-Paul", "Montreuil", "Argenteuil", "Roubaix", "Dunkerque14", "Tourcoing", "Nanterre", "Avignon", "Créteil", "Poitiers", "Fort-de-France", "Courbevoie", "Versailles", "Vitry-sur-Seine", "Colombes", "Pau", "Aulnay-sous-Bois", "Asnières-sur-Seine", "Rueil-Malmaison", "Saint-Pierre", "Antibes", "Saint-Maur-des-Fossés", "Champigny-sur-Marne", "La Rochelle", "Aubervilliers", "Calais", "Cannes", "Le Tampon", "Béziers", "Colmar", "Bourges", "Drancy", "Mérignac", "Saint-Nazaire", "Valence", "Ajaccio", "Issy-les-Moulineaux", "Villeneuve-d'Ascq", "Levallois-Perret", "Noisy-le-Grand", "Quimper", "La Seyne-sur-Mer", "Antony", "Troyes", "Neuilly-sur-Seine", "Sarcelles", "Les Abymes", "Vénissieux", "Clichy", "Lorient", "Pessac", "Ivry-sur-Seine", "Cergy", "Cayenne", "Niort", "Chambéry", "Montauban", "Saint-Quentin", "Villejuif", "Hyères", "Beauvais", "Cholet");
        AddressField field = new AddressField("city-france", Locale.forLanguageTag("fr"));
        field.getConfig().put(AddressField.ADDRESS_TYPE_PROPERTY.getKey(), AddressField.AddressType.CITY.toString());
        assertTrue(cities.contains(field.generateData()));
    }

}