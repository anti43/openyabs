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
package mp4.utils.text;

import java.util.Random;

/**
 * A random text generator
 * @author Andreas
 */
public class RandomText {

    private String source = new String("QAa0bcLdUK2eHfJgTP8XhiFj61DOklNm9nBoI5pGqYVrs3" +
            "CtSuMZvwWx4yEkjsdhgfkurewyhflkdsnfukhewlfkrelugflsdnfuiosefkkl" +
            "kndgldslgfmlfdhiglklkjljliurdgouhkjgiuerhg0lndfvgbhorekngfdnvo" +
            "nrejgrhgntroghrejglerhjgoijergkjerg7zRhhkjhsfdjnqkuweyljnlihlf" +
            "loiuwyehflnoqwyld6poqoidandkiuqwhdjhdgiwqdnedgwefdutgddknwyqfi" +
            "uhvljjdgiwejfnuigdifuqazwsxedcrfvtgbyhnujmikkklopq1234567894343");
    private String string;
    private int length = 5;

    /**
     * Constructs a random text generator 
     * with the given length and source text
     */
    public RandomText(String source, int length) {
        this.length = length;
        this.source = source;
    }

    /**
     * Constructs a random text generator with the given length
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
