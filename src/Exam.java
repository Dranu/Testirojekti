class A {
    String name;
    int size;
    double prize;
    double weight;
    
    A(String n, int s, double p, double w) {
        name = n;
        size = s;
        prize = p;
        weight = w;
    }
    
    public void writeToAFile(B b) {
        BufferedWriter br = new BufferedWriter(new FileWriter(new File("data.txt")));
        br.write(b.name);

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("object.ser"));
        out.writeObject(b);
    }
}

class B implements Serializable{
    String name;
    int size;
    double prize;
    double lenght;
    
    B(String n, int s, double p, double l) {
        name = n;
        size = s;
        prize = p;
        lenght = l;
    }
    
    public float getUltimateValue() {
        return size * prize / Math.sqrt(lenght);
    }
}

public class Exam {

    public static void main(String[] args) {
        A a = new A("A", 2, 10.4, 4.7);
        B b = new B("B", 7, 21.9, 102,7);
        
        a.writeToAFile(b);

    }
    
}