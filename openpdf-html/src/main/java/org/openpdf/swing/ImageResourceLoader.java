package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.extend.FSImage;
import org.openpdf.resource.ImageResource;
import org.openpdf.util.Configuration;
import org.openpdf.util.IOUtil;
import org.openpdf.util.ImageUtil;
import org.openpdf.util.XRLog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.openpdf.util.ImageUtil.isEmbeddedBase64Image;

public class ImageResourceLoader {
    public static final RepaintListener NO_OP_REPAINT_LISTENER = doLayout -> XRLog.general(Level.FINE, "No-op repaint requested");
    private final Map<CacheKey, ImageResource> _imageCache;

    @Nullable
    private final ImageLoadQueue _loadQueue;
    private final int _imageCacheCapacity;
    private final RepaintListener _repaintListener;
    private final boolean _useBackgroundImageLoading;

    public ImageResourceLoader() {
        this(16, NO_OP_REPAINT_LISTENER);
    }

    public ImageResourceLoader(RepaintListener repaintListener) {
        this(16, repaintListener);
    }

    public ImageResourceLoader(int cacheSize, RepaintListener repaintListener) {
        this._imageCacheCapacity = cacheSize;
        this._useBackgroundImageLoading = Configuration.isTrue("xr.image.background.loading.enable", false);

        if (_useBackgroundImageLoading) {
            this._loadQueue = new ImageLoadQueue();
            final int workerCount = Configuration.valueAsInt("xr.image.background.workers", 5);
            for (int i = 0; i < workerCount; i++) {
                new ImageLoadWorker(_loadQueue).start();
            }
        } else {
            this._loadQueue = null;
        }

        // note we do *not* override removeEldestEntry() here--users of this class must call shrinkImageCache().
        // that's because we don't know when is a good time to flush the cache
        this._imageCache = new LinkedHashMap<>(cacheSize, 0.75f, true);
        this._repaintListener = repaintListener;
    }

    public static ImageResource loadImageResourceFromUri(final String uri) {
        if (isEmbeddedBase64Image(uri)) {
            return loadEmbeddedBase64ImageResource(uri);
        }

        try (InputStream is = IOUtil.getInputStream(uri)) {
            try {
                if (is == null) {
                    return createImageResource(uri, null);
                }
                BufferedImage img = ImageIO.read(is);
                if (img == null) {
                    throw new IOException("ImageIO.read() returned null");
                }
                return createImageResource(uri, img);
            } catch (FileNotFoundException e) {
                XRLog.exception("Can't read image file; image at URI '" + uri + "' not found");
            } catch (IOException e) {
                XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
            }
        } catch (IOException e) {
            // couldn't open stream at URI...
            XRLog.exception("Can't open stream for URI '" + uri + "': " + e.getMessage());
        }

        return createImageResource(uri, null);
    }

    public static ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        BufferedImage bufferedImage = ImageUtil.loadEmbeddedBase64Image(uri);
        if (bufferedImage != null) {
            FSImage image = AWTFSImage.createImage(bufferedImage);
            return new ImageResource(null, image);
        } else {
            return new ImageResource(null, null);
        }
    }

    public synchronized void shrink() {
        int ovr = _imageCache.size() - _imageCacheCapacity;
        Iterator<CacheKey> it = _imageCache.keySet().iterator();
        while (it.hasNext() && ovr-- > 0) {
            it.next();
            it.remove();
        }
    }

    public synchronized void clear() {
        _imageCache.clear();
    }

    @CheckReturnValue
    public ImageResource get(final String uri) {
        return get(uri, -1, -1);
    }

    @CheckReturnValue
    public synchronized ImageResource get(final String uri, final int width, final int height) {
        if (isEmbeddedBase64Image(uri)) {
            ImageResource resource = loadEmbeddedBase64ImageResource(uri);
            FSImage image = resource.getImage();
            FSImage scaledImage = image == null ? null : image.scale(width, height);
            return new ImageResource(resource.getImageUri(), scaledImage);
        } else {
            CacheKey key = new CacheKey(uri, width, height);
            ImageResource ir = _imageCache.get(key);
            if (ir == null) {
                // not loaded, or not loaded at target size

                // loaded a base size?
                ir = _imageCache.get(new CacheKey(uri, -1, -1));

                // no: loaded
                if (ir == null) {
                    if (isImmediateLoadUri(uri)) {
                        XRLog.load(Level.FINE, "Load immediate: " + uri);
                        ir = loadImageResourceFromUri(uri);
                        FSImage awtfsImage = ir.getImage();
                        BufferedImage newImg = ((AWTFSImage) awtfsImage).getImage();
                        loaded(ir, -1, -1);
                        if (width > -1 && height > -1) {
                            XRLog.load(Level.FINE, this + ", scaling " + uri + " to " + width + ", " + height);
                            newImg = ImageUtil.getScaledInstance(newImg, width, height);
                            ir = new ImageResource(ir.getImageUri(), AWTFSImage.createImage(newImg));
                            loaded(ir, width, height);
                        }
                    } else {
                        XRLog.load(Level.FINE, "Image cache miss, URI not yet loaded, queueing: " + uri);
                        MutableFSImage mfsi = new MutableFSImage(_repaintListener);
                        ir = new ImageResource(uri, mfsi);
                        _loadQueue.addToQueue(this, uri, mfsi, width, height);
                    }

                    _imageCache.put(key, ir);
                } else {
                    // loaded at base size, need to scale
                    XRLog.load(Level.FINE, this + ", scaling " + uri + " to " + width + ", " + height);
                    FSImage awtfsImage = ir.getImage();
                    BufferedImage newImg = ((AWTFSImage) awtfsImage).getImage();

                    newImg = ImageUtil.getScaledInstance(newImg, width, height);
                    ir = new ImageResource(ir.getImageUri(), AWTFSImage.createImage(newImg));
                    loaded(ir, width, height);
                }
            }
            return ir;
        }
    }

    public boolean isImmediateLoadUri(final String uri) {
        return ! _useBackgroundImageLoading || uri.startsWith("jar:file:") || uri.startsWith("file:");
    }

    public synchronized void loaded(final ImageResource ir, final int width, final int height) {
        String imageUri = ir.getImageUri();
        if (imageUri != null) {
            _imageCache.put(new CacheKey(imageUri, width, height), ir);
        }
    }

    public static ImageResource createImageResource(final String uri, @Nullable final BufferedImage img) {
        if (img == null) {
            return new ImageResource(uri, AWTFSImage.createImage(ImageUtil.createTransparentImage(10, 10)));
        } else {
            return new ImageResource(uri, AWTFSImage.createImage(ImageUtil.makeCompatible(img)));
        }
    }

    public void stopLoading() {
        if (_loadQueue != null) {
            XRLog.load("By request, clearing pending items from load queue: " + _loadQueue.size());
            _loadQueue.reset();
        }
    }

    private record CacheKey(String uri, int width, int height) {
    }
}

// from-io-loader
// from-cache-loader
// from-fs-loader
