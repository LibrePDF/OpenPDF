package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TextUtil {
    @Nullable
    @CheckReturnValue
    public static String readTextContentOrNull(Element element) {
        String text = readTextContent(element);
        return text.isEmpty() ? null : text;
    }

    @CheckReturnValue
    public static String readTextContent(Element element) {
        StringBuilder result = new StringBuilder();
        Node current = element.getFirstChild();
        while (current != null) {
            short nodeType = current.getNodeType();
            if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                Text t = (Text) current;
                result.append(t.getData());
            }
            current = current.getNextSibling();
        }
        return result.toString();
    }
}
