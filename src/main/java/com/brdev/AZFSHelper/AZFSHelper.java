package com.brdev.AZFSHelper;

import com.azure.storage.file.share.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AZFSHelper {
    private static final Logger log = LoggerFactory.getLogger(AZFSHelper.class);
    private final String connectionString;
    private final String shareName;
    private final String accountName;
    private ShareClient shareClient;

    public AZFSHelper(String connectionString, String shareName, String accountName) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalArgumentException("Connection string cannot be null or empty.");
        }
        if (shareName == null || shareName.isEmpty()) {
            throw new IllegalArgumentException("Share name cannot be null or empty.");
        }
        if (accountName == null || accountName.isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be null or empty.");
        }

        this.connectionString = connectionString;
        this.shareName = shareName;
        this.accountName = accountName;
    }

    public void initConnection() {
        try {
            String shareURL = String.format("https://%s.file.core.windows.net", accountName);

            shareClient = new ShareClientBuilder()
                    .endpoint(shareURL)
                    .connectionString(connectionString)
                    .shareName(shareName)
                    .buildClient();

            System.out.println("Connection initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize connection: " + e.getMessage());
            log.error(e.getMessage());
        }
    }

    public ShareClient getShareClient() {
        return shareClient;
    }

    private ShareDirectoryClient enterDirectory(String filePath, boolean canCreateFolder)
    {
        ShareDirectoryClient directoryClient = null;
        String[] paths = filePath.split("/");

        try {
            ShareDirectoryClient rootDir = shareClient.getRootDirectoryClient();
            ShareDirectoryClient fileDir = null;

            for (int i = 0, size = paths.length; i < size; i++) {
                String filePathName = paths[i];

                if (i == 0 && Objects.equals(filePathName, rootDir.getShareName())) {
                    continue;
                }

                fileDir = rootDir.getSubdirectoryClient(filePathName);

                if (canCreateFolder) {
                    if (!fileDir.exists()) {
                        fileDir = rootDir.createSubdirectory(filePathName);
                    }
                }

                if (fileDir.exists()) {
                    directoryClient = fileDir;
                    rootDir = fileDir;
                }
                else {
                    return null;
                }
            }

        }
        catch (Exception e) {
            log.error("Cannot enter into the filepath: {}", e.getMessage());
            return null;
        }

        log.info("cd: {}", filePath);
        return directoryClient;
    }

    public OutputStream donwloadFileAsStream(String fileName, String filePath) {
        try {
            ShareDirectoryClient directory = enterDirectory(filePath, false);

            if (directory != null) {
                ShareFileClient file = directory.getFileClient(fileName);

                if (file.exists()) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    file.download(stream);
                    return  stream;
                }
            }
        }
        catch (Exception e) {
            log.error("Error while downloading the file: {}", e.getMessage());
        }

        return null;
    }

    public List<String> listFiles(String matchFileName, String filePath, boolean matchExactly) {
        List<String> files = new LinkedList<>();

        try {
            ShareDirectoryClient directory = enterDirectory(filePath, false);
            if (directory != null)
            {
                var filesAndDirectories = directory.listFilesAndDirectories();

                filesAndDirectories.forEach(shareFileItem -> {
                    if (!shareFileItem.isDirectory()) {
                        if (matchExactly) {
                            if (Objects.equals(shareFileItem.getName(), matchFileName)) {
                                files.add(shareFileItem.getName());
                            }
                        }
                        else {
                            if (shareFileItem.getName().contains(matchFileName)) {
                                files.add(shareFileItem.getName());
                            }
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        return files;
    }

    public boolean uploadFile(String fName, StorageFileInputStream file, String filePath)
    {
        boolean uploaded = false;

        try {
            ShareDirectoryClient directory = enterDirectory(filePath, true);
            if (directory != null) {
                byte[] bytes = file.readAllBytes();
                int len = bytes.length;
                ShareFileClient fileClient = directory.createFile(fName, len);
                InputStream inputStream = new ByteArrayInputStream(bytes);
                fileClient.uploadRange(inputStream, len);
                uploaded = true;

                log.info("The file {} was uploaded into {} directory.", fName, filePath);
            }
        }
        catch (Exception e) {
            log.error("Error while uploading file: {}", e.getMessage());
        }

        return uploaded;
    }

    public boolean deleteFile(String fileName, String filePath) {
        boolean deleted = false;

        try {
            ShareDirectoryClient directory = enterDirectory(filePath, false);

            if (directory != null) {
                var file = directory.getFileClient(fileName);
                if (file.exists()) {
                    file.delete();
                    deleted = true;
                    log.info("The file {} was deleted.", fileName);
                }
            }

        }
        catch (Exception e) {
            log.error("Error while deleting file: {}", e.getMessage());
        }

        return deleted;
    }

    public boolean moveFile(String fileName, String fromFilePath, String toFilePath, boolean deleteOriginal)
    {
        boolean moved = false;

        try {
            var fromDirectory = enterDirectory(fromFilePath, false);
            var toDirectory = enterDirectory(toFilePath, true);

            if (fromDirectory != null && toDirectory != null) {
                var file = fromDirectory.getFileClient(fileName);
                if (file.exists()) {
                    var inputStream = file.openInputStream();
                    uploadFile(fileName, inputStream, toFilePath);
                    inputStream.close();

                    if (deleteOriginal){
                        file.delete();
                    }

                    moved = true;
                }
            }
        }
        catch (Exception e) {
            log.error("Erro while moving file: {}", e.getMessage());
        }

        return moved;
    }
}
