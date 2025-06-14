/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache of PDF pages and images.
 */
public class Cache {

    /** the pages in the cache, mapped by page number */
    private final Map<Integer, SoftReference<PageRecord>> pages;

    /** Creates a new instance of a Cache */
    public Cache() {
        this.pages = Collections.synchronizedMap(new HashMap<Integer, SoftReference<PageRecord>>());
    }

    /**
     * Add a page to the cache.  This method should be used for
     * pages which have already been completely rendered.  
     * 
     * @param pageNumber the page number of this page
     * @param page the page to add
     */
    public void addPage(Integer pageNumber, PDFPage page) {
        addPageRecord(pageNumber, page, null);
    }

    /**
     * Add a page to the cache.  This method should be used for
     * pages which are still in the process of being rendered.
     *
     * @param pageNumber the page number of this page
     * @param page the page to add
     * @param parser the parser which is parsing this page
     */
    public void addPage(Integer pageNumber, PDFPage page, PDFParser parser) {
        addPageRecord(pageNumber, page, parser);
    }

    /**
     * Add an image to the cache.  This method should be used for images
     * which have already been completely rendered
     *
     * @param page page this image is associated with
     * @param info the image info associated with this image
     * @param image the image to add
     */
    public void addImage(PDFPage page, ImageInfo info, BufferedImage image) {
        addImageRecord(page, info, image, null);
    }

    /**
     * Add an image to the cache.  This method should be used for images
     * which are still in the process of being rendered.
     *
     * @param page the page this image is associated with
     * @param info the image info associated with this image
     * @param image the image to add
     * @param renderer the renderer which is rendering this page
     */
    public void addImage(PDFPage page, ImageInfo info, BufferedImage image,
            PDFRenderer renderer) {
        addImageRecord(page, info, image, renderer);
    }

    /**
     * Get a page from the cache
     * 
     * @param pageNumber the number of the page to get
     * @return the page, if it is in the cache, or null if not
     */
    public PDFPage getPage(Integer pageNumber) {
        PageRecord rec = getPageRecord(pageNumber);
        if (rec != null) {
            return (PDFPage) rec.value;
        }

        // not found
        return null;
    }

    /**
     * Get a page's parser from the cache
     *
     * @param pageNumber the number of the page to get the parser for
     * @return the parser, or null if it is not in the cache
     */
    public PDFParser getPageParser(Integer pageNumber) {
        PageRecord rec = getPageRecord(pageNumber);
        if (rec != null) {
            return (PDFParser) rec.generator;
        }

        // not found
        return null;
    }

    /**
     * Get an image from the cache
     *
     * @param page the page the image is associated with
     * @param info the image info that describes the image
     *
     * @return the image if it is in the cache, or null if not
     */
    public BufferedImage getImage(PDFPage page, ImageInfo info) {
        Record rec = getImageRecord(page, info);
        if (rec != null) {
            return (BufferedImage) rec.value;
        }

        // not found 
        return null;
    }

    /**
     * Get an image's renderer from the cache
     *
     * @param page the page this image was generated from
     * @param info the image info describing the image
     * @return the renderer, or null if it is not in the cache
     */
    public PDFRenderer getImageRenderer(PDFPage page, ImageInfo info) {
        Record rec = getImageRecord(page, info);
        if (rec != null) {
            return (PDFRenderer) rec.generator;
        }

        // not found
        return null;
    }

    /**
     * Remove a page and all its associated images, as well as its parser
     * and renderers, from the cache
     *
     * @param pageNumber the number of the page to remove
     */
    public void removePage(Integer pageNumber) {
        removePageRecord(pageNumber);
    }

    /**
     * Remove an image and its associated renderer from the cache
     *
     * @param page the page the image is generated from
     * @param info the image info of the image to remove
     */
    public void removeImage(PDFPage page, ImageInfo info) {
        removeImageRecord(page, info);
    }

    /**
     * The internal routine to add a page to the cache, and return the
     * page record which was generated
     */
    PageRecord addPageRecord(Integer pageNumber, PDFPage page,
            PDFParser parser) {
        PageRecord rec = new PageRecord();
        rec.value = page;
        rec.generator = parser;

        this.pages.put(pageNumber, new SoftReference<PageRecord>(rec));

        return rec;
    }

    /**
     * Get a page's record from the cache
     *
     * @return the record, or null if it's not in the cache
     */
    PageRecord getPageRecord(Integer pageNumber) {
        PDFDebugger.debug("Request for page " + pageNumber, 1000);
        SoftReference<PageRecord> ref = this.pages.get(pageNumber);
        if (ref != null) {
            String val = (ref.get() == null) ? " not in " : " in ";
            PDFDebugger.debug("Page " + pageNumber + val + "cache", 1000);
            return ref.get();
        }

        PDFDebugger.debug("Page " + pageNumber + " not in cache", 1000);
        // not in cache
        return null;
    }

    /**
     * Remove a page's record from the cache
     */
    PageRecord removePageRecord(Integer pageNumber) {
        SoftReference<PageRecord> ref = this.pages.remove(pageNumber);
        if (ref != null) {
            return ref.get();
        }

        // not in cache
        return null;
    }

    /**
     * The internal routine to add an image to the cache and return the
     * record that was generated.
     */
    Record addImageRecord(PDFPage page, ImageInfo info,
            BufferedImage image, PDFRenderer renderer) {
        // first, find or create the relevant page record
        Integer pageNumber = Integer.valueOf(page.getPageNumber());
        PageRecord pageRec = getPageRecord(pageNumber);
        if (pageRec == null) {
            pageRec = addPageRecord(pageNumber, page, null);
        }

        // next, create the image record
        Record rec = new Record();
        rec.value = image;
        rec.generator = renderer;

        // add it to the cache
        pageRec.images.put(info, new SoftReference<Record>(rec));

        return rec;
    }

    /**
     * Get an image's record from the cache
     *
     * @return the record, or null if it's not in the cache
     */
    Record getImageRecord(PDFPage page, ImageInfo info) {
        // first find the relevant page record
        Integer pageNumber = Integer.valueOf(page.getPageNumber());

        PDFDebugger.debug("Request for image on page " + pageNumber, 1000);

        PageRecord pageRec = getPageRecord(pageNumber);
        if (pageRec != null) {
            SoftReference<Record> ref = pageRec.images.get(info);
            if (ref != null) {
                String val = (ref.get() == null) ? " not in " : " in ";
                PDFDebugger.debug("Image on page " + pageNumber + val + " cache", 1000);
                return ref.get();
            }
        }

        PDFDebugger.debug("Image on page " + pageNumber + " not in cache", 1000);
        // not found
        return null;
    }

    /**
     * Remove an image's record from the cache
     */
    Record removeImageRecord(PDFPage page, ImageInfo info) {
        // first find the relevant page record
        Integer pageNumber = Integer.valueOf(page.getPageNumber());
        PageRecord pageRec = getPageRecord(pageNumber);
        if (pageRec != null) {
            SoftReference<Record> ref = pageRec.images.remove(info);
            if (ref != null) {
                return ref.get();
            }

        }

        return null;
    }

    /** the basic information about a page or image */
    class Record {

        /** the page or image itself */
        Object value;
        /** the thing generating the page, or null if done/not provided */
        BaseWatchable generator;
    }

    /** the record stored for each page in the cache */
    class PageRecord extends Record {

        /** any images associated with the page */
        Map<ImageInfo, SoftReference<Record>> images;

        /** create a new page record */
        public PageRecord() {
            this.images = Collections.synchronizedMap(new HashMap<ImageInfo, SoftReference<Record>>());
        }
    }
}