package com.techweblearn.Utils;

import com.techweblearn.CombiningFileListener;
import com.techweblearn.DownloadListener;
import com.techweblearn.DownloadTask;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

public class CombiningPartFilesThread implements Runnable {

    private File output;
    private DownloadTask[] downloadTasks;
    private CombiningFileListener combiningFileListener;


    public CombiningPartFilesThread(File output, DownloadTask[] downloadTasks, CombiningFileListener combiningFileListener) {
        this.output = output;
        this.downloadTasks = downloadTasks;
        this.combiningFileListener = combiningFileListener;
    }


    @Override
    public void run() {
        int read;
        int total_read = 0;

        if(!output.exists())
        {
            try {
                output.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteBuffer byteBuffer=ByteBuffer.allocate(8192);
        Path outFile=Paths.get(output.toURI());

        try(FileChannel outChannel=FileChannel.open(outFile,WRITE,APPEND,CREATE)) {

            System.out.println("Combining Files");
            for (DownloadTask downloadTask : downloadTasks) {

                Path inFile = Paths.get(downloadTask.getFilename());
                try (FileChannel inFileChannel = FileChannel.open(inFile, READ,WRITE)) {

                    while (true) {
                        byteBuffer.clear();
                        read = inFileChannel.read(byteBuffer);
                        byteBuffer.flip();
                        outChannel.write(byteBuffer);
                        if (read == -1)
                            break;
                        total_read += read;

                    }

                } catch (IOException e) {
                    combiningFileListener.combineError();
                    e.printStackTrace();
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        DeleteFiles.delete(downloadTasks);
        combiningFileListener.combineCompleted();
    }
}