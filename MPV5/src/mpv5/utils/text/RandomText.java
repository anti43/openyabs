/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.text;

import java.util.Random;

/**
 * A random text generator
 * @author Andreas
 */
public class RandomText {

    private String source = new String("43Atowelitsaysisaboutthemostmassivelyusefu" +
            "lthinganinterstellarhitchhikercanhavePartlyithasgreatpracticalvalue" +
            "youcanwrapitaroundyouforwarmthasyouboundacrossthecoldmoonsofJaglanBet" +
            "ayoucanlieonitonthebrilliantmarblesandedbeachesofSantraginusVinhalingt" +
            "394060333188397074520172340739371965868498876592311755875219798318317206994017" +
            "heheadyseavapoursyoucansleepunderitbeneaththestarswhichshinesoredlyont" +
            "hedesertworldofKakrafoonuseittosailaminiraftdowntheslowheavyriverMothw" +
            "etitforuseinhandtohandcombatwrapitroundyourheadtowardoffnoxiousfumesora" +
            "voidthegazeoftheRavenousBugblatterBeastofTraalamindbogginglystupidanimali" +
            "eckonedwith308399816614085306859704892269592775111711855313343669299238236306" +
            "tassumesthatifyoucantseeititcantseeyoudaftasabrushbutveryveryravenousyouc" +
            "anwaveyourtowelinemergenciesasadistresssignalandofcoursedryyourselfoffwithit" +
            "ifitstillseemstobecleanenoughMoreimportantlyatowelhasimmensepsychologicalval" +
            "ueForsomereasonifastragstragnonhitchhikerdiscoversthatahitchhikerhashistowel" +
            "withhimhewillautomaticallyassumethatheisalsoinpossessionofatoothbrushfacefl" +
            "386832922784929950232869659529615917138847v64003519483258392515302176217868694" +
            "annelsoaptinofbiscuitsflaskcompassmapballofstringgnatspraywetweathergearspa" +
            "cesuitetcetcFurthermorethestragwillthenhappilylendthehitchhikeranyoftheseor" +
            "adozenotheritemsthatthehitchhikermightaccidentallyhavelostWhatthestragwillt" +
            "hinkisthatanymanwhocanhitchthelengthandbreadthofthegalaxyroughitslumitstrugg" +
            "leagainstterribleoddswinthroughandstillknowswherehistowelisisclearlyamantober" +
            "2324989105969625913643");

    private String string;
    private int length = 5;

    /**
     * Constructs a random text generator 
     * with the given length and source text
     * @param source 
     * @param length
     */
    public RandomText(String source, int length) {
        this.length = length;
        this.source = source;
    }

    /**
     * Constructs a random text generator with the given length
     * @param length
     */
    public RandomText(int length) {
        this.length = length;
    }

    /**
     * Constructs a random text generator with the default length (5)
     */
    public RandomText() {
    }

    /**
     * @return The random text
     */
    public String getString() {
        StringBuffer sb = new StringBuffer();
        Random r = new Random();
        int te = 0;
        for (int i = 1; i <= length; i++) {
            te = r.nextInt(source.length());
            sb.append(source.charAt(te));
        }
        string = sb.toString();
        return string;
    }

    /**
     * Equal to new RandomText(8).getString()
     * @return A random 8- char text
     */
    public static String getText() {
        return new RandomText(8).getString();
    }
}
