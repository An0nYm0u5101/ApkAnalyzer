package sk.styk.martin.bakalarka.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.styk.martin.bakalarka.data.ApkData;
import sk.styk.martin.bakalarka.data.ResourceData;
import sk.styk.martin.bakalarka.files.ApkFile;
import sk.styk.martin.bakalarka.files.FileFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin Styk on 29.11.2015.
 */
public class ResourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceProcessor.class);
    private static ResourceProcessor instance = null;
    private ApkData data;
    private ApkFile apkFile;
    private ResourceData resourceData;

    private ResourceProcessor() {
        // Exists only to defeat instantiation.
    }

    public static ResourceProcessor getInstance(ApkData data, ApkFile apkFile) {
        if (data == null) {
            throw new IllegalArgumentException("data null");
        }
        if (apkFile == null) {
            throw new IllegalArgumentException("apkFile null");
        }

        if (instance == null) {
            instance = new ResourceProcessor();
        }
        instance.data = data;
        instance.apkFile = apkFile;
        instance.resourceData = null;
        return instance;
    }

    public static ResourceProcessor getInstance(ApkFile apkFile) {
        if (instance == null) {
            instance = new ResourceProcessor();
        }
        if (apkFile == null) {
            throw new IllegalArgumentException("apkFile null");
        }
        instance.data = null;
        instance.apkFile = apkFile;
        instance.resourceData = null;
        return instance;
    }

    public ResourceData processResources() {

        logger.trace("Started processing of resources");

        resourceData = new ResourceData();
        resourceData.setLocale(getStringLocalizations());

        processDrawableResources();

        if (data != null) {
            data.setResourceData(resourceData);
        }

        logger.trace("Finished processing of localizations");

        return resourceData;
    }

    private void processDrawableResources() {
        List<File> drawableFiles = null;
        List<File> directories = null;

        try {
            FileFinder ff = new FileFinder(new File(apkFile.getUnzipDirectoryWithUnzipedData(), "res"));
            directories = ff.getDrawableDirectories();
            ff = new FileFinder(directories);
            drawableFiles = ff.getDrawableResourceFiles();
        } catch (IllegalArgumentException e) {
            logger.warn("res directory doesn`t exists");
            return;
        }
        processDrawablesTypes(drawableFiles);
        processDifferentDrawables(drawableFiles);
        processDrawableDirectories(directories);
    }

    private List<String> getStringLocalizations() {

        List<File> files = null;

        try {
            FileFinder ff = new FileFinder(new File(apkFile.getDecompiledDirectoryWithDecompiledData(), "res"));
            files = ff.getStringResourceFilesInDirectories();
        } catch (IllegalArgumentException e) {
            logger.warn("res directory doesn`t exists");
            return null;
        }

        List<String> localizations = new ArrayList<String>();

        for (File f : files) {
            processLocalization(f, localizations);
        }

        return localizations;

    }

    private void processLocalization(File f, List<String> localizations) {

        String parentName = null;
        try {
            parentName = f.getParentFile().getName();
        } catch (Exception e) {
            logger.warn("parent of file " + f.getPath() + " not found");
            return;
        }

        String REGEX = "values-(\\D*)";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(parentName);


        if (matcher.find()) {
            localizations.add(matcher.group(1));
        }
    }

    private void processDrawablesTypes(List<File> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        int numJpg = 0;
        int numGif = 0;
        int numPng = 0;
        int numXml = 0;

        for (File f : files) {

            String name = f.getName();

            if (name.endsWith(".jpg") ||
                    name.endsWith(".JPG") ||
                    name.endsWith(".jpeg") ||
                    name.endsWith(".JPEG")) {
                numJpg++;
            } else if (name.endsWith(".gif") ||
                    name.endsWith(".GIF")) {
                numGif++;
            } else if (name.endsWith(".png") ||
                    name.endsWith(".PNG")) {
                numPng++;
            } else if (name.endsWith(".xml") ||
                    name.endsWith(".XML")) {
                numXml++;
            }
        }
        resourceData.setGifDrawables(numGif);
        resourceData.setPngDrawables(numPng);
        resourceData.setJpgDrawables(numJpg);
        resourceData.setXmlDrawables(numXml);

    }

    private void processDifferentDrawables(List<File> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        Set<String> names = new HashSet<String>();
        for (File f : files) {
            names.add(f.getName());
        }
        resourceData.setDifferentDrawables(names.size());
    }

    private void processDrawableDirectories(List<File> directories) {

        boolean success = false;

        int withoutdpi = 0;
        int ldpi = 0;
        int mdpi = 0;
        int hdpi = 0;
        int xhdpi = 0;
        int xxhdpi = 0;
        int xxxhdpi = 0;

        if (directories == null || directories.isEmpty()) {
            return;
        }

        for (File dir : directories) {
            FileFinder ff = new FileFinder(dir);
            int size = ff.getDrawableResourceFiles().size();

            if (size > 0) {
                success = true;
                if (dir.getName().contains("ldpi")) ldpi += size;
                else if (dir.getName().contains("mdpi")) mdpi += size;
                else if (dir.getName().contains("xxxhdpi")) xxxhdpi += size;
                else if (dir.getName().contains("xxhdpi")) xxhdpi += size;
                else if (dir.getName().contains("xhdpi")) xhdpi += size;
                else if (dir.getName().contains("hdpi")) hdpi += size;
                else if (dir.getName().contains("drawable")) withoutdpi += size;
            }
        }
        if (success) {
            resourceData.setLdpiDrawables(ldpi);
            resourceData.setMdpiDrawables(mdpi);
            resourceData.setHdpiDrawables(hdpi);
            resourceData.setXhdpiDrawables(xhdpi);
            resourceData.setXxhdpiDrawables(xxhdpi);
            resourceData.setXxxhdpiDrawables(xxxhdpi);
            resourceData.setUnspecifiedDpiDrawables(withoutdpi);
        }
    }
}