import java.io.*;
import java.util.List;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileManager{
    String result[];
    public String[] readFile(String filename){
        File file = new File(filename);
        try{
            BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8") );
            BufferedReader br2 = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8") );//Windows-1252

            int lines = 0, counter=0;
            while (br2.readLine() != null) lines++;
            result=new String[lines];

            String st;
            while ((st = br.readLine()) != null){
                result[counter]=st;
                counter++;
            }
            return result;
        }catch (IOException e){return null;}
    }
    
    public String[] getFiles(String path){
        File directoryPath = new File(path);
        int counter=0;
        for (File file : directoryPath.listFiles()){
            counter++;
        }
        String allFiles[]=new String[counter];counter=0;
        for (File file : directoryPath.listFiles()){
            allFiles[counter]=file.getName(); counter++;
        }
        return allFiles;
    }

    public String[] getFilesWithEnding(String path, String ending){
        final String ending2=ending.replace(".", "");
        File directoryPath = new File(path);

        File[] files=directoryPath.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith("."+ending2);
                    }
                });

        int counter=0;
        for (File file : files){
            counter++;
        }
        String allFiles[]=new String[counter];counter=0;
        for (File file : files){
            allFiles[counter]=file.getName(); counter++;
        }
        return allFiles;
    }
}