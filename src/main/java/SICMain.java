import java.util.Scanner;

public class SICMain {
    public static void main(String[] args){
        Assembler assembler = new Assembler();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please choose a assembly code file:");
        int x = scanner.nextInt();
        assembler.execute(x);
    }
}
