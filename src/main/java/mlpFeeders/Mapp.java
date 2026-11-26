package mlpFeeders;

import java.util.*;

public class Mapp extends CustomMemory<String,List<String>> {


    public void print() {
        memory.forEach(System.out::println);
    }

    public Mapp add(String name, List<String> tasks) {
        memory.add(Map.entry(name, tasks));
        return this;
    }

    static List<String> names = List.of("Jay", "Sahu");

    public static void main(String[] args) {

        List<String> name = names.stream().map(String::toUpperCase).filter(n -> n.startsWith("J")).toList();

        System.out.println(name);
        boolean flag = false;
        if(!flag){

        }
    }
}