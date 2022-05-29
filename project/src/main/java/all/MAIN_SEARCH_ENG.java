package all;

public class MAIN_SEARCH_ENG {
    public static void main(String[] args) throws Exception {
    Runnable ex1 = new SEARCH_ENG();
    Runnable ex2 = new SEARCH_ENG();
    Thread t1 = new Thread(ex1);
    Thread t2 = new Thread(ex2);
    t1.setName("1");
    t2.setName("2");
    t1.start();
    t2.start();
    t1.join();
    t2.join();
}
}
