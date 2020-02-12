import java.io.*;
import javax.swing.JOptionPane;
public class Language{
    String strings[][][],langs[],files[],selectedLangStr="english",namespace="lang";
    boolean ready=false; int selectedLangInt=0;
    public Language(String language,String pNamespace){
        try{
            selectedLangStr=language;
            namespace=pNamespace;
            readLang();
        }catch(Exception e){message("Error","An error occured while reading lang files.");}
    }

    public Language(String pNamespace,boolean isParamNamespace){
        try{
            namespace=pNamespace;
            readLang();
        }catch(Exception e){message("Error","An error occured while reading lang files.");}
    }

    public Language(String language){
        try{
            selectedLangStr=language;
            readLang();
        }catch(Exception e){message("Error","An error occured while reading lang files.");}
    }

    public Language(){
        try{
            selectedLangStr=getDefaultLanguage();
            readLang();
        }catch(Exception e){message("Error","An error occured while reading lang files.");}
    }

    public String get(String st){
        if(ready){
            for(int i=0;i<strings[selectedLangInt].length;i++){
                if(strings[selectedLangInt][i][0].equals(st)) return strings[selectedLangInt][i][1];
            }
        }
        return "missingString:"+st;
    }

    public String get(String st, Object variable1, Object variable2, Object variable3){
        if(ready){
            for(int i=0;i<strings[selectedLangInt].length;i++){
                if(strings[selectedLangInt][i][0].equals(st)) return strings[selectedLangInt][i][1].replace("{1}",String.valueOf(variable1)).replace("{2}",String.valueOf(variable2)).replace("{3}",String.valueOf(variable3));
            }
        }
        return "missingString:"+st;
    }

    public String get(String st, Object variable1, Object variable2){
        if(ready){
            for(int i=0;i<strings[selectedLangInt].length;i++){
                if(strings[selectedLangInt][i][0].equals(st)) return strings[selectedLangInt][i][1].replace("{1}",String.valueOf(variable1)).replace("{2}",String.valueOf(variable2));
            }
        }
        return "missingString:"+st;
    }

    public String get(String st, Object variable1){
        if(ready){
            for(int i=0;i<strings[selectedLangInt].length;i++){
                if(strings[selectedLangInt][i][0].equals(st)) return strings[selectedLangInt][i][1].replace("{1}",String.valueOf(variable1));
            }
        }
        return "missingString:"+st;
    }

    public void selectLanguage(String language, boolean selectAsDefault){
        language=language.toLowerCase();
        try{
            for(int i=0;i<files.length;i++){
                if(files[i].contains(language)){
                    if(selectAsDefault)writeToFile(namespace+"/defaultLanguage.opt",language);
                    selectedLangStr=language;selectedLangInt=i;return;
                }
            }
        }catch(Exception e){message("Information","Did not set language.");}
    }

    public void selectLanguagePopup(boolean selectAsDefault){
        try{
            String language=dropDown(get("langSelectionText"),"",langs).toLowerCase();
            for(int i=0;i<files.length;i++){
                if(files[i].contains(language)){
                    if(selectAsDefault)writeToFile(namespace+"/defaultLanguage.opt",language);
                    selectedLangStr=language;selectedLangInt=i;return;
                }
            }
        }catch(Exception e){message("Information","Did not set language.");}
    }

    private String dropDown(String titel, String nachricht, String[] optionen){
        return (String) JOptionPane.showInputDialog(null,nachricht,titel,JOptionPane.QUESTION_MESSAGE,null,optionen,optionen[0]);
    }

    public void refresh(){
        readLang();
    }

    public String getDefaultLanguage(){
        return readFile(namespace+"/defaultLanguage.opt")[0];
    }

    public void getInformation(){
        if(ready)
            message(selectedLangStr.toUpperCase().charAt(0)+selectedLangStr.substring(1,selectedLangStr.length()),get("langInfoAuthorText")+" "+get("langInfoAuthor")+"\n"+get("langInfoVersionText")+" "+get("langInfoVersion"));
        else message("Error","An error occured while reading lang files.");
    }

    public String[] getLanguages(){
        return langs;
    }

    private void message(String title, String message){
        JOptionPane.showMessageDialog(null,message,title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void readLang(){
        files=getFilesWithEnding(namespace+"/",".lang");
        langs=new String[files.length];
        strings=new String[files.length][][];
        for(int i=0;i<files.length;i++){
            langs[i]=files[i].toUpperCase().charAt(0)+files[i].substring(1,files[i].length()).replace(".lang","");
            String input[]=readFile(namespace+"/"+files[i]);
            strings[i]=new String[input.length][2];
            for(int j=0;j<input.length;j++){
                strings[i][j]=input[j].split(":",2);
            }
        }
        selectLanguage(selectedLangStr,false);
        ready=true;
    }

    private String[] readFile(String pFilename){
        String result[];
        File file = new File(pFilename);
        try{
            BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(file), "Windows-1252") );
            BufferedReader br2 = new BufferedReader( new InputStreamReader( new FileInputStream(file), "Windows-1252") );

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

    private void writeToFile(String filename, String text){
        try{
            File file = new File(filename);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bw.write(text);
            bw.close();
        }catch (IOException e){}
    }

    private String[] getFilesWithEnding(String path, String ending){
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
