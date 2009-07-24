/*
 *  This file is part of MP.
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
package enoa.handler;

import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.IParagraph;
import ag.ion.bion.officelayer.text.IParagraphProperties;
import ag.ion.bion.officelayer.text.ITextCursor;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.ITextService;
import ag.ion.bion.officelayer.text.TextException;
import java.awt.Font;

/**
 *
 */
public class TextHandler {

    private final ITextDocument doc;
    private final ITextService textservice;
    private final ITextFieldService textfieldservice;

    public TextHandler(ITextDocument doc) {
        this.doc = doc;
        this.textservice = doc.getTextService();
        this.textfieldservice = doc.getTextFieldService();
    }

    public void append(String text) throws TextException {
        ITextCursor textCursor = getTextservice().getText().getTextCursorService().getTextCursor();
        textCursor.getEnd().setText(text);
    }

    public void appendBefore(String text) throws TextException {
        ITextCursor textCursor = getTextservice().getText().getTextCursorService().getTextCursor();
        textCursor.getStart().setText(text);
    }

    public void setAlign(int paragraph, short align) throws TextException {
        IParagraph[] paragraphs = getDoc().getTextService().getText().getTextContentEnumeration().getParagraphs();
        IParagraphProperties paragraphPropoerties = paragraphs[paragraph].getParagraphProperties();
        paragraphPropoerties.setParaAdjust(align);
    }

    public void setTextFormat(int paragraph, Font font, int color) throws TextException {
        IParagraph[] paragraphs = getTextservice().getText().getTextContentEnumeration().getParagraphs();
        IParagraphProperties paragraphPropoerties = paragraphs[paragraph].getParagraphProperties();
        paragraphPropoerties.getCharacterProperties().setFontBold(font.isBold());
        paragraphPropoerties.getCharacterProperties().setFontSize(font.getSize());
        paragraphPropoerties.getCharacterProperties().setFontItalic(font.isItalic());
        paragraphPropoerties.getCharacterProperties().setFontColor(color);
    }

    public void addParagraphs(String[] text) throws TextException {
        for (int i = 0; i < text.length; i++) {
            ITextCursor textCursor = getTextservice().getText().getTextCursorService().getTextCursor();
            textCursor.gotoEnd(false);
            IParagraph paragraph = getTextservice().getTextContentService().constructNewParagraph();
            textCursor.gotoEnd(false);
            getTextservice().getTextContentService().insertTextContent(textCursor.getEnd(), paragraph);
            paragraph.setParagraphText(text[i]);
        }
    }

    /**
     * @return the doc
     */
    public ITextDocument getDoc() {
        return doc;
    }

    /**
     * @return the textservice
     */
    public ITextService getTextservice() {
        return textservice;
    }

    /**
     * @return the textfieldservice
     */
    public ITextFieldService getTextfieldservice() {
        return textfieldservice;
    }
}
