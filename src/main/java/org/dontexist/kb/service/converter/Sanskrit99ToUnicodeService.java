package org.dontexist.kb.service.converter;

public interface Sanskrit99ToUnicodeService extends Text2UnicodeService {

    /**
     * Converts Sanskrit99 input to Unicode output.
     * <p>
     * 
     * @param input
     * @return Unicode output <b>with characters escaped for HTML4<b>!!
     * @throws IllegalStateException
     *             if error occurs while attempting to read/call Javascript file
     *             that is used for conversion
     */
    String convert(String input);

    String convertHtmlBlock(String input);

    String performPreConvertReplacements(String input);
}