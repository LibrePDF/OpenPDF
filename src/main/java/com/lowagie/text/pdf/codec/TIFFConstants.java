/*
 * Copyright 2003-2005 by Paulo Soares.
 *
 * This list of constants was originally released with libtiff
 * under the following license:
 *
 * Copyright (c) 1988-1997 Sam Leffler
 * Copyright (c) 1991-1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler and Silicon Graphics may not be used in any advertising or
 * publicity relating to the software without the specific, prior written
 * permission of Sam Leffler and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
 * ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */
package com.lowagie.text.pdf.codec;

/**
 * A list of constants used in class TIFFImage.
 */
public class TIFFConstants {
    
/*
 * TIFF Tag Definitions (from tifflib).
 */
    public static final int TIFFTAG_SUBFILETYPE = 254;	/* subfile data descriptor */
    public static final int     FILETYPE_REDUCEDIMAGE = 0x1;	/* reduced resolution version */
    public static final int     FILETYPE_PAGE = 0x2;	/* one page of many */
    public static final int     FILETYPE_MASK = 0x4;	/* transparency mask */
    public static final int TIFFTAG_OSUBFILETYPE = 255; /* +kind of data in subfile */
    public static final int     OFILETYPE_IMAGE = 1; /* full resolution image data */
    public static final int     OFILETYPE_REDUCEDIMAGE = 2; /* reduced size image data */
    public static final int     OFILETYPE_PAGE = 3; /* one page of many */
    public static final int TIFFTAG_IMAGEWIDTH = 256; /* image width in pixels */
    public static final int TIFFTAG_IMAGELENGTH = 257; /* image height in pixels */
    public static final int TIFFTAG_BITSPERSAMPLE = 258; /* bits per channel (sample) */
    public static final int TIFFTAG_COMPRESSION = 259; /* data compression technique */
    public static final int     COMPRESSION_NONE = 1; /* dump mode */
    public static final int     COMPRESSION_CCITTRLE = 2; /* CCITT modified Huffman RLE */
    public static final int     COMPRESSION_CCITTFAX3 = 3;	/* CCITT Group 3 fax encoding */
    public static final int     COMPRESSION_CCITTFAX4 = 4;	/* CCITT Group 4 fax encoding */
    public static final int     COMPRESSION_LZW = 5;       /* Lempel-Ziv  & Welch */
    public static final int     COMPRESSION_OJPEG = 6; /* !6.0 JPEG */
    public static final int     COMPRESSION_JPEG = 7; /* %JPEG DCT compression */
    public static final int     COMPRESSION_NEXT = 32766; /* NeXT 2-bit RLE */
    public static final int     COMPRESSION_CCITTRLEW = 32771; /* #1 w/ word alignment */
    public static final int     COMPRESSION_PACKBITS = 32773; /* Macintosh RLE */
    public static final int     COMPRESSION_THUNDERSCAN = 32809; /* ThunderScan RLE */
    /* codes 32895-32898 are reserved for ANSI IT8 TIFF/IT <dkelly@etsinc.com) */
    public static final int     COMPRESSION_IT8CTPAD = 32895;   /* IT8 CT w/padding */
    public static final int     COMPRESSION_IT8LW = 32896;   /* IT8 Linework RLE */
    public static final int     COMPRESSION_IT8MP = 32897;   /* IT8 Monochrome picture */
    public static final int     COMPRESSION_IT8BL = 32898;   /* IT8 Binary line art */
    /* compression codes 32908-32911 are reserved for Pixar */
    public static final int     COMPRESSION_PIXARFILM = 32908;   /* Pixar companded 10bit LZW */
    public static final int     COMPRESSION_PIXARLOG = 32909;   /* Pixar companded 11bit ZIP */
    public static final int     COMPRESSION_DEFLATE = 32946; /* Deflate compression */
    public static final int     COMPRESSION_ADOBE_DEFLATE = 8;       /* Deflate compression, as recognized by Adobe */
    /* compression code 32947 is reserved for Oceana Matrix <dev@oceana.com> */
    public static final int     COMPRESSION_DCS = 32947;   /* Kodak DCS encoding */
    public static final int     COMPRESSION_JBIG = 34661; /* ISO JBIG */
    public static final int     COMPRESSION_SGILOG = 34676; /* SGI Log Luminance RLE */
    public static final int     COMPRESSION_SGILOG24 = 34677;	/* SGI Log 24-bit packed */
    public static final int TIFFTAG_PHOTOMETRIC = 262; /* photometric interpretation */
    public static final int     PHOTOMETRIC_MINISWHITE = 0; /* min value is white */
    public static final int     PHOTOMETRIC_MINISBLACK = 1; /* min value is black */
    public static final int     PHOTOMETRIC_RGB = 2; /* RGB color model */
    public static final int     PHOTOMETRIC_PALETTE = 3; /* color map indexed */
    public static final int     PHOTOMETRIC_MASK = 4; /* $holdout mask */
    public static final int     PHOTOMETRIC_SEPARATED = 5; /* !color separations */
    public static final int     PHOTOMETRIC_YCBCR = 6; /* !CCIR 601 */
    public static final int     PHOTOMETRIC_CIELAB = 8; /* !1976 CIE L*a*b* */
    public static final int     PHOTOMETRIC_LOGL = 32844; /* CIE Log2(L) */
    public static final int     PHOTOMETRIC_LOGLUV = 32845; /* CIE Log2(L) (u',v') */
    public static final int TIFFTAG_THRESHHOLDING = 263; /* +thresholding used on data */
    public static final int     THRESHHOLD_BILEVEL = 1; /* b&w art scan */
    public static final int     THRESHHOLD_HALFTONE = 2; /* or dithered scan */
    public static final int     THRESHHOLD_ERRORDIFFUSE = 3; /* usually floyd-steinberg */
    public static final int TIFFTAG_CELLWIDTH = 264; /* +dithering matrix width */
    public static final int TIFFTAG_CELLLENGTH = 265; /* +dithering matrix height */
    public static final int TIFFTAG_FILLORDER = 266; /* data order within a byte */
    public static final int     FILLORDER_MSB2LSB = 1; /* most significant -> least */
    public static final int     FILLORDER_LSB2MSB = 2; /* least significant -> most */
    public static final int TIFFTAG_DOCUMENTNAME = 269; /* name of doc. image is from */
    public static final int TIFFTAG_IMAGEDESCRIPTION = 270; /* info about image */
    public static final int TIFFTAG_MAKE = 271; /* scanner manufacturer name */
    public static final int TIFFTAG_MODEL = 272; /* scanner model name/number */
    public static final int TIFFTAG_STRIPOFFSETS = 273; /* offsets to data strips */
    public static final int TIFFTAG_ORIENTATION = 274; /* +image orientation */
    public static final int     ORIENTATION_TOPLEFT = 1; /* row 0 top, col 0 lhs */
    public static final int     ORIENTATION_TOPRIGHT = 2; /* row 0 top, col 0 rhs */
    public static final int     ORIENTATION_BOTRIGHT = 3; /* row 0 bottom, col 0 rhs */
    public static final int     ORIENTATION_BOTLEFT = 4; /* row 0 bottom, col 0 lhs */
    public static final int     ORIENTATION_LEFTTOP = 5; /* row 0 lhs, col 0 top */
    public static final int     ORIENTATION_RIGHTTOP = 6; /* row 0 rhs, col 0 top */
    public static final int     ORIENTATION_RIGHTBOT = 7; /* row 0 rhs, col 0 bottom */
    public static final int     ORIENTATION_LEFTBOT = 8; /* row 0 lhs, col 0 bottom */
    public static final int TIFFTAG_SAMPLESPERPIXEL = 277; /* samples per pixel */
    public static final int TIFFTAG_ROWSPERSTRIP = 278; /* rows per strip of data */
    public static final int TIFFTAG_STRIPBYTECOUNTS = 279; /* bytes counts for strips */
    public static final int TIFFTAG_MINSAMPLEVALUE = 280; /* +minimum sample value */
    public static final int TIFFTAG_MAXSAMPLEVALUE = 281; /* +maximum sample value */
    public static final int TIFFTAG_XRESOLUTION = 282; /* pixels/resolution in x */
    public static final int TIFFTAG_YRESOLUTION = 283; /* pixels/resolution in y */
    public static final int TIFFTAG_PLANARCONFIG = 284; /* storage organization */
    public static final int     PLANARCONFIG_CONTIG = 1; /* single image plane */
    public static final int     PLANARCONFIG_SEPARATE = 2; /* separate planes of data */
    public static final int TIFFTAG_PAGENAME = 285; /* page name image is from */
    public static final int TIFFTAG_XPOSITION = 286; /* x page offset of image lhs */
    public static final int TIFFTAG_YPOSITION = 287; /* y page offset of image lhs */
    public static final int TIFFTAG_FREEOFFSETS = 288; /* +byte offset to free block */
    public static final int TIFFTAG_FREEBYTECOUNTS = 289; /* +sizes of free blocks */
    public static final int TIFFTAG_GRAYRESPONSEUNIT = 290; /* $gray scale curve accuracy */
    public static final int     GRAYRESPONSEUNIT_10S = 1; /* tenths of a unit */
    public static final int     GRAYRESPONSEUNIT_100S = 2; /* hundredths of a unit */
    public static final int     GRAYRESPONSEUNIT_1000S = 3; /* thousandths of a unit */
    public static final int     GRAYRESPONSEUNIT_10000S = 4; /* ten-thousandths of a unit */
    public static final int     GRAYRESPONSEUNIT_100000S = 5; /* hundred-thousandths */
    public static final int TIFFTAG_GRAYRESPONSECURVE = 291; /* $gray scale response curve */
    public static final int TIFFTAG_GROUP3OPTIONS = 292; /* 32 flag bits */
    public static final int     GROUP3OPT_2DENCODING = 0x1;	/* 2-dimensional coding */
    public static final int     GROUP3OPT_UNCOMPRESSED = 0x2;	/* data not compressed */
    public static final int     GROUP3OPT_FILLBITS = 0x4;	/* fill to byte boundary */
    public static final int TIFFTAG_GROUP4OPTIONS = 293; /* 32 flag bits */
    public static final int     GROUP4OPT_UNCOMPRESSED = 0x2;	/* data not compressed */
    public static final int TIFFTAG_RESOLUTIONUNIT = 296; /* units of resolutions */
    public static final int     RESUNIT_NONE = 1; /* no meaningful units */
    public static final int     RESUNIT_INCH = 2; /* english */
    public static final int     RESUNIT_CENTIMETER = 3;	/* metric */
    public static final int TIFFTAG_PAGENUMBER = 297;	/* page numbers of multi-page */
    public static final int TIFFTAG_COLORRESPONSEUNIT = 300;	/* $color curve accuracy */
    public static final int     COLORRESPONSEUNIT_10S = 1;	/* tenths of a unit */
    public static final int     COLORRESPONSEUNIT_100S = 2;	/* hundredths of a unit */
    public static final int     COLORRESPONSEUNIT_1000S = 3;	/* thousandths of a unit */
    public static final int     COLORRESPONSEUNIT_10000S = 4;	/* ten-thousandths of a unit */
    public static final int     COLORRESPONSEUNIT_100000S = 5;	/* hundred-thousandths */
    public static final int TIFFTAG_TRANSFERFUNCTION = 301;	/* !colorimetry info */
    public static final int TIFFTAG_SOFTWARE = 305;	/* name & release */
    public static final int TIFFTAG_DATETIME = 306;	/* creation date and time */
    public static final int TIFFTAG_ARTIST = 315;	/* creator of image */
    public static final int TIFFTAG_HOSTCOMPUTER = 316;	/* machine where created */
    public static final int TIFFTAG_PREDICTOR = 317;	/* prediction scheme w/ LZW */
    public static final int TIFFTAG_WHITEPOINT = 318;	/* image white point */
    public static final int TIFFTAG_PRIMARYCHROMATICITIES = 319;	/* !primary chromaticities */
    public static final int TIFFTAG_COLORMAP = 320;	/* RGB map for pallette image */
    public static final int TIFFTAG_HALFTONEHINTS = 321;	/* !highlight+shadow info */
    public static final int TIFFTAG_TILEWIDTH = 322;	/* !rows/data tile */
    public static final int TIFFTAG_TILELENGTH = 323;	/* !cols/data tile */
    public static final int TIFFTAG_TILEOFFSETS = 324;	/* !offsets to data tiles */
    public static final int TIFFTAG_TILEBYTECOUNTS = 325;	/* !byte counts for tiles */
    public static final int TIFFTAG_BADFAXLINES = 326;	/* lines w/ wrong pixel count */
    public static final int TIFFTAG_CLEANFAXDATA = 327;	/* regenerated line info */
    public static final int     CLEANFAXDATA_CLEAN = 0;	/* no errors detected */
    public static final int     CLEANFAXDATA_REGENERATED = 1;	/* receiver regenerated lines */
    public static final int     CLEANFAXDATA_UNCLEAN = 2;	/* uncorrected errors exist */
    public static final int TIFFTAG_CONSECUTIVEBADFAXLINES = 328;	/* max consecutive bad lines */
    public static final int TIFFTAG_SUBIFD = 330;	/* subimage descriptors */
    public static final int TIFFTAG_INKSET = 332;	/* !inks in separated image */
    public static final int     INKSET_CMYK = 1;	/* !cyan-magenta-yellow-black */
    public static final int TIFFTAG_INKNAMES = 333;	/* !ascii names of inks */
    public static final int TIFFTAG_NUMBEROFINKS = 334;	/* !number of inks */
    public static final int TIFFTAG_DOTRANGE = 336;	/* !0% and 100% dot codes */
    public static final int TIFFTAG_TARGETPRINTER = 337;	/* !separation target */
    public static final int TIFFTAG_EXTRASAMPLES = 338;	/* !info about extra samples */
    public static final int     EXTRASAMPLE_UNSPECIFIED = 0;	/* !unspecified data */
    public static final int     EXTRASAMPLE_ASSOCALPHA = 1;	/* !associated alpha data */
    public static final int     EXTRASAMPLE_UNASSALPHA = 2;	/* !unassociated alpha data */
    public static final int TIFFTAG_SAMPLEFORMAT = 339;	/* !data sample format */
    public static final int     SAMPLEFORMAT_UINT = 1;	/* !unsigned integer data */
    public static final int     SAMPLEFORMAT_INT = 2;	/* !signed integer data */
    public static final int     SAMPLEFORMAT_IEEEFP = 3;	/* !IEEE floating point data */
    public static final int     SAMPLEFORMAT_VOID = 4;	/* !untyped data */
    public static final int     SAMPLEFORMAT_COMPLEXINT = 5;	/* !complex signed int */
    public static final int     SAMPLEFORMAT_COMPLEXIEEEFP = 6;	/* !complex ieee floating */
    public static final int TIFFTAG_SMINSAMPLEVALUE = 340;	/* !variable MinSampleValue */
    public static final int TIFFTAG_SMAXSAMPLEVALUE = 341;	/* !variable MaxSampleValue */
    public static final int TIFFTAG_JPEGTABLES = 347;	/* %JPEG table stream */
/*
 * Tags 512-521 are obsoleted by Technical Note #2
 * which specifies a revised JPEG-in-TIFF scheme.
 */
    public static final int TIFFTAG_JPEGPROC = 512;	/* !JPEG processing algorithm */
    public static final int     JPEGPROC_BASELINE = 1;	/* !baseline sequential */
    public static final int     JPEGPROC_LOSSLESS = 14;	/* !Huffman coded lossless */
    public static final int TIFFTAG_JPEGIFOFFSET = 513;	/* !pointer to SOI marker */
    public static final int TIFFTAG_JPEGIFBYTECOUNT = 514;	/* !JFIF stream length */
    public static final int TIFFTAG_JPEGRESTARTINTERVAL = 515;	/* !restart interval length */
    public static final int TIFFTAG_JPEGLOSSLESSPREDICTORS = 517;	/* !lossless proc predictor */
    public static final int TIFFTAG_JPEGPOINTTRANSFORM = 518;	/* !lossless point transform */
    public static final int TIFFTAG_JPEGQTABLES = 519;	/* !Q matrice offsets */
    public static final int TIFFTAG_JPEGDCTABLES = 520;	/* !DCT table offsets */
    public static final int TIFFTAG_JPEGACTABLES = 521;	/* !AC coefficient offsets */
    public static final int TIFFTAG_YCBCRCOEFFICIENTS = 529;	/* !RGB -> YCbCr transform */
    public static final int TIFFTAG_YCBCRSUBSAMPLING = 530;	/* !YCbCr subsampling factors */
    public static final int TIFFTAG_YCBCRPOSITIONING = 531;	/* !subsample positioning */
    public static final int     YCBCRPOSITION_CENTERED = 1;	/* !as in PostScript Level 2 */
    public static final int     YCBCRPOSITION_COSITED = 2;	/* !as in CCIR 601-1 */
    public static final int TIFFTAG_REFERENCEBLACKWHITE = 532;	/* !colorimetry info */
    /* tags 32952-32956 are private tags registered to Island Graphics */
    public static final int TIFFTAG_REFPTS = 32953;	/* image reference points */
    public static final int TIFFTAG_REGIONTACKPOINT = 32954;	/* region-xform tack point */
    public static final int TIFFTAG_REGIONWARPCORNERS = 32955;	/* warp quadrilateral */
    public static final int TIFFTAG_REGIONAFFINE = 32956;	/* affine transformation mat */
    /* tags 32995-32999 are private tags registered to SGI */
    public static final int TIFFTAG_MATTEING = 32995;	/* $use ExtraSamples */
    public static final int TIFFTAG_DATATYPE = 32996;	/* $use SampleFormat */
    public static final int TIFFTAG_IMAGEDEPTH = 32997;	/* z depth of image */
    public static final int TIFFTAG_TILEDEPTH = 32998;	/* z depth/data tile */
    /* tags 33300-33309 are private tags registered to Pixar */
/*
 * TIFFTAG_PIXAR_IMAGEFULLWIDTH and TIFFTAG_PIXAR_IMAGEFULLLENGTH
 * are set when an image has been cropped out of a larger image.
 * They reflect the size of the original uncropped image.
 * The TIFFTAG_XPOSITION and TIFFTAG_YPOSITION can be used
 * to determine the position of the smaller image in the larger one.
 */
    public static final int TIFFTAG_PIXAR_IMAGEFULLWIDTH = 33300;   /* full image size in x */
    public static final int TIFFTAG_PIXAR_IMAGEFULLLENGTH = 33301;   /* full image size in y */
 /* Tags 33302-33306 are used to identify special image modes and data
  * used by Pixar's texture formats.
  */
    public static final int TIFFTAG_PIXAR_TEXTUREFORMAT = 33302;	/* texture map format */
    public static final int TIFFTAG_PIXAR_WRAPMODES = 33303;	/* s & t wrap modes */
    public static final int TIFFTAG_PIXAR_FOVCOT = 33304;	/* cotan(fov) for env. maps */
    public static final int TIFFTAG_PIXAR_MATRIX_WORLDTOSCREEN = 33305;
    public static final int TIFFTAG_PIXAR_MATRIX_WORLDTOCAMERA = 33306;
    /* tag 33405 is a private tag registered to Eastman Kodak */
    public static final int TIFFTAG_WRITERSERIALNUMBER = 33405;   /* device serial number */
    /* tag 33432 is listed in the 6.0 spec w/ unknown ownership */
    public static final int TIFFTAG_COPYRIGHT = 33432;	/* copyright string */
    /* IPTC TAG from RichTIFF specifications */
    public static final int TIFFTAG_RICHTIFFIPTC = 33723;
    /* 34016-34029 are reserved for ANSI IT8 TIFF/IT <dkelly@etsinc.com) */
    public static final int TIFFTAG_IT8SITE = 34016;	/* site name */
    public static final int TIFFTAG_IT8COLORSEQUENCE = 34017;	/* color seq. [RGB,CMYK,etc] */
    public static final int TIFFTAG_IT8HEADER = 34018;	/* DDES Header */
    public static final int TIFFTAG_IT8RASTERPADDING = 34019;	/* raster scanline padding */
    public static final int TIFFTAG_IT8BITSPERRUNLENGTH = 34020;	/* # of bits in short run */
    public static final int TIFFTAG_IT8BITSPEREXTENDEDRUNLENGTH = 34021;/* # of bits in long run */
    public static final int TIFFTAG_IT8COLORTABLE = 34022;	/* LW colortable */
    public static final int TIFFTAG_IT8IMAGECOLORINDICATOR = 34023;	/* BP/BL image color switch */
    public static final int TIFFTAG_IT8BKGCOLORINDICATOR = 34024;	/* BP/BL bg color switch */
    public static final int TIFFTAG_IT8IMAGECOLORVALUE = 34025;	/* BP/BL image color value */
    public static final int TIFFTAG_IT8BKGCOLORVALUE = 34026;	/* BP/BL bg color value */
    public static final int TIFFTAG_IT8PIXELINTENSITYRANGE = 34027;	/* MP pixel intensity value */
    public static final int TIFFTAG_IT8TRANSPARENCYINDICATOR = 34028;	/* HC transparency switch */
    public static final int TIFFTAG_IT8COLORCHARACTERIZATION = 34029;	/* color character. table */
    /* tags 34232-34236 are private tags registered to Texas Instruments */
    public static final int TIFFTAG_FRAMECOUNT = 34232;   /* Sequence Frame Count */
    /* tag 34750 is a private tag registered to Adobe? */
    public static final int TIFFTAG_ICCPROFILE = 34675;	/* ICC profile data */
    /* tag 34377 is private tag registered to Adobe for PhotoShop */
    public static final int TIFFTAG_PHOTOSHOP = 34377;
    /* tag 34750 is a private tag registered to Pixel Magic */
    public static final int TIFFTAG_JBIGOPTIONS = 34750;	/* JBIG options */
    /* tags 34908-34914 are private tags registered to SGI */
    public static final int TIFFTAG_FAXRECVPARAMS = 34908;	/* encoded Class 2 ses. parms */
    public static final int TIFFTAG_FAXSUBADDRESS = 34909;	/* received SubAddr string */
    public static final int TIFFTAG_FAXRECVTIME = 34910;	/* receive time (secs) */
    /* tags 37439-37443 are registered to SGI <gregl@sgi.com> */
    public static final int TIFFTAG_STONITS = 37439;	/* Sample value to Nits */
    /* tag 34929 is a private tag registered to FedEx */
    public static final int TIFFTAG_FEDEX_EDR = 34929;	/* unknown use */
    /* tag 65535 is an undefined tag used by Eastman Kodak */
    public static final int TIFFTAG_DCSHUESHIFTVALUES = 65535;   /* hue shift correction data */
    
}
