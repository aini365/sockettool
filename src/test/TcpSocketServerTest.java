import org.junit.Test;

import java.nio.ByteBuffer;

public class TcpSocketServerTest {

    @Test
    public void sendData(){
        byte[] data={0x01,0x01};
        ByteBuffer outBuf = ByteBuffer.allocateDirect(10);    //

        System.out.println(outBuf.limit());
        System.out.println(outBuf.position());
        outBuf.put(data);    // 向缓冲区中设置内容

        System.out.println(outBuf.limit());
        System.out.println(outBuf.position());

        outBuf.flip();

        System.out.println(outBuf.limit());
        System.out.println(outBuf.position());

        outBuf.clear();

        System.out.println(outBuf.limit());
        System.out.println(outBuf.position());
    }
}
