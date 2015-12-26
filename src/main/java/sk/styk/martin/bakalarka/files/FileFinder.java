package sk.styk.martin.bakalarka.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Styk on 23.11.2015.
 */
public class FileFinder {

    private List<File> apkFolders;
    private List<File> files = new ArrayList<File>();

    public FileFinder(File folder) {
        if (folder == null) {
            throw new NullPointerException("folder null");
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not valid directory");
        }
        this.apkFolders = new ArrayList<File>();
        this.apkFolders.add(folder);
    }

    public FileFinder(File... folders) {
        this.apkFolders = new ArrayList<File>();
        for (File folder : folders) {
            if (folder == null) {
                throw new NullPointerException("folder null");
            }
            if (!folder.isDirectory()) {
                throw new IllegalArgumentException("folder is not valid directory" + folder.getAbsolutePath());
            }
            this.apkFolders.add(folder);
        }
    }

    public FileFinder(List<File> folders) {
        this.apkFolders = new ArrayList<File>();
        for (File folder : folders) {
            if (folder == null) {
                throw new NullPointerException("folder null");
            }
            if (!folder.isDirectory()) {
                throw new IllegalArgumentException("folder is not valid directory" + folder.getAbsolutePath());
            }
            this.apkFolders.add(folder);
        }
    }

    public List<File> getAllFilesInDirectories(){
        for (File f : apkFolders){
            getAllFilesInDirectory(f);
        }
        return files;
    }

    private void getAllFilesInDirectory(File directory) {
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {

                files.add(file);

            } else if (file.isDirectory()) {
                getAllFilesInDirectory(new File(file.getAbsolutePath()));
            }
        }
    }

    public List<ApkFile> getApkFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, "apk");
        }
        List<ApkFile> apkFiles = new ArrayList<ApkFile>();
        for (File f : files) {
            apkFiles.add(new ApkFile(f.getAbsolutePath()));
        }
        return apkFiles;
    }

    public List<File> getCertificateFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, "RSA", "DSA");
        }
        return files;
    }

    public List<File> getMFFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, "MF");
        }
        return files;
    }

    public List<File> getJsonFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, "json");
        }
        return files;
    }

    public List<File> getXmlFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, "xml");
        }
        return files;
    }

    public List<File> getStringResourceFilesInDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFullNameMatch(directory, "strings.xml");
        }
        return files;
    }

    public List<File> getDrawableResourceFiles() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getFilesInDirectoryFileTypeMatch(directory, ".jpg", ".jpeg", ".JPG", ".JPEG", ".gif", ".GIF", "png", "PNG", ".xml", ".XML");
        }
        return files;
    }

    private void getFilesInDirectoryFileTypeMatch(File directory, String... typeFilter) {
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                for (String type : typeFilter) {
                    if (file.getName().endsWith(type))
                        files.add(file);
                    //break;
                }
            } else if (file.isDirectory()) {
                getFilesInDirectoryFileTypeMatch(new File(file.getAbsolutePath()), typeFilter);
            }
        }
    }

    public void getFilesInDirectoryFullNameMatch(File directory, String... typeFilter) {
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                for (String type : typeFilter) {
                    if (file.getName().equals(type))
                        files.add(file);
                    break;
                }
            } else if (file.isDirectory()) {
                getFilesInDirectoryFullNameMatch(new File(file.getAbsolutePath()), typeFilter);
            }
        }
    }

    public List<File> getDrawableDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getDirectoriesContainingExpression(directory, "drawable");
        }
        return files;
    }

    public List<File> getLayoutDirectories() {
        files = new ArrayList<File>();
        for (File directory : apkFolders) {
            getDirectoriesContainingExpression(directory, "layout");
        }
        return files;
    }

    private void getDirectoriesContainingExpression(File directory, String matchExpression) {
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isDirectory()) {
                if (file.getName().contains(matchExpression)) {
                    files.add(file);
                }
                getDirectoriesContainingExpression(new File(file.getAbsolutePath()), matchExpression);
            }
        }
    }
}
