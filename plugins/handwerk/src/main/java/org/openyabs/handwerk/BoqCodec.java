/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.handwerk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pluggable Bill-of-Quantities (BoQ) codec interface. Each European format
 * (GAEB DA XML, GAEB 90, CSV, and future ÖNORM A 2063 / SIA 451 / FIEBDC-3)
 * implements this contract without modifying UI or persistence layers.
 */
public interface BoqCodec {

    /**
     * @return stable format family identifier (e.g. {@code "GAEB_XML"}, {@code "GAEB_90"}, {@code "CSV"})
     */
    String id();

    /**
     * @param fileName file name including extension
     * @param content raw text/XML content
     * @return {@code true} if this codec can read the given file
     */
    boolean canRead(String fileName, String content);

    /**
     * Parses a tender/BoQ file into the format-neutral {@link BoqDocument}.
     *
     * @param fileName original file name
     * @param content file contents
     * @return parsed BoQ document
     * @throws Exception if parsing fails
     */
    BoqDocument read(String fileName, String content) throws Exception;

    /**
     * @param formatCode stored {@link BoqDocument#formatCode}
     * @return {@code true} if this codec can export a priced bid for the stored document
     */
    boolean canExportBid(String formatCode);

    /**
     * @return default file extension for exported bids (without dot, e.g. {@code "X84"}, {@code "D84"}, {@code "csv"})
     */
    String bidFileExtension();

    /**
     * Exports a priced bid document from an imported {@link BoqDocument} and
     * updated unit prices / bidder texts keyed by ordinal number ({@code ordinalNo}).
     *
     * @param document stored BoQ document (including original {@code rawSource} when available)
     * @param unitPricesByOrdinal updated unit prices per ordinal number
     * @param bidderTextByOrdinal optional bidder text additions per ordinal number
     * @param producerVersion OpenYaBS / plugin version string for metadata regeneration
     * @return serialized bid file bytes
     * @throws Exception if export fails
     */
    byte[] exportBid(
            BoqDocument document,
            Map<String, java.math.BigDecimal> unitPricesByOrdinal,
            Map<String, String> bidderTextByOrdinal,
            String producerVersion) throws Exception;

    /**
     * Default ordered registry of built-in BoQ codecs.
     */
    static List<BoqCodec> defaultCodecs() {
        return Collections.unmodifiableList(Arrays.<BoqCodec>asList(
                new GaebXmlCodec(),
                new Gaeb90Codec(),
                new CsvBoqCodec()));
    }

    static BoqCodec findReader(String fileName, String content) {
        for (BoqCodec codec : defaultCodecs()) {
            if (codec.canRead(fileName, content)) {
                return codec;
            }
        }
        return null;
    }

    static BoqCodec findExporter(String formatCode) {
        for (BoqCodec codec : defaultCodecs()) {
            if (codec.canExportBid(formatCode)) {
                return codec;
            }
        }
        return null;
    }
}
