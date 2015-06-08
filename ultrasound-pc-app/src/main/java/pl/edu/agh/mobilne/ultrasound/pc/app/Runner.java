package pl.edu.agh.mobilne.ultrasound.pc.app;

public class Runner {

    public static void main(String[] argv) throws Exception {

        //byte[] byteArr = new byte[] {1, 2, 3, 4, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 1, 5, 1, 1, 1, 4, 5, 5, 5, 5, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0};
        //byte[] byteArr = new byte[] {1, 3, 5, 7, 9, 11, 13, 15, 0, 2, 4, 6, 8, 10, 12, 14};
        SenderPC senderPC = new SenderPC(new byte[]{0});
        //ReceiverPC receiverPC = new ReceiverPC();
        TokenGenerator tokenGenerator = new TokenGenerator();
        new Thread(senderPC).start();
        //new Thread(receiverPC).start();
        Thread.sleep(500);
        senderPC.setData(tokenGenerator.getToken());
        Thread.sleep(5000);
        senderPC.setData(tokenGenerator.getToken());
        Thread.sleep(5000);
        senderPC.setData(tokenGenerator.getToken());
        Thread.sleep(5000);
        senderPC.setData(tokenGenerator.getToken());
        /*ByteArrayInputStream bais = new ByteArrayInputStream(byteArr);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new DataProcessor(bais, baos).processData();

        System.out.println(Arrays.toString(baos.toByteArray()));*/
        //new Runner().start();

    }
}
