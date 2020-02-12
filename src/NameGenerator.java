public class NameGenerator{
    FileManager fm=new FileManager();
    String names[], decoration[];
    int maxLength=99;
    String lastGenerated="";
    public NameGenerator(){
        names=fm.readFile("names/usernames.name");
        decoration=fm.readFile("names/decoration.name");
    }

    public NameGenerator(int maxLength){
        this.maxLength=maxLength;
        names=fm.readFile("names/usernames.name");
        decoration=fm.readFile("names/decoration.name");
    }

    public void generateNames(int amount){
        for(int i=0;i<amount;i++)
            System.out.println(addDecoration(addNumbers(names[randomInt(0,names.length-1)])));
    }

    public String generateName(){
        do{
            lastGenerated=addDecoration(addNumbers(names[randomInt(0,names.length-1)]));
        }while(lastGenerated.length()>=maxLength);
        return lastGenerated;
    }

    public String addDecoration(String name){
        if(randomInt(0, 1)==0)
            name=String.valueOf(name.charAt(0)).toUpperCase()+name.substring(1,name.length()-1);
        if(randomInt(0, 2)==0){
            int randomIndex=randomInt(0,decoration.length-1);
            if(decoration[randomIndex].charAt(0)!='#'){
                name=decoration[randomIndex]+name;
                for(int i=decoration[randomIndex].length()-1;i>=0;i--)
                    name=name+decoration[randomIndex].charAt(i);
            }else{
                if(decoration[randomIndex].charAt(1)!='#'){
                    name=decoration[randomIndex].replace("#","")+name+decoration[randomIndex].replace("#","");
                }else{
                    if(decoration[randomIndex].charAt(2)=='#'){
                        if(decoration[randomIndex].charAt(3)=='#'){
                            name=decoration[randomIndex].replace("#","")+name;
                        }else{
                            name=name+decoration[randomIndex].replace("#","");
                        }
                    }else{
                        name=decoration[randomIndex].replace("#","")+" "+name+" "+decoration[randomIndex].replace("#","");
                    }
                }
            }
        }
        return name;
    }

    public String addNumbers(String name){
        if(randomInt(0, 2)==0){
            int randomAmount=randomInt(1,6);
            for(int i=0;i<randomAmount;i++)
                name=name+randomInt(0,9);
        }
        return name;
    }

    int randomInt(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
