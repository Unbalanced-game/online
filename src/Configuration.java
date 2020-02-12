import java.io.*;
public class Configuration{
    String config[][], filename;
    boolean ready=false;
    public Configuration(String pFilename){
        filename=pFilename+".cfg";
        readConfig(filename);
    }

    public String getOptionString(String option){
        if(ready){
            for(int i=0;i<config.length;i++){
                if(config[i][0].equals(option)) return config[i][1];
            }
        }
        return "";
    }

    public int getOptionInt(String option){
        if(ready){
            for(int i=0;i<config.length;i++){
                if(config[i][0].equals(option)) return Integer.parseInt(config[i][1]);
            }
        }
        return -1;
    }

    public void setOption(String option, String value){
        if(ready){
            String output="";
            for(int i=0;i<config.length;i++){
                output=output+config[i][0]+":"+config[i][1]+"\n";
            }
            output=output.substring(0, output.length() - 1).replace(option+":"+getOptionString(option), option+":"+value);
            try{
                writeToFile(filename,output.replaceAll("(?m)^[ \t]*\r?\n",""));
            }catch(Exception e){;}
            readConfig(filename);
        }
    }

    public void setOption(String option, int value){
        if(ready){
            String output="";
            for(int i=0;i<config.length;i++){
                output=output+config[i][0]+":"+config[i][1]+"\n";
            }
            output=output.substring(0, output.length() - 1).replace(option+":"+getOptionString(option), option+":"+value);
            try{
                writeToFile(filename,output.replaceAll("(?m)^[ \t]*\r?\n",""));
            }catch(Exception e){;}
            readConfig(filename);
        }
    }

    public void addOption(String option){
        if(ready && !optionExists(option)){
            String output="";
            for(int i=0;i<config.length;i++){
                output=output+config[i][0]+":"+config[i][1]+"\n";
            }
            output=(output+"\n"+option+":null");
            try{
                writeToFile(filename,output.replaceAll("(?m)^[ \t]*\r?\n",""));
            }catch(Exception e){;}
            readConfig(filename);
        }
    }

    public void removeOption(String option){
        if(ready && optionExists(option)){
            String output="";
            for(int i=0;i<config.length;i++){
                if(!config[i][0].equals(option))
                    output=output+config[i][0]+":"+config[i][1]+"\n";
            }
            output=output.substring(0, output.length() - 1);
            try{
                writeToFile(filename,output.replaceAll("(?m)^[ \t]*\r?\n",""));
            }catch(Exception e){;}
            readConfig(filename);
        }
    }

    public boolean optionExists(String option){
        if(ready){
            for(int i=0;i<config.length;i++){
                if(option.equals(config[i][0]))return true;
            }
        }
        return false;
    }
    
    public void refresh(){
        readConfig(filename);
    }

    private void readConfig(String pFilename){
        if(!(new File(pFilename).isFile()))
            try{
                writeToFile(pFilename,"");
            }catch(Exception e){;}
        try{
            String input[]=readFile(pFilename);
            config=new String[input.length][2];
            for(int i=0;i<input.length;i++){
                config[i]=input[i].split(":");
            }
            ready=true;
        }catch(Exception e){;}
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

    private void writeToFile(String pFilename, String text) throws IOException{
        File file = new File(pFilename);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "Cp1252"));
        bw.write(text);
        bw.close();
    }
}
