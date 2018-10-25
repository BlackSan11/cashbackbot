package me.btcback;

public class UpdateAgent extends Thread {

    public UpdateAgent() {
    }

    public void run(){
        while(true){
            Setts.getInstance().updateSettings();
            System.out.println("Settings updated");
            try {
                this.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
