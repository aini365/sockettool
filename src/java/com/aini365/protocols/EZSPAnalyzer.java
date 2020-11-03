package com.aini365.protocols;

import com.aini365.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class EZSPAnalyzer implements Analyzer {

    private ArrayList<Byte> zigbee_buffer=new ArrayList<>();
    private final static int ZIGBEE_BUFFER_SIZE = 256;
    private final static byte  ZIGBEE_EZSP_CANCEL = 0x1A;  // cancel byte
    private final static byte  ZIGBEE_EZSP_EOF = 0x7E;          // end of frame
    private final static byte  ZIGBEE_EZSP_ESCAPE = 0x7D;       // escape byte
    private boolean  escape = false;
    private boolean frame_complete =false;
    private Queue<Byte> receiveQueue= new ConcurrentLinkedQueue();

    private Function<byte[],Boolean> onExplained;


    public EZSPAnalyzer(Function<byte[],Boolean> onExplained){
        this.onExplained=onExplained;
        startAnalyze();
    }

    @Override
    public void store(byte[] data){

        for (int index = 0; index < data.length; index++) {
            receiveQueue.add(data[index]);
        }
    }

    private void startAnalyze(){
        ExecutorService executorService =Executors.newSingleThreadExecutor();
        executorService.submit(()-> {
                try {
                    while (true){
                        byte[] explainData = explain();
                        if(explainData!=null && onExplained!=null) {
                            onExplained.apply(explainData);
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }


    public  byte[] explain(){
        byte[] result = null;
        byte zigbee_in_byte;
        Byte poll_byte = null;
        while (receiveQueue.size()>0 && !frame_complete){
            poll_byte =receiveQueue.poll();
            if(poll_byte==null){
                break;
            }
            zigbee_in_byte =poll_byte.byteValue();

            if ((0x11 == zigbee_in_byte) || (0x13 == zigbee_in_byte)) {
                continue;           // ignore reserved bytes XON/XOFF
            }

            if (ZIGBEE_EZSP_ESCAPE == zigbee_in_byte) {
                // AddLog_P2(LOG_LEVEL_DEBUG_MORE, PSTR("ZIG: Escape byte received"));
                escape = true;
                continue;
            }

            if (ZIGBEE_EZSP_CANCEL == zigbee_in_byte) {
                // AddLog_P2(LOG_LEVEL_DEBUG_MORE, PSTR("ZIG: ZbInput byte=0x1A, cancel byte received, discarding %d bytes"), zigbee_buffer->len());
                zigbee_buffer.clear();		// empty buffer
                escape = false;
                frame_complete = false;
                continue;                   // re-loop
            }

            if (ZIGBEE_EZSP_EOF == zigbee_in_byte) {
                // end of frame
                frame_complete = true;
                break;
            }

            if (zigbee_buffer.size() < ZIGBEE_BUFFER_SIZE) {
                if (escape) {
                    // invert bit 5
                    zigbee_in_byte ^= 0x20;
                    escape = false;
                }

                zigbee_buffer.add(zigbee_in_byte);
            }
        }


        int frame_len = zigbee_buffer.size();
        if (frame_complete) {
            char[] hex_char = new char[frame_len * 2 + 2];
            //ToHex_P((unsigned char*)zigbee_buffer->getBuffer(), zigbee_buffer->len(), hex_char, sizeof(hex_char));

            // AddLog_P2(LOG_LEVEL_DEBUG_MORE, PSTR(D_LOG_ZIGBEE "Bytes follow_read_metric = %0d"), ZigbeeSerial->getLoopReadMetric());
            if ((frame_complete) && (frame_len >= 3)) {
                // frame received and has at least 3 bytes (without EOF), checking CRC
                // AddLog_P2(LOG_LEVEL_INFO, PSTR(D_JSON_ZIGBEE_EZSP_RECEIVED ": received raw frame %s"), hex_char);



                int crc =crc1(zigbee_buffer,frame_len-2);
                //int crc = crc2(zigbee_buffer,frame_len-2);


                int crc_received = (zigbee_buffer.get(frame_len - 2) & 0xff) << 8 | zigbee_buffer.get(frame_len - 1) & 0xff;
                // remove 2 last bytes

                if (crc_received != crc) {
                    System.out.println("crc is error");
                    //AddLog_P2(LOG_LEVEL_INFO, PSTR(D_JSON_ZIGBEE_EZSP_RECEIVED ": bad crc (received 0x%04X, computed 0x%04X) %s"), crc_received, crc, hex_char);
                } else {
                    // copy buffer

                    List<Byte> ezsp_buffer = zigbee_buffer.subList(0, frame_len - 2);	// CRC

                    // CRC is correct, apply de-stuffing if DATA frame
                    if (0 == (ezsp_buffer.get(0) & 0x80)) {
                        // DATA frame
                        byte rand = 0x42;
                        for (int i=1; i<ezsp_buffer.size(); i++) {
                            ezsp_buffer.set(i, (byte)(ezsp_buffer.get(i) ^ rand));
                            if ((rand & 0xFF & 1)>0) {
                                rand = (byte)(((rand & 0xFF) >> 1) ^ 0xB8);
                            }
                            else {
                                rand = (byte)(((rand & 0xFF) >> 1));
                            }
                        }
                    }

                    //ToHex_P((unsigned char*)ezsp_buffer.getBuffer(), ezsp_buffer.len(), hex_char, sizeof(hex_char));
                    //AddLog_P2(LOG_LEVEL_DEBUG_MORE, PSTR(D_LOG_ZIGBEE "{\"" D_JSON_ZIGBEE_EZSP_RECEIVED "2\":\"%s\"}"), hex_char);
                    // now process the message
                    //ZigbeeProcessInputRaw(ezsp_buffer);
                    result = ByteUtil.arrayListToBytes(new ArrayList<>(ezsp_buffer));
                }
            } else {
                // the buffer timed-out, print error and discard
                //AddLog_P2(LOG_LEVEL_INFO, PSTR(D_JSON_ZIGBEE_EZSP_RECEIVED ": time-out, discarding %s, %d"), hex_char);
            }
            zigbee_buffer.clear();		// empty buffer
            escape = false;
            frame_complete = false;
        }
        return result;


    }


    public  int crc1(ArrayList<Byte> data_arr, int frame_len)
    {
        short crc = (short) 0xFFFF;                 // frame CRC
        // compute CRC
        for (int i=0; i<frame_len; i++) {
            crc = (short) (crc ^ ((data_arr.get(i)&0xFF) << 8));
            for (short j=0; j<8; j++) {
                if ((crc & 0x8000)!=0) {
                    crc = (short) (crc << 1 ^ 0x1021);          // polynom is x^16 + x^12 + x^5 + 1, CCITT standard
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xffff;
    }

    public static int crc2(ArrayList<Byte> testBytes,int length) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12), in your case: 0x1081

        // CRC of this should be 28570
        byte b;
        for (int j = 0; j < length; j++) {
            b= testBytes.get(j).byteValue();
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return crc;
        // 6F9A = 28570

    }


    public static byte[] send(byte[] msg){

        if(msg==null){
            return null;
        }

        boolean data_frame = (0 == (msg[0] & 0x80));
        int rand = 0x42;          // pseudo-randomizer initial value
        int crc = 0xFFFF;        // CRC16 CCITT initialization
        int len=msg.length;

        ArrayList<Byte> listResult=new ArrayList<>();

        for (int i=0; i<len; i++) {
            byte out_byte = msg[i];

            // apply randomization if DATA field
            if (data_frame && (i > 0)) {
                out_byte ^= rand;
                if ((rand & 1) >0) { rand = (rand >> 1) ^ 0xB8; }
                else          { rand = (rand >> 1); }
            }

            // compute CRC
            crc = crc ^ (((int)out_byte) << 8);
            for (int j=0; j<8; j++) {
                if ((crc & 0x8000)>0) {
                    crc = (crc << 1) ^ 0x1021;          // polynom is x^16 + x^12 + x^5 + 1, CCITT standard
                } else {
                    crc <<= 1;
                }
            }
            output(listResult,out_byte);
        }
        // send CRC16 in big-endian
        output(listResult,(byte)(crc >> 8));
        output(listResult,(byte)(crc & 0xFF));

        // finally send End of Frame
        listResult.add(ZIGBEE_EZSP_EOF);		// 0x7F
        return ByteUtil.arrayListToBytes(listResult);
    }

    static void output(ArrayList<Byte> listResult,byte out_byte) {
        switch (out_byte) {
            case 0x7E:      // Flag byte
            case 0x11:      // XON
            case 0x13:      // XOFF
            case 0x18:      // Substitute byte
            case 0x1A:      // Cancel byte
            case 0x7D:      // Escape byte
                // case 0xFF:      // special wake-up
                listResult.add(ZIGBEE_EZSP_ESCAPE);      // send Escape byte 0x7D
                listResult.add((byte)(out_byte ^ 0x20));           // send with bit 5 inverted
                break;
            default:
                listResult.add(out_byte);                  // send unchanged
                break;
        }
    }

}
