/*
 * This file is part of the OpenPDF HTML module.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openpdf.resource;

import org.jspecify.annotations.Nullable;

/**
 * Configuration options for the htmlunit-neko HTML parser.
 * <p>
 * This class provides a builder-style API for configuring the HTML5 parser behavior.
 * The configuration options correspond to features and properties available in the
 * htmlunit-neko parser (<a href="https://github.com/HtmlUnit/htmlunit-neko">htmlunit-neko</a>).
 * <p>
 * Example usage:
 * <pre>
 * HtmlParserConfig config = HtmlParserConfig.builder()
 *     .reportErrors(true)
 *     .allowSelfClosingTags(true)
 *     .elementNameCase("lower")
 *     .encoding("UTF-8")
 *     .build();
 * </pre>
 *
 * @see HtmlResource
 */
public final class HtmlParserConfig {

    /**
     * Element name case values for configuring name handling.
     */
    public static final String CASE_UPPER = "upper";
    public static final String CASE_LOWER = "lower";
    public static final String CASE_DEFAULT = "default";

    private static final HtmlParserConfig DEFAULT_CONFIG = new Builder().build();

    private final boolean reportErrors;
    private final boolean allowSelfClosingTags;
    private final boolean allowSelfClosingIframe;
    private final boolean parseNoScriptContent;
    private final boolean scriptStripCommentDelims;
    private final boolean styleStripCommentDelims;
    @Nullable
    private final String elementNameCase;
    @Nullable
    private final String attributeNameCase;
    @Nullable
    private final String encoding;

    private HtmlParserConfig(Builder builder) {
        this.reportErrors = builder.reportErrors;
        this.allowSelfClosingTags = builder.allowSelfClosingTags;
        this.allowSelfClosingIframe = builder.allowSelfClosingIframe;
        this.parseNoScriptContent = builder.parseNoScriptContent;
        this.scriptStripCommentDelims = builder.scriptStripCommentDelims;
        this.styleStripCommentDelims = builder.styleStripCommentDelims;
        this.elementNameCase = builder.elementNameCase;
        this.attributeNameCase = builder.attributeNameCase;
        this.encoding = builder.encoding;
    }

    /**
     * Returns the default configuration.
     * <p>
     * Default settings:
     * <ul>
     *   <li>reportErrors: false</li>
     *   <li>allowSelfClosingTags: false</li>
     *   <li>allowSelfClosingIframe: false</li>
     *   <li>parseNoScriptContent: true</li>
     *   <li>scriptStripCommentDelims: false</li>
     *   <li>styleStripCommentDelims: false</li>
     *   <li>elementNameCase: null (parser default)</li>
     *   <li>attributeNameCase: null (parser default)</li>
     *   <li>encoding: null (auto-detect)</li>
     * </ul>
     *
     * @return the default configuration
     */
    public static HtmlParserConfig defaults() {
        return DEFAULT_CONFIG;
    }

    /**
     * Creates a new configuration builder.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Whether to report parsing errors.
     * When enabled, the parser will report syntax errors, malformed markup,
     * and other parsing issues.
     *
     * @return true if error reporting is enabled
     */
    public boolean isReportErrors() {
        return reportErrors;
    }

    /**
     * Whether to allow XHTML-style self-closing tags for all elements.
     * When enabled, treats tags like {@code <div/>} as complete elements
     * rather than requiring separate closing tags.
     *
     * @return true if self-closing tags are allowed
     */
    public boolean isAllowSelfClosingTags() {
        return allowSelfClosingTags;
    }

    /**
     * Whether to allow self-closing iframe tags.
     * When enabled, treats {@code <iframe/>} as a complete element.
     *
     * @return true if self-closing iframe tags are allowed
     */
    public boolean isAllowSelfClosingIframe() {
        return allowSelfClosingIframe;
    }

    /**
     * Whether to parse content within {@code <noscript>} tags as HTML markup.
     * When disabled, noscript content is treated as plain text.
     *
     * @return true if noscript content should be parsed as markup
     */
    public boolean isParseNoScriptContent() {
        return parseNoScriptContent;
    }

    /**
     * Whether to strip HTML comment delimiters from script content.
     * Useful for handling legacy JavaScript wrapped in HTML comments.
     *
     * @return true if script comment delimiters should be stripped
     */
    public boolean isScriptStripCommentDelims() {
        return scriptStripCommentDelims;
    }

    /**
     * Whether to strip HTML comment delimiters from style content.
     * Useful for handling CSS wrapped in HTML comments.
     *
     * @return true if style comment delimiters should be stripped
     */
    public boolean isStyleStripCommentDelims() {
        return styleStripCommentDelims;
    }

    /**
     * Get the element name case handling setting.
     * <p>
     * Possible values:
     * <ul>
     *   <li>{@code "upper"} - convert element names to uppercase</li>
     *   <li>{@code "lower"} - convert element names to lowercase</li>
     *   <li>{@code "default"} - preserve original case</li>
     *   <li>{@code null} - use parser default</li>
     * </ul>
     *
     * @return the element name case setting, or null for parser default
     */
    @Nullable
    public String getElementNameCase() {
        return elementNameCase;
    }

    /**
     * Get the attribute name case handling setting.
     * <p>
     * Possible values:
     * <ul>
     *   <li>{@code "upper"} - convert attribute names to uppercase</li>
     *   <li>{@code "lower"} - convert attribute names to lowercase</li>
     *   <li>{@code "default"} - preserve original case</li>
     *   <li>{@code null} - use parser default</li>
     * </ul>
     *
     * @return the attribute name case setting, or null for parser default
     */
    @Nullable
    public String getAttributeNameCase() {
        return attributeNameCase;
    }

    /**
     * Get the default character encoding.
     *
     * @return the encoding name, or null for auto-detection
     */
    @Nullable
    public String getEncoding() {
        return encoding;
    }

    /**
     * Builder for creating HtmlParserConfig instances.
     */
    public static final class Builder {
        private boolean reportErrors = false;
        private boolean allowSelfClosingTags = false;
        private boolean allowSelfClosingIframe = false;
        private boolean parseNoScriptContent = true;
        private boolean scriptStripCommentDelims = false;
        private boolean styleStripCommentDelims = false;
        @Nullable
        private String elementNameCase = null;
        @Nullable
        private String attributeNameCase = null;
        @Nullable
        private String encoding = null;

        private Builder() {
        }

        /**
         * Enable or disable error reporting during parsing.
         *
         * @param reportErrors true to enable error reporting
         * @return this builder
         */
        public Builder reportErrors(boolean reportErrors) {
            this.reportErrors = reportErrors;
            return this;
        }

        /**
         * Enable or disable XHTML-style self-closing tags.
         *
         * @param allowSelfClosingTags true to allow self-closing tags
         * @return this builder
         */
        public Builder allowSelfClosingTags(boolean allowSelfClosingTags) {
            this.allowSelfClosingTags = allowSelfClosingTags;
            return this;
        }

        /**
         * Enable or disable self-closing iframe tags.
         *
         * @param allowSelfClosingIframe true to allow self-closing iframe
         * @return this builder
         */
        public Builder allowSelfClosingIframe(boolean allowSelfClosingIframe) {
            this.allowSelfClosingIframe = allowSelfClosingIframe;
            return this;
        }

        /**
         * Enable or disable parsing of noscript content as markup.
         *
         * @param parseNoScriptContent true to parse noscript as markup
         * @return this builder
         */
        public Builder parseNoScriptContent(boolean parseNoScriptContent) {
            this.parseNoScriptContent = parseNoScriptContent;
            return this;
        }

        /**
         * Enable or disable stripping of comment delimiters from script content.
         *
         * @param scriptStripCommentDelims true to strip comment delimiters
         * @return this builder
         */
        public Builder scriptStripCommentDelims(boolean scriptStripCommentDelims) {
            this.scriptStripCommentDelims = scriptStripCommentDelims;
            return this;
        }

        /**
         * Enable or disable stripping of comment delimiters from style content.
         *
         * @param styleStripCommentDelims true to strip comment delimiters
         * @return this builder
         */
        public Builder styleStripCommentDelims(boolean styleStripCommentDelims) {
            this.styleStripCommentDelims = styleStripCommentDelims;
            return this;
        }

        /**
         * Set the element name case handling.
         *
         * @param elementNameCase one of "upper", "lower", "default", or null
         * @return this builder
         */
        public Builder elementNameCase(@Nullable String elementNameCase) {
            this.elementNameCase = elementNameCase;
            return this;
        }

        /**
         * Set the attribute name case handling.
         *
         * @param attributeNameCase one of "upper", "lower", "default", or null
         * @return this builder
         */
        public Builder attributeNameCase(@Nullable String attributeNameCase) {
            this.attributeNameCase = attributeNameCase;
            return this;
        }

        /**
         * Set the default character encoding.
         *
         * @param encoding the encoding name (e.g., "UTF-8")
         * @return this builder
         */
        public Builder encoding(@Nullable String encoding) {
            this.encoding = encoding;
            return this;
        }

        /**
         * Build the configuration.
         *
         * @return the configured HtmlParserConfig
         */
        public HtmlParserConfig build() {
            return new HtmlParserConfig(this);
        }
    }
}
