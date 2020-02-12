public class Card{
    String langName,name,imageName;
    int ownerID=-1;
    public Card(String name, String langName, String imageName){
        this.name=name;
        this.langName=langName;
        this.imageName=imageName;
    }
    
    public void printCard(){
        System.out.println("Intern card name:     "+name);
        System.out.println("Translated card name: "+langName);
        System.out.println("Image file name:      "+imageName+".png");
    }
}
